import models.AccountTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class SalesforceAccountTree {

    private static final Logger logger = LogManager.getLogger(SalesforceAccountTree.class);

    private static long counter = 0;
    private static Map<String, AccountTree> accountTrees = new TreeMap<>(String::compareToIgnoreCase);
    private static Map<String, AccountTree> subAccountTrees = new TreeMap<>(String::compareToIgnoreCase);

    public static void main(String[] args) {


        try (Connection connection = HikariPostgreSQL.getConnection();
             Statement statement = connection.createStatement()) {

            String query = "SELECT a.\"Id\"\n" +
                    "FROM salesforce.\"Account\" as a\n" +
                    "\n" +
                    "WHERE 1=1\n" +
                    "AND a.\"IsDeleted\" = FALSE\n" +
                    "AND a.\"ParentId\" IS NULL\n" +
//                    "AND a.\"Id\" = '0012o00002W1fxXAAR'\n" +
                    "\n" +
                    "ORDER BY a.\"Id\";";

            ResultSet resultSet = statement.executeQuery(query);



            while (resultSet.next()) {
                AccountTree accountTree = new AccountTree();
                String value = resultSet.getString("Id");
                accountTree.setId(value);
                accountTree.setRootId(value);
                accountTree.setGroup(counter++);

                System.out.println(accountTree);

                accountTrees.put(accountTree.getId(), accountTree);

            }

            statement.close();
            connection.close();


            System.out.println();
            System.out.println("----------------------- Initial Loop Over --------------------");
            System.out.println();


//            accountTrees.values().parallelStream().map(SalesforceAccountTree::getDaughters).forEach(accountTrees::putAll);

//            accountTrees.values().parallelStream().forEach(SalesforceAccountTree::getDaughters);
//            accountTrees.putAll(subAccountTrees);


            for (AccountTree acc : accountTrees.values()) {

//                accountTrees.putAll(getDaughters(acc));
                getDaughters(acc);

            }

            accountTrees.putAll(subAccountTrees);


            System.out.println();
            System.out.println("---------------------------- DONE ---------------------------------");
            System.out.println();
            System.out.println();


            System.out.println(accountTrees.values());


            System.out.println();
            System.out.println("---------------------------- Saving Results ---------------------------------");
            System.out.println();
            System.out.println();

            saveResults(accountTrees);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }





    public static Map<String, AccountTree> getDaughters(AccountTree acc) {


        System.out.println("Group " + acc.getGroup() + ": 1. Going for getDaughters => " + acc);


        if (acc.isProcessed()) {
            System.out.println("*********** Method isProcessed is being used!!!! Why? " + acc + " ******************");
            return subAccountTrees;
        }


        String query2 = "SELECT a.\"Id\"\n" +
                "FROM salesforce.\"Account\" as a\n" +
                "\n" +
                "WHERE 1=1\n" +
                "AND a.\"IsDeleted\" = FALSE\n" +
                "AND a.\"ParentId\" = ?\n" +
                "\n" +
                "ORDER BY a.\"Id\";";

        try (Connection connection2 = HikariPostgreSQL.getConnection();
             PreparedStatement preparedStatement2 = connection2.prepareStatement(query2)) {

            preparedStatement2.setString(1, acc.getId());

            ResultSet resultSet2 = preparedStatement2.executeQuery();


            while (resultSet2.next()) {
                AccountTree accountTree = new AccountTree();

                accountTree.setParentId(acc.getId());
                accountTree.setRootId(acc.getRootId());
                accountTree.setGroup(acc.getGroup());

                String value = resultSet2.getString("Id");

                accountTree.setId(value);
//                accountTree.setProcessed(true);

                System.out.println("Group " + acc.getGroup() + ": 2. Daughter Account Found => " + accountTree);

                subAccountTrees.put(accountTree.getId(), accountTree);

                getDaughters(accountTree);

            }

            acc.setProcessed(true);
            System.out.println("Group " + acc.getGroup() + ": 3. Flagged as Processed => " + acc);


            resultSet2.close();
            preparedStatement2.close();
            connection2.close();


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return subAccountTrees;

    }


    static void saveResults(Map<String, AccountTree> map) {

        String upsertPostgresql = "INSERT INTO public.sfaccount_tree (\"id\", \"parentId\", \"rootId\", \"groupId\", \"processed\") VALUES (?, ?, ?, ?, ?);";

        try (Connection connection3 = HikariPostgreSQL.getConnection();
             Statement statement3 = connection3.createStatement();
             PreparedStatement preparedStatement3 = connection3.prepareStatement(upsertPostgresql)) {

            connection3.setSchema("salesforce");

            statement3.execute("DROP TABLE IF EXISTS public.sfaccount_tree;");

            statement3.execute("create table if not exists public.sfaccount_tree\n" +
                    "(\n" +
                    "\tid varchar(18) not null\n" +
                    "\t\tconstraint sfaccount_tree_pk\n" +
                    "\t\t\tprimary key,\n" +
                    "\t\"parentId\" varchar(18),\n" +
                    "\t\"rootId\" varchar(18),\n" +
                    "\t\"groupId\" bigint,\n" +
                    "\tprocessed boolean\n" +
                    ");");

            statement3.execute("alter table public.sfaccount_tree owner to postgres;");


            statement3.execute("create index sfaccount_tree_parentid_index\n" +
                    "\ton public.sfaccount_tree (\"parentId\");");

            statement3.execute("create index sfaccount_tree_rootid_index\n" +
                    "\ton public.sfaccount_tree (\"rootId\");");


            statement3.execute("truncate table public.sfaccount_tree;");


            int batchSize = 0;


            for (AccountTree accountTree : map.values()) {

                preparedStatement3.setString(1, accountTree.getId());
                preparedStatement3.setString(2, accountTree.getParentId());
                preparedStatement3.setString(3, accountTree.getRootId());
                preparedStatement3.setLong(4, accountTree.getGroup());
                preparedStatement3.setBoolean(5, accountTree.isProcessed());
                preparedStatement3.addBatch();
                batchSize++;

                if (batchSize == 200) {
                    int[] executeBatchResults = preparedStatement3.executeBatch();
                    preparedStatement3.clearBatch();
                    System.out.println(Arrays.toString(executeBatchResults));
                    batchSize = 0;
                }


            }

            if (batchSize > 0) {
                int[] executeBatchResults = preparedStatement3.executeBatch();
                System.out.println(Arrays.toString(executeBatchResults));
            }

            preparedStatement3.close();
            statement3.close();
            connection3.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

}
