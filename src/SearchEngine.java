import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by abonagiri on 11/15/16.
 */
public class SearchEngine extends SimpleFileVisitor<Path> {
    /**
     * Search criteria 1
     */
    public static final String search1Start = "<pc:query";

    /**
     * Search criteria 1
     */
    public static final String search1End = "</pc:query>";
    /**
     * Search criteria 2
     */
    public static final String search2Start = "<pc:queryParam10";
    /**
     * Search criteria 2
     */
    public static final String search2End = "</pc:queryParam10>";
    /**
     * Exception message
     */
    public static final String FILE_NOT_FOUND_EXCEPTION_MSG = "Requested path / file location '%s' is not found! \n Please check path once again!";

    /**
     *
     */
    String sqlQuerySearchCriteria = "(?i).*\"\\s*delete from.*|.*\"\\s*insert into.*|.*\"\\s*update .*|.*\"\\s*select .*";

    /**
     *
     */
    String dbClassAppendSearchCriteria = ".*public.*|.*private.*|.*protected.*";

    /**
     *
     */
    String negativeSearchCriteria = "^|(\\/\\/).*|(\\* @param).*|(\\*).*";

    /**
     *
     */
    String appendCriteria = ".*\\.append.*";

    /**
     * HashMap to store queries correspondong to a file
     */
    static Map<String, List<String[]>> hfs = new HashMap<>();

    /**
     * Default value of the index is 2. Based on condition this value can be manipulated.
     */
    private int specialScenarioAddlIndex = 2;

    /**
     * Argument based constructor, which accepts folder / file path to search files
     * @param path
     * @throws Exception
     */
    public SearchEngine(String path) throws Exception {
        try {
            Files.walkFileTree(Paths.get(path), this);
        } catch (Exception ex) {
            throw new Exception(String.format(FILE_NOT_FOUND_EXCEPTION_MSG, path));
        }
    }

    /**
     *
     * @param dir
     * @param attrs
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().endsWith("gradleBuild")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Visit file mehtod will be invoked by the API immediate after walk file tree invoked
     * @param filePath
     * @param attributes
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) throws IOException {
        searchInJSPs(filePath);
        searchInJavaFiles(filePath);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Findings method will search file line by line and based on the search criteria
     * it will accumulate SQL queries as JSON object.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public FileVisitResult searchInJSPs(Path filePath) throws IOException {
        /**
         * ignore all other files except jsp and inc files.
         */
        if (!(filePath.toString().endsWith(".jsp") || filePath.toString().endsWith(".inc"))) {
            return FileVisitResult.CONTINUE;
        }
        boolean flag = false;
        List<String[]> sl = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.contains(search1Start) || line.contains(search2Start)) {
                flag = true;
            }
            if (flag) {
                String[] str = {line.trim(), line.trim()};
                sl.add(str);
            }
            if (line.contains(search1End) || line.contains(search2End)) {
                flag = false;
                String fileName = filePath.toString();
                hfs.put(fileName,sl);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Here is the begining of search SQL Query with in the java files.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public FileVisitResult searchInJavaFiles(Path filePath) throws IOException {
        /**
         * ignore all other files except java files.
         */
        if (!(filePath.toString().endsWith(".java"))) {
            return FileVisitResult.CONTINUE;
        }
        /**
         * Ignore QueryDatabase and OpsQueryDatabase from the search
         * since those are going to be handle separately due to type of queries
         * it has.
         */
        if (filePath.toString().endsWith("QueryDatabase.java") || filePath.toString().endsWith("OpsQueryDatabase.java")) {
            return FileVisitResult.CONTINUE;
        }

        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);

        boolean append = false;
        boolean additional = false;
        String fileName = filePath.toString();
        List<String[]> strQueryList = new ArrayList<>();
        StringBuffer modifiedQuery = new StringBuffer();
        StringBuffer origianlQuery = new StringBuffer();

        for (String line : lines) {
            if (line.trim().matches(sqlQuerySearchCriteria) || additional || append) {
                // iterate each line of the file and ignore commented lines
                if (!line.trim().matches(negativeSearchCriteria)) {
                    // enable Boolean flags in case of string concatination and string append SQL Queries
                    if(line.trim().endsWith("+") || (line.trim().endsWith("\"") && !line.trim().endsWith(";"))) {
                        additional = true;
                    }
                    if (line.contains(".append")) {
                        append = true;
                    }
                    // accumlate String form of SQL Query string into a buffer object --> and list object
                    if(additional) {
                        origianlQuery.append(line.trim());
                        modifiedQuery.append(filterConcatenateSQLQuery(line.trim()));
                    } else if (append && line.contains(".append")){
                        origianlQuery.append(line.trim());
                        modifiedQuery = parseAppendQueryStrings(line, modifiedQuery);
                    }
                    else if (line.trim().matches(sqlQuerySearchCriteria)){
                        String[] str = {line.trim(), filterSqlQuery(parseExecuteSingleColumnQuery(line.trim()))};
//                        String[] str = {line.trim(), filterSqlQuery(line.trim())};
                        strQueryList.add(str);
                    }
                }
                // break once string concatination sql query logic
                if(additional && line.trim().endsWith(";")) {
                    String[] str = {origianlQuery.toString(), modifiedQuery.toString()};
                    strQueryList.add(str);
                    additional=false;
                    modifiedQuery = new StringBuffer();
                }
                // break once string append sql query logic
                if (append && line.trim().matches(dbClassAppendSearchCriteria)) {
                    String[] str = {origianlQuery.toString(), modifiedQuery.toString()};
                    strQueryList.add(str);
                    append=false;
                    modifiedQuery = new StringBuffer();
                }
            }
        }
        if (!strQueryList.isEmpty())
            hfs.put(fileName,strQueryList);
        return FileVisitResult.CONTINUE;
    }

    /**
     *
     * Replace double quote with a space at the end of the query string
     *
     * @param input
     * @return
     */
    protected String filterConcatenateSQLQuery(String input) {
        String retVal = filterSqlQuery(input);
        retVal = retVal.replaceAll("\""," ");
        retVal = retVal.replaceAll("\\+"," ");
        return retVal;
    }

    /**
     * Parse SQL query by removing semi colon at the end of the query
     *
     * @param inputString
     * @return
     */
    protected String filterSqlQuery(String inputString) {

        String selectSearch = "\"select";
        String selectSearchWithSpace = "\" select";
        String selectSearchWithBracket = "(\"select";

        String updateSearch = "\"update";
        String updateSearchWithSpace = "\" update";
        String updateSearchWithBracket = "(\"update";

        String deleteSearch = "\"delete";
        String deleteSearchWithSpace = "\" delete";
        String deleteSearchWithBracket = "(\"delete";

        String insertSearch = "\"insert";
        String insertSearchWithSpace = "\" insert";
        String insertSearchWithBracket = "(\"insert";

        inputString = inputString.toLowerCase();
        inputString = formatStringBasedOnSUID(inputString, selectSearchWithBracket, selectSearch, selectSearchWithSpace);
        inputString = formatStringBasedOnSUID(inputString, updateSearchWithBracket, updateSearch, updateSearchWithSpace);
        inputString = formatStringBasedOnSUID(inputString, deleteSearchWithBracket, deleteSearch, deleteSearchWithSpace);
        inputString = formatStringBasedOnSUID(inputString, insertSearchWithBracket, insertSearch, insertSearchWithSpace);
        inputString = inputString.replaceAll("\"", " ");

        return parseDynamicValues(inputString);
    }

    /**
     *
     * @param inputString
     * @param searchWithBracket
     * @param search
     * @param searchWithSpace
     * @return
     */
    protected String formatStringBasedOnSUID(String... arguments) {
        String inputString = arguments[0];
        String searchWithBracket = arguments[1];
        String plainSearch = arguments[2];
        String searchWithSpace = arguments[3];
        if (inputString.indexOf(searchWithBracket) > 0) {
            inputString = inputString.substring(inputString.indexOf(searchWithBracket) + 2, inputString.length());
            inputString = inputString.substring(0,inputString.length()-specialScenarioAddlIndex);
        }
        else if (inputString.indexOf(plainSearch) > 0)
            inputString = inputString.substring(inputString.indexOf(plainSearch)+1, inputString.length());
        else if (inputString.indexOf(searchWithSpace) > 0)
            inputString = inputString.substring(inputString.indexOf(searchWithSpace)+1, inputString.length());
        return inputString;
    }

    /**
     * replace #p1...#p2..#p3 values with '-123'
     * Since Oracle allows varchar value to a Integer datatype column
     * <i>Note: This is just for testing purpsoe.</i>
     *
     * @param query
     * @return
     */
    protected String parseDynamicValues(String query) {
        String modifiedVal = query;
        String pattern = "#(?i)p(\\d?)";
        modifiedVal = modifiedVal.replaceAll(pattern, "'-123'");
        return modifiedVal;
    }

    /**
     * parse all sql queries forming with .append logic
     * @param line
     * @param origianlQuery
     * @param sbb
     */
    protected StringBuffer parseAppendQueryStrings(String line, StringBuffer tweakedQuery) {
        String str = line.trim().substring(line.trim().indexOf("("), line.trim().length());
        str = str.replaceAll(".append"," ");
        str = str.replaceAll("\\(\""," ");
        str = str.replaceAll("\"\\)"," ");
        str = str.replaceAll(";"," ");
        tweakedQuery.append(parseDynamicValues(str));
        return tweakedQuery;
    }

    protected String parseExecuteSingleColumnQuery(String originalQuery) {
        if (originalQuery.contains("executeSingleColumnQuery")) {
            if (originalQuery.indexOf("\",") > 0) {
                originalQuery = originalQuery.substring(0, originalQuery.indexOf("\","));
                // chaning default value to 0 since extra string is discarded in this logic no need of additional trimming
                specialScenarioAddlIndex = 0;
            }
        }
        return originalQuery;
    }
}