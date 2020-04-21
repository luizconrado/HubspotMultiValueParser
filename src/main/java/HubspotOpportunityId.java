import models.OpportunityParsed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HubspotOpportunityId {

    private static final Logger logger = LogManager.getLogger(HubspotOpportunityId.class);

    public static void main(String[] args) {

        String upsertPostgresql = "INSERT INTO hubspot_legacy.sfopportunity_mapping (opportunityid, accountid, hubspot_deal_id) VALUES (?, ?, ?);";

        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement preparedStatement = connection.prepareStatement(upsertPostgresql)) {

            connection.setSchema("salesforce");


            statement.execute("create table if not exists hubspot_legacy.sfopportunity_mapping\n" +
                    "(\n" +
                    "\topportunityid varchar(18) not null,\n" +
                    "\taccountid varchar(18) not null,\n" +
                    "\thubspot_deal_id varchar(100) not null\n" +
                    "\t\tconstraint sfopportunity_mapping_pk\n" +
                    "\t\t\tprimary key\n" +
                    ");");

            statement.execute("alter table hubspot_legacy.sfopportunity_mapping owner to postgres;");


            statement.execute("create index if not exists sfopportunity_mapping_opportunityid_index\n" +
                    "\ton hubspot_legacy.sfopportunity_mapping (opportunityid);");


            statement.execute("create index if not exists sfopportunity_mapping_accountid_index\n" +
                    "\ton hubspot_legacy.sfopportunity_mapping (accountid);");


            statement.execute("truncate table hubspot_legacy.sfopportunity_mapping;");


            ResultSet resultSet = statement.executeQuery("SELECT c.\"Id\", c.\"AccountId\", c.\"InfinitecOppID__c\" FROM salesforce.\"Opportunity\" as c WHERE c.\"InfinitecOppID__c\" IS NOT NULL AND c.\"IsDeleted\" = FALSE;");


            List<OpportunityParsed> opportunityParsedList = new ArrayList<>();
            int batchSize = 0;
            while (resultSet.next()) {
                String opportunityId = resultSet.getString("Id");
                String accountId = resultSet.getString("AccountId");
                String hubspotIds = resultSet.getString("InfinitecOppID__c");

                List<String> hubspotIdList = Arrays.asList(hubspotIds.split(";"));

                for (String hubspotId : hubspotIdList) {
                    OpportunityParsed opportunityParsed = new OpportunityParsed();
                    opportunityParsed.setAccountId(accountId);
                    opportunityParsed.setOpportunityId(opportunityId);
                    opportunityParsed.setHubspotId(hubspotId);

                    opportunityParsedList.add(opportunityParsed);

                    preparedStatement.setString(1, opportunityId);
                    preparedStatement.setString(2, accountId);
                    preparedStatement.setString(3, hubspotId);
                    preparedStatement.addBatch();
                    batchSize++;

                    if (batchSize == 50) {
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

            opportunityParsedList.forEach(System.out::println);


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
