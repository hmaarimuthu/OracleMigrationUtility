import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.swing.*;

/**
 * File search based on the provided path and given search criteria
 */
public class FileSearch extends SimpleFileVisitor<Path> {

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
     * JSON Array for to store all SQL queries
     */
    static JSONArray jsonList = new JSONArray();

    /**
     * Argument based constructor, which accepts folder / file path to search files
     * @param path
     * @throws Exception
     */
    public FileSearch(String path) throws Exception {
        try {
            Files.walkFileTree(Paths.get(path), this);
        } catch (Exception ex) {
            throw new Exception(String.format(FILE_NOT_FOUND_EXCEPTION_MSG, path));
        }
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
        findings(filePath);
        return FileVisitResult.CONTINUE;
    }

    /**
     * preVisitDirectory will be invoked by the API
     * @param dir
     * @param attributes
     * @return
     * @throws IOException
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Findings method will search file line by line and based on the search criteria
     * it will accumulate SQL queries as JSON object.
     * @param filePath
     * @return
     * @throws IOException
     */
    public FileVisitResult findings(Path filePath) throws IOException {

        boolean flag = false;

        StringBuilder sb = new StringBuilder();

        if (!(filePath.toString().endsWith(".jsp") || filePath.toString().endsWith(".inc"))) {
            return FileVisitResult.CONTINUE;
        }
        List<String> lines = Files.readAllLines(Paths.get(filePath.toString()), StandardCharsets.ISO_8859_1);
        JSONObject jsonObject = null;
        for (String line : lines) {
            if (line.contains(search1Start) || line.contains(search2Start)) {
                flag = true;
                jsonObject = new JSONObject();
            }
            if (flag) {
                sb.append(line.trim());
            }
            if (line.contains(search1End) || line.contains(search2End)) {
                flag = false;
                String fileName = filePath.toString();
                fileName = fileName.substring(fileName.lastIndexOf('/'), fileName.length());
                jsonObject.put(fileName, sb.toString());
                jsonList.put(jsonObject);
            }
        }
        //System.out.println("jsonList:::::"+jsonList);
        return FileVisitResult.CONTINUE;
    }

    /**
     * main method to call File search functionality
     * @param args
     */
    public static void main(String args[]) {
        // create a jframe
        JFrame frame = new JFrame();
        try {
            //String folderPath = "/Users/abonagiri/dev/ems/depot/iop/development-shard/Public/WebApp";
            String folderPath = JOptionPane.showInputDialog("Enter Path:");

            if (null != folderPath && folderPath.trim().length() > 0) {
                new FileSearch(folderPath);
                System.out.println(jsonList);

                // create a JTextArea
                JTextArea textArea = new JTextArea(10, 75);
                textArea.setText(jsonList.toString());
                textArea.setLineWrap(true);
                textArea.setEditable(false);

                // wrap a scrollpane around it
                JScrollPane scrollPane = new JScrollPane(textArea);

                // display them in a message dialog
                JOptionPane.showMessageDialog(frame, scrollPane, "Result - JSON Format!", JOptionPane.DEFAULT_OPTION);

                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(frame, "No path provided! Please provide a path!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}