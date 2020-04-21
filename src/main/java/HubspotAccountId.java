import models.AccountParsed;
import models.ContactParsed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubspotAccountId {

    private static final Logger logger = LogManager.getLogger(HubspotAccountId.class);

    public static void main(String[] args) {

        String upsertPostgresql = "INSERT INTO hubspot_legacy.sfaccount_mapping (accountid, hubspot_account_id) VALUES (?, ?);";

        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(upsertPostgresql)) {

            connection.setSchema("salesforce");


            statement.execute("create table if not exists hubspot_legacy.sfaccount_mapping\n" +
                    "(\n" +
                    "\taccountid varchar(18) not null,\n" +
                    "\thubspot_account_id varchar(100) not null\n" +
                    "\t\tconstraint sfaccount_mapping_pk\n" +
                    "\t\t\tprimary key\n" +
                    ");");

            statement.execute("alter table hubspot_legacy.sfaccount_mapping owner to postgres;");


            statement.execute("create index if not exists sfaccount_mapping_accountid_index\n" +
                    "\ton hubspot_legacy.sfaccount_mapping (accountid);");


            statement.execute("truncate table hubspot_legacy.sfaccount_mapping;");



//            ResultSet resultSet = statement.executeQuery("SELECT c.\"Id\", c.\"InfinitecAccID__c\" FROM salesforce_legacy.\"Account\" as c WHERE c.\"InfinitecAccID__c\" IS NOT NULL AND c.\"IsDeleted\" = FALSE;");
//            ResultSet resultSet = statement.executeQuery("SELECT c.\"Id\", c.\"InfinitecAccID__c\" FROM salesforce_legacy.\"Account\" as c WHERE c.\"InfinitecAccID__c\" IS NOT NULL;");
            ResultSet resultSet = statement.executeQuery("SELECT c.\"Id\", c.\"InfinitecAccID__c\" FROM salesforce.\"Account\" as c WHERE c.\"InfinitecAccID__c\" IS NOT NULL AND c.\"IsDeleted\" = FALSE;");


            List<AccountParsed> accountParsedList = new ArrayList<>();
            int batchSize = 0;
            while (resultSet.next()) {
                String accountId = resultSet.getString("Id").trim();
                String hubspotIds = resultSet.getString("InfinitecAccID__c").trim();

                List<String> hubspotIdList = Arrays.asList(hubspotIds.split(";"));

                for (String hubspotId : hubspotIdList) {
                    AccountParsed accountParsed = new AccountParsed();
                    accountParsed.setAccountId(accountId);
                    accountParsed.setHubspotId(hubspotId);

                    accountParsedList.add(accountParsed);

                    preparedStatement.setString(1, accountId);
                    preparedStatement.setString(2, hubspotId);
                    preparedStatement.addBatch();
                    batchSize++;

                    if (batchSize == 50){
                        int[] executeBatchResults = preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        System.out.println(Arrays.toString(executeBatchResults));
                        batchSize = 0;
                    }


                }

            }

            if(resultSet.isAfterLast()){
                int[] executeBatchResults = preparedStatement.executeBatch();
                System.out.println(Arrays.toString(executeBatchResults));
            }

            preparedStatement.close();
            statement.close();
            resultSet.close();
            connection.close();

            accountParsedList.forEach(System.out::println);





        } catch (SQLException e) {
            e.printStackTrace();
        }


    }




}
