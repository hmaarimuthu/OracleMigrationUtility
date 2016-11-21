import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;

import javax.swing.*;
import java.io.FileOutputStream;

/**
 * File search based on the provided path and given search criteria
 */
public class FileSearch {

    public void callSearchEngine() {
        // create a jframe
        JFrame frame = new JFrame();
        try {
            // String folderPath = "/Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/webservices/
            // vendors/liveperson/PostChatTranscriptServlet.java";
            // String folderPath = "/Users/abonagiri/dev/ems/depot/iop/development/Common/";
            String folderPath = JOptionPane.showInputDialog("Enter Path:");

            if (null != folderPath && folderPath.trim().length() > 0) {
                String fileName = "OracleMigration.xls";
                SearchEngine fs = new SearchEngine(folderPath);
                fs.deleteFile(fileName);
                HSSFWorkbook workbook = fs.writeToXL();
                try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                    workbook.write(outputStream);
                }

                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(frame, "No path provided! Please provide a path!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            System.exit(0);
        }
    }
    public static void main(String args[]) {
        new FileSearch().callSearchEngine();
    }

}