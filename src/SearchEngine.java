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
     * Search criteria of DataBase.java
     */
    //String dbClassSearchCriteria = "(?i).*execute.*query.*|.*executeSqlUpdate.*|.*executeSqlInsertWithAutoIncrement.*|.*delete from.*|.*insert into.*|.*update .*|.*select .*";

    // String dbClassSearchCriteria = "(?i).*Database.execute.*|.*stmt.excute.*|.*Database.readObject.*";

    String dbClassSearchCriteria = "(?i).*delete from.*|.*insert into.*|.*update .*|.*select .*";

    //String tunedSearchCriteria = "(?i)Database.exec.*|.*stmt..*";

    String negativeSearchCriteria = "^|(\\/\\/).*|(\\* @param).*|(\\*).*";

    int fileCounter = 1;

    /**
     * HashMap to store queries correspondong to a file
     */
    static Map<String, List<String>> hfs = new HashMap<>();
    /**
     * Queries on a single file
     */
    static List<String> hl = new ArrayList<String>();

    /**
     * Argument based constructor, which accepts folder / file path to search files
     * @param path
     * @throws Exception
     */
    public SearchEngine(String path) throws Exception {
        try {
            getHeaderDetails();
            Files.walkFileTree(Paths.get(path), this);
        } catch (Exception ex) {
            throw new Exception(String.format(FILE_NOT_FOUND_EXCEPTION_MSG, path));
        }
    }

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
     * @param filePath
     * @return
     * @throws IOException
     */
    public FileVisitResult searchInJSPs(Path filePath) throws IOException {
        if (!(filePath.toString().endsWith(".jsp") || filePath.toString().endsWith(".inc"))) {
            return FileVisitResult.CONTINUE;
        }
        int counter = 0;
        boolean flag = false;
        List<String> sl = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.contains(search1Start) || line.contains(search2Start)) {
                flag = true;
            }
            if (flag) {
                sl.add(line.trim());
            }
            if (line.contains(search1End) || line.contains(search2End)) {
                flag = false;
                String fileName = filePath.toString();
                if (counter == 0)
                    System.out.println(fileCounter++ + ": "+fileName);
                fileName = fileName.substring(fileName.lastIndexOf('/'), fileName.length());
                hfs.put(fileName,sl);
                //System.out.println("\t Occurance "+ counter +": "+sb.toString() + "\n");
                counter ++;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult searchInJavaFiles(Path filePath) throws IOException {
        if (!(filePath.toString().endsWith(".java"))) {
            return FileVisitResult.CONTINUE;
        }
        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);
        int counter = 0;
        List<String> sl = new ArrayList<>();
        String fileName = filePath.toString();
        fileName = fileName.substring(fileName.lastIndexOf('/'), fileName.length());

        for (String line : lines) {
            if (line.trim().matches(dbClassSearchCriteria)) {
                if (!line.trim().matches(negativeSearchCriteria)) {
                    if (counter == 0) {
                        System.out.println(fileCounter++ + ": " + filePath);
                    }
                    System.out.println("\t Occurance " + counter + ": " + line.trim());
                    System.out.println();
                    sl.add(line.trim());
                    counter++;
                }
            }
        }

        if (!sl.isEmpty()) hfs.put(fileName,sl);
        return FileVisitResult.CONTINUE;
    }

    /**
     *  Creating XL header
     */

    private List<String> getHeaderDetails(){
        hl.add("PageName");
        hl.add("SQL Query");
        hl.add("ANSI Standard");
        hl.add("Error messages/Comments");
        hl.add("Oracle Compliant Query - Tested Queries on Oracle DB");
        return hl;
    }

    /**
     * Delete if file Exists
     */
    public void deleteFile(String fileName){
        try {
            File file = new File(fileName);
            if (file.exists()){
                file.delete();
            }
        }catch(Exception ex){
            // if any error occurs
            ex.printStackTrace();
        }

    }

    /**
     * Write to XL
     */
    public HSSFWorkbook writeToXL() {

        //Create new sheet and set style
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Public");
        HSSFCellStyle cellStyle = workbook.createCellStyle();

        //set column width
        for (int i = 0; i < 6; i++) {
            //sheet.autoSizeColumn(i, true);
            sheet.setColumnWidth(i, 7000);
        }

        int rowCount = 0;
        int columnCount = 0;

        //creating header row
        Row row = sheet.createRow(rowCount++);
        for (String line : hl) {
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(line);
        }
        //adding details
        for (String filename : hfs.keySet()) {
            List<String> tl = new ArrayList<>();
            tl = hfs.get(filename);
            columnCount = 0;
            row = sheet.createRow(rowCount++);
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(filename);
            cell = row.createCell(columnCount++);
            StringBuilder sbr = new StringBuilder();
            int i =1;
            for (String eachQuery : tl) {
                sbr.append("Occurance "+ i++ +":: "+eachQuery);
                sbr.append("\n\n");
            }
            cell.setCellValue(sbr.toString());
            cell.setCellStyle(cellStyle);
            cellStyle.setWrapText(true);

        }
        return workbook;
    }

}
