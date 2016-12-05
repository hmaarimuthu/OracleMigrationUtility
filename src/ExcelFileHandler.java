import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by abonagiri on 11/30/16.
 */
public class ExcelFileHandler {

    /**
     * Queries on a single file
     */
    static List<String> hl = new ArrayList<String>();

    String selectQueryExecuteCriteria = "(?i)\\s*select.*";

    String insertQueryExecuteCriteria = "(?i)\\s*insert into.*";

    /**
     *
     */
    Connection dbconn = null;

    // TODO: these once remove later
    int successCounter = 0;
    int noResultCounter = 0;
    int errorCounter = 0;

    /**
     *  Creating XL header
     */
    private List<String> getHeaderDetails(){
        hl.add("File Name");
        hl.add("File Full Path");
        hl.add("Original Query");
        hl.add("Modified Query");
        hl.add("Query Compatable status");
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
    public HSSFWorkbook writeToXL(Map<String, List<String[]>> hfs, boolean flagDbExecution) {
        int rowCount = 0;
        int columnCount = 0;

        getHeaderDetails();

        // Create new sheet and set style
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Public");
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        // set column width
        for (int i = 0; i < 6; i++) {
            // sheet.autoSizeColumn(i, true);
            sheet.setColumnWidth(i, 7000);
        }

        // creating header row
        Row row = sheet.createRow(rowCount++);
        for (String line : hl) {
            Cell cell = row.createCell(columnCount++);
            cell.setCellValue(line);
        }

        // adding details
        for (String fileName : hfs.keySet()) {
            List<String[]> tl = new ArrayList<>();
            tl = hfs.get(fileName);

            for (String[] queryString : tl) {
                columnCount = 0;
                row = sheet.createRow(rowCount++);

                // filename cell
                Cell cell = row.createCell(columnCount++);
                cell.setCellValue(fileName.substring(fileName.lastIndexOf('/'), fileName.length()));
                cell.setCellStyle(cellStyle);

                // fullpath cell
                cell = row.createCell(columnCount++);
                cell.setCellValue(fileName);
                cell.setCellStyle(cellStyle);

                // Original Query cell
                cell = row.createCell(columnCount++);
                cell.setCellValue(queryString[0]);
                cell.setCellStyle(cellStyle);

                // Modified SQL Query cell
                cell = row.createCell(columnCount++);
                cell.setCellValue(queryString[1]);
                cell.setCellStyle(cellStyle);

                // Status
                cell = row.createCell(columnCount++);
                if (flagDbExecution) {
                    if (queryString[1].matches(selectQueryExecuteCriteria)) {
                        cell.setCellValue(validateSelectQuerySyntax(queryString[1]));
                    }
                }
                cell.setCellStyle(cellStyle);
            }
        }
        return workbook;
    }

    /**
     *
     * @param query
     */
    public boolean validateSelectQuerySyntax(String selectQuery) {
        // TODO: Filter update / delete / insert into statements. They need stmt.executeUpdate
        Statement stmt = null;
        try {
            // Getting a new DB connection
            dbconn = MyDatabaseConnection.getConnection();
            // create statment object
            stmt = dbconn.createStatement();
            stmt.setQueryTimeout(5);
            // format sql query before execution
            selectQuery = (selectQuery.charAt(selectQuery.length()-1) == ';') ? selectQuery.substring(0,selectQuery.length()-1) : selectQuery;
            // execute sql query
            boolean syntaxCheck = stmt.execute(selectQuery);
            // check result of sql query execution
            if (syntaxCheck) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqlex) {
            System.out.println("\n "+ errorCounter++ +"Error --------------" + "Exception occurred!!!" + sqlex.getMessage() + " ----- " + selectQuery);
            return false;
        } finally {
            try {
                // close statement object
                stmt.close();
                // close db connection object
                dbconn.close();
            } catch (SQLException ex) {

            }
        }
    }
}
