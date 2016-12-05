import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import java.io.FileOutputStream;

/**
 * File search based on the provided path and given search criteria
 */
public class FileSearch {

    public static final boolean flag_syntax_check = true;

    public void callSearchEngine() {
        // create a jframe
        JFrame frame = new JFrame();
        try {
             /*
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/webservices/vendors/liveperson/PostChatTranscriptServlet.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/tx/CompanyTx.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/efile/TexasEnrollmentManager.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/efile/BulkEFile.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/tx/PartnerTx.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/biz/CompanyDeletion.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/intuit/payroll/platform/contractor/ContractorDAOEclipseLink.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/payroll/DirectDepositAnswers.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/biz/Company.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/data/CompanyTaxInfo.java
             /Users/abonagiri/dev/ems/depot/iop/development/Common/src/com/paycycle/util/DatabasePropertiesMgr.java
             */

            String folderPath = JOptionPane.showInputDialog("Enter Path:");
            folderPath = "/Users/abonagiri/dev/ems/depot/iop/development/Common/";

            if (null != folderPath && folderPath.trim().length() > 0) {
                String fileName = "OracleMigration_12.xls";
                // search in the specified folder
                SearchEngine fs = new SearchEngine(folderPath);
                // create excel with the result
                ExcelFileHandler excelFile = new ExcelFileHandler();
                excelFile.deleteFile(fileName);
                HSSFWorkbook workbook = excelFile.writeToXL(SearchEngine.hfs, flag_syntax_check);
                try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                    workbook.write(outputStream);
                }
                // exit program upon done!!!
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