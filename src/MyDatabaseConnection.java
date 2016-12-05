import java.sql.*;

/**
 *
 */
public class MyDatabaseConnection {

    private static Connection connection = null;

    /**
     *
     * @return
     */
    public static Connection getConnection() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@pprfiopdb804.ie.intuit.net:1521:iop3dev", "iop_app", "iop_app");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return null;
        }
        return connection;
    }

    /**
     *
     * @return
     */
    public static Connection getIntance() {
        if (null == connection) {
            connection = getConnection();
            System.out.println("Connection created!!!");
        }
        return connection;
    }
}