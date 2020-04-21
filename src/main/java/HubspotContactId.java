import models.ContactParsed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubspotContactId {

    private static final Logger logger = LogManager.getLogger(HubspotContactId.class);

    public static void main(String[] args) {

        String upsertPostgresql = "INSERT INTO hubspot_legacy.sfcontact_mapping (contactid, accountid, hubspot_contact_id) VALUES (?, ?, ?);";

        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(upsertPostgresql)) {

            connection.setSchema("salesforce");


            statement.execute("create table if not exists hubspot_legacy.sfcontact_mapping\n" +
                    "(\n" +
                    "\tcontactid varchar(18) not null,\n" +
                    "\taccountid varchar(18) not null,\n" +
                    "\thubspot_contact_id varchar(100) not null\n" +
                    "\t\tconstraint sfcontact_mapping_pk\n" +
                    "\t\t\tprimary key\n" +
                    ");");

            statement.execute("alter table hubspot_legacy.sfcontact_mapping owner to postgres;");


            statement.execute("create index if not exists sfcontact_mapping_contactid_index\n" +
                    "\ton hubspot_legacy.sfcontact_mapping (contactid);");


            statement.execute("create index if not exists sfcontact_mapping_accountid_index\n" +
                    "\ton hubspot_legacy.sfcontact_mapping (accountid);");


            statement.execute("truncate table hubspot_legacy.sfcontact_mapping;");



            ResultSet resultSet = statement.executeQuery("SELECT c.\"Id\", c.\"AccountId\", c.\"InfinitecContactID__c\" FROM salesforce.\"Contact\" as c WHERE c.\"InfinitecContactID__c\" IS NOT NULL AND c.\"IsDeleted\" = FALSE;");


            List<ContactParsed> contactParsedList = new ArrayList<>();
            int batchSize = 0;
            while (resultSet.next()) {
                String contactId = resultSet.getString("Id");
                String accountId = resultSet.getString("AccountId");
                String hubspotIds = resultSet.getString("InfinitecContactID__c");

                List<String> hubspotIdList = Arrays.asList(hubspotIds.split(";"));

                for (String hubspotId : hubspotIdList) {
                    ContactParsed contactParsed = new ContactParsed();
                    contactParsed.setAccountId(accountId);
                    contactParsed.setContactId(contactId);
                    contactParsed.setHubspotId(hubspotId);

                    contactParsedList.add(contactParsed);

                    preparedStatement.setString(1, contactId);
                    preparedStatement.setString(2, accountId);
                    preparedStatement.setString(3, hubspotId);
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

            contactParsedList.forEach(System.out::println);





        } catch (SQLException e) {
            e.printStackTrace();
        }


    }




}
