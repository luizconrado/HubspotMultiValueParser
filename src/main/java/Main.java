import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement()) {

            connection.setSchema("hubspot_legacy");

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet company = metaData.getColumns(null, null, "geographical_focus", null);

            while (company.next()) {
                String columnName = company.getString("COLUMN_NAME");
                String datatype = company.getString("DATA_TYPE");
                String columnsize = company.getString("COLUMN_SIZE");
                String decimaldigits = company.getString("DECIMAL_DIGITS");
                String isNullable = company.getString("IS_NULLABLE");
                String is_autoIncrment = company.getString("IS_AUTOINCREMENT");
                //Printing results
                System.out.println(columnName + "---" + datatype + "---" + columnsize + "---" + decimaldigits + "---" + isNullable + "---" + is_autoIncrment);
            }

            ResultSet resultSet = statement.executeQuery("SELECT geographical_focus FROM company");

            String[] webTechnologies = new String[15000];
            int currentSize = 0;
            while (resultSet.next()) {
                ResultSetMetaData metaData1 = resultSet.getMetaData();

                for (int i = 1; i <= metaData1.getColumnCount(); i++) {


                    String webTechnologiesLine = resultSet.getString(i);
                    System.out.println(webTechnologiesLine);
                    if (webTechnologiesLine != null) {
                        String[] webTechnologiesTemp = webTechnologiesLine.split(";");
                        System.out.println(webTechnologiesTemp.length);

                        for (int j = 0; j < webTechnologiesTemp.length; j++) {
                            webTechnologies[currentSize] = webTechnologiesTemp[j];
                            currentSize++;
                        }
                    }


                }
            }

            Set<String> webTechnologiesSet = new TreeSet<>(String::compareToIgnoreCase);
            List<String> webTechnolgiesList = Arrays.asList(webTechnologies)
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            webTechnologiesSet.addAll(webTechnolgiesList);

            System.out.println();
            System.out.println("----------- Results ------------- ");
            System.out.println();

            webTechnologiesSet.forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
