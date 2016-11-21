/**
 * Created by abonagiri on 11/15/16.
 */
public class Test {

    public void addFormW2BindSource_hasRetirementCompanyContributions() {
        final String QUERY = "SELECT EmployerContributionId, SUM(Amount) AS ContributionAmount " +
                "FROM ContributionDetail " +
                "WHERE EmployeeId = #employeeId AND CheckDate BETWEEN #periodStartDate AND #periodEndDate " +
                "GROUP BY EmployerContributionId";
        String[][] arguments = { {"employeeId","java.lang.Long"}, {"periodStartDate","java.util.Date"}, {"periodEndDate","java.util.Date"}
        };
        //addDataReadQuery(QUERY, FormW2BindSource_hasRetirementCompanyContributions, arguments);
    }
}
