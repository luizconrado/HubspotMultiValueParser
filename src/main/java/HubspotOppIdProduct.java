import models.OpportunityParsed;
import models.OpportunityProductsParsed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubspotOppIdProduct {

    private static final Logger logger = LogManager.getLogger(HubspotOppIdProduct.class);

    public static void main(String[] args) {

        String upsertPostgresql = "INSERT INTO public.opportunity_product_mapping (opportunityid, accountid, product_name) VALUES (?, ?, ?);";

        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(upsertPostgresql)) {

            connection.setSchema("salesforce");


            statement.execute("create table if not exists public.opportunity_product_mapping\n" +
                    "(\n" +
                    "\topportunityid varchar(18) not null,\n" +
                    "\taccountid varchar(18) not null,\n" +
                    "\tproduct_name varchar(100) not null\n" +
                    ");");

            statement.execute("alter table public.opportunity_product_mapping owner to postgres;");


            statement.execute("create index if not exists sfoppproduct_mapping_opportunityid_index\n" +
                    "\ton public.opportunity_product_mapping (opportunityid);");


            statement.execute("create index if not exists sfoppproduct_mapping_accountid_index\n" +
                    "\ton public.opportunity_product_mapping (accountid);");


            statement.execute("truncate table public.opportunity_product_mapping;");


            ResultSet resultSet = statement.executeQuery("SELECT opp.\"Id\",\n" +
                    "       opp.\"AccountId\",\n" +
                    "       opp.\"Product__c\"\n" +
                    "\n" +
                    "FROM salesforce.\"Opportunity\" as opp\n" +
                    "\n" +
                    "WHERE 1=1\n" +
                    "AND opp.\"IsDeleted\" IS FALSE\n" +
                    "AND opp.\"Product__c\" IS NOT NULL\n" +
                    "\n" +
                    "ORDER BY opp.\"SystemModstamp\" ASC;");


            List<OpportunityProductsParsed> oppProductParsedList = new ArrayList<>();
            int batchSize = 0;
            while (resultSet.next()) {
                String opportunityId = resultSet.getString("Id");
                String accountId = resultSet.getString("AccountId");
                String products = resultSet.getString("Product__c");

                List<String> productsList = Arrays.asList(products.split(";"));

                for (String product : productsList) {
                    OpportunityProductsParsed oppProductParsed = new OpportunityProductsParsed();
                    oppProductParsed.setAccountId(accountId);
                    oppProductParsed.setOpportunityId(opportunityId);
                    oppProductParsed.setProductName(product);

                    oppProductParsedList.add(oppProductParsed);

                    preparedStatement.setString(1, opportunityId);
                    preparedStatement.setString(2, accountId);
                    preparedStatement.setString(3, product);
                    preparedStatement.addBatch();
                    batchSize++;

                    if (batchSize == 200) {
                        int[] executeBatchResults = preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        System.out.println(Arrays.toString(executeBatchResults));
                        batchSize = 0;
                    }


                }

            }

            if (resultSet.isAfterLast()) {
                int[] executeBatchResults = preparedStatement.executeBatch();
                System.out.println(Arrays.toString(executeBatchResults));
            }

            preparedStatement.close();
            statement.close();
            resultSet.close();
            connection.close();

            oppProductParsedList.forEach(System.out::println);


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
