import java.sql.*;

public class OracleJDBC {

    public static void main(String[] argv) {

        System.out.println("-------- Oracle JDBC Connection Testing ------");

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return;

        }

        System.out.println("Oracle JDBC Driver Registered!");

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@pprfiopdb804.ie.intuit.net:1521:iop3dev", "iop_app", "iop_app");

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM IOP_APP.employees where ROWNUM < 5");

            while (rs.next()) {

                int personid = rs.getInt("PERSONID"); // get first column returned
                int companyid = rs.getInt("COMPANYID"); // get first column returned
                String bankRoutingNumber = rs.getString("BANKROUTINGNUMBER"); // get first column returned

                System.out.println("Person Id :: " + personid+ " Company Id:: " + companyid + "Bank Routing Number::"+ bankRoutingNumber);
            }

            if (connection != null) {
                System.out.println("You made it, take control your database now!");
            } else {
                System.out.println("Failed to make connection!");
            }

            rs.close();

            stmt.close();

            connection.close();

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

    }

}