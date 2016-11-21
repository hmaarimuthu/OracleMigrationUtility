import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class JavaSearch  extends SimpleFileVisitor<Path> {

    /**
     * This gives total number files in the provided location. Including all extenstions.
     */
    static int totalFileCount = 0;

    /**
     * Gives only those java files which meet the below search criteria
     */
    static int resultedFileCount = 0;

    // Search criteria form DataBase class
    String dbClassSearchCriteria = "(?i).*execute.*query.*|.*executeSqlUpdate.*|.*executeSqlInsertWithAutoIncrement.*|.*delete from.*|.*insert into.*|.*update .*|.*select .*";

    public JavaSearch(String path) throws Exception {
        Files.walkFileTree(Paths.get(path), this);
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) throws IOException {
        fetchQueries(filePath);
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult fetchQueries(Path filePath) throws IOException {
        totalFileCount ++;
        if (!(filePath.toString().endsWith(".java"))) {
            return FileVisitResult.CONTINUE;
        }
        resultedFileCount ++;
        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.matches(dbClassSearchCriteria)) {
                System.out.println(line);
                System.out.println();
            }
        }
        return FileVisitResult.CONTINUE;
    }

    public static void main (String args[]) throws Exception {
        // String folderPath = "/Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/intuit/entityeventlistener/entitypublishing/recovery/";
        String folderPath =  "/Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/billing";
        //String folderPath = "/Users/abonagiri/dev/ems/depot/iop/development/Common/";
        new JavaSearch(folderPath);
        System.out.println("Total file count :: "+totalFileCount);
        System.out.println("Total java file count :: "+resultedFileCount);
    }
}
