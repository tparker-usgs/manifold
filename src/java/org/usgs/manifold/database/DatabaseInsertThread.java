package org.usgs.manifold.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer thread to read {@link DatabaseInsertStatement}s off a
 * {@link BlockingQueue} and execute them on a given database connection.
 *
 */
public class DatabaseInsertThread extends Thread {

    private static Logger log =
            LoggerFactory.getLogger(DatabaseInsertThread.class);
    
    /** Known good tables. */
    private final Set<String> validTables = new HashSet<String>();
    /** Known bad tables. */
    private final Set<String> invalidTables = new HashSet<String>();
    /** Connection string specifying the address, username and password. */
    private final String dbURL;
    /** Database connection object. */
    private Connection dbConnection;
    /** Used to execute SQL insertStatements on the connection. */
    private final QueryRunner runQuery;
    /** Holds insertStatements to consumed and be executed. */
    private final BlockingQueue<DatabaseInsertStatement> insertStatements;
    /** Milliseconds to sleep between reconnection attempts. */
    private final int RECONNECT_DELAY;
    /** Specifies if the thread should be stopped. */
    private boolean run;

    /**
     * @param statements a blocking queue of {@link DatabaseInsertStatement}
     *        objects to run on the database connection.
     * @param dbURL database connection string specifying the address, username
     *        and password.
     */
    public DatabaseInsertThread(
            BlockingQueue<DatabaseInsertStatement> statements, String dbURL) {

        this.dbURL = dbURL;
        this.dbConnection = null;
        this.runQuery = new QueryRunner();
        this.insertStatements = statements;

        RECONNECT_DELAY = 30000;
        run = true;

        DriverManager.setLoginTimeout(3);
    }

    /** 
     * Connect to a database using the URL provided in the constructor. If a
     * {@code SQLException} is thrown during the connection process the
     * {@code Thread} is put to sleep for a time before another connection
     * attempt is made.
     */
    private void connect() {

        validTables.clear();
        invalidTables.clear();
        
        while (dbConnection == null) {

            try {
                dbConnection = DriverManager.getConnection(dbURL);

            } catch (SQLException e) {
                log.warn("Could not connect to the database.", e);

                DbUtils.closeQuietly(dbConnection);
                dbConnection = null;

                try {
                    Thread.sleep(RECONNECT_DELAY);
                } catch (InterruptedException ex) {
                    /*don't care*/
                }
            }
        }
    }

    /**
     * Reads {@link DatabaseInsertStatement} objects from the blocking queue and
     * uses them to insert data into a database. If there is an
     * {@code SQLException} the database connection is closed and a reconnection
     * attempt is made but the data from the {@link DatabaseInsertStatement} is
     * lost.
     */
    @Override
    public void run() {

        connect();

        while (run) {
            try {
                /* This blocks when the queue is empty. */
                DatabaseInsertStatement insert = this.insertStatements.take();

                String tablePath = validateTable(insert.getTable(),
                        insert.getDatabse(), insert.getTableDefinition(),
                        insert.getTableCreation());

                if (tablePath != null) {

                    String statement = "INSERT INTO " + tablePath
                            + insert.getStatement();

                    if (insert.getParameters() == null) {
                        runQuery.update(dbConnection, statement);
                    } else {
                        runQuery.update(dbConnection, statement,
                                insert.getParameters());
                    }
                }
            } catch (SQLException e) {


            System.out.println("fail insert in DabaseInsertThread");

            //TODO: fix this to give a better error message
            //    log.warn("Could not execute the insert statement on the "
            //            + "database.", e);



                DbUtils.closeQuietly(dbConnection);
                dbConnection = null;
                connect();
            } catch (InterruptedException e) {/*don't care*/}
        }
    }

    /**
     * Checks to see if the table exists and matches the given table definition.
     * If the table does not exist it is created and validated. Tables are only
     * validated once per database connection. Tables changed during the
     * connection will not be re-evaluated and may cause errors. This also
     * goes for tables marked as invalid.
     *
     * @param table the name of the table.
     * @param database the name of the database.
     * @param tableDefinition the 'definition' of the table. A mapping of column
     *        names to column types that should exist in the table.
     * @param tableCreation the body of the SQL creation statement to use to
     *        create the table if it doesn't exist. Everything that comes after
     *        "CREATE TABLE IF NOT EXISTS database.table"
     * @return the table location as "database.table" if it was found to be
     *         valid. If the table or database is not valid {@code null} is
     *         returned.
     */
    private String validateTable(String table, String database,
            Map<String, String> tableDefinition, String tableCreation) {

        // The full path of the table
        String tablePath = database + "." + table;

        /* Only validate the table if it has not previously been evaluated. */
        if (invalidTables.contains(tablePath)) {
            return null;
        } else if (validTables.contains(tablePath)) {
            return tablePath;
        }

        try {
            /* Test if the database is valid. This is checked here for security
             * reasons since the database comes from user input and the create
             * table statement uses it directly. */
            String statement = "SELECT TABLE_SCHEMA FROM "
                    + "INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA = ?;";

            SchemaResultSet schemaResultSet = new SchemaResultSet();
            boolean isValidSchema = runQuery.query(dbConnection, statement,
                    schemaResultSet, database);

            if (isValidSchema) {

                runQuery.update(dbConnection,
                        "CREATE TABLE IF NOT EXISTS " + tablePath + " "
                        + tableCreation + ";");

                /* Test to see if the table is valid. */
                statement = "SELECT COLUMN_NAME, DATA_TYPE "
                        + "FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME  = ? "
                        + "AND TABLE_SCHEMA = ?;";

                SingleTableResultSet tableResultSet =
                        new SingleTableResultSet(tableDefinition);
                boolean isValidTable = runQuery.query(dbConnection, statement,
                        tableResultSet, table, database);

                if (isValidTable) {
                    validTables.add(tablePath);
                    return tablePath;
                } else {
                    //TODO fix this later os it actualy checks the table.
                    return tablePath;
                    //log.error("The table '{}' was found to be invalid.",
                    //    tablePath);
                    
                    //invalidTables.add(tablePath);
                    //return null;

                }
            } else {
                log.error("The schema '{}' was found to be invalid.",
                        database);

                invalidTables.add(tablePath);
                return null;
            }
        } catch (SQLException e) {
            log.warn("Failed trying to validate the table '{}'.", tablePath,
                    e);
            return null;
        }
    }

//    /**
//     * Try to create the tables specified in the array of tableSets if they
//     * don't exist.
//     */
//    private void makeTable(String table, String tableCreation) {
//
//        if (tables.contains(table)) {
//            return;
//        }
//
//        try {
//            runQuery.update(dbConnection,
//                    "CREATE TABLE IF NOT EXISTS " + table + " "
//                    + tableCreation + ";");
//
//            tables.add(table);
//        } catch (SQLException e) {
//            log.warn("Could not create the missing table '{}'", table, e);
//        }
//    }

    /**
     * Stop the thread after it completes its current loop.
     */
    public void stopRun() {
        run = false;
    }
}
