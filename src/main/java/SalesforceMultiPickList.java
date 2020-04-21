import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class SalesforceMultiPickList {

    private static final Logger logger = LogManager.getLogger(SalesforceMultiPickList.class);

    public static void main(String[] args) {


        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement()) {

            String query = "SELECT DISTINCT\n" +
                    "REPLACE(INITCAP(REPLACE(c.web_technologies, '_', ' ')),'  ', '') as web_technologies\n" +
                    "\n" +
                    "FROM hubspot_legacy.company as c\n" +
                    "\n" +
                    "WHERE 1=1\n" +
                    "AND c.web_technologies IS NOT NULL;";

            ResultSet resultSet = statement.executeQuery(query);

            Set<String> valuesList = new TreeSet<>(String::compareToIgnoreCase);
            while (resultSet.next()) {
                String values = resultSet.getString("web_technologies");
                if (values.contains(";")) {
                    valuesList.addAll(Arrays.asList(values.split(";")));
                } else {
                    valuesList.add(values);
                }
            }

            valuesList.forEach(System.out::println);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
