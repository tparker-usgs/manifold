package org.usgs.manifold.database;

import java.util.Map;

/**
 * Object to hold values for generating an SQL insert statement and validating
 * the table.
 *
 * <pre> <code>
 * Example:
 *      Table: "Oregon"
 *   Database: "People"
 *  Statement: "(name,height) VALUES (?,?)"
 * Parameters: "John Doe", 1.82}
 * </code> </pre>
 *
 */
public class DatabaseInsertStatement {

    /** The SQL statement to execute. */
    private final String statement;

    /** Parameters used by the SQL statement. */
    private final Object[] parameters;

    /** the name of the table .*/
    private String table;

    /** the Schema the table is in. */
    private String database;
    
    /** The statement to use to create the tables if they don't exist. */
    private String tableCreation;

    /** Defines the columns that the table should contain. */
    private Map<String, String> tableDefinition;

    /**
     * @param table the name of the table to insert into.
     * @param database the schema the table belongs to.
     * @param tableDefinition a mapping of column names to column types which
     *        defines the basic structures of the table.
     * @param tableCreation the body of the SQL creation statement to use to
     *        create the table if it doesn't exist. Everything that comes after
     *        "CREATE TABLE IF NOT EXISTS database.table"
     * @param statement the body of an SQL insert statement. Everything that
     *        comes after "INSERT INTO database.table"
     * @param parameters the parameters that will used by the SQL insert
     *        statement. May be {@code (Object[])null} if no parameters are
     *        needed.
     */
    public DatabaseInsertStatement(String table, String database, 
            Map<String, String> tableDefinition, String tableCreation,
            String statement, Object... parameters) {

        this.table = table;
        this.database = database;
        this.tableDefinition = tableDefinition;
        this.tableCreation = tableCreation;
        this.statement = statement;
        this.parameters = parameters;
    }

    /**
     * @return the name of the table.
     */
    public String getTable() {
        return table;
    }

    /**
     * @return the schema the table belongs to.
     */
    public String getDatabse() {
        return database;
    }

    /**
     * @return the body of the SQL creation statement that should be used to
     *         create the table if it doesn't exist.
     */
    public Map<String, String> getTableDefinition() {
        return tableDefinition;
    }

    /**
     * @return the mapping of column names to column types that define the basic
     *         structure of the table.
     */
    public String getTableCreation() {
        return tableCreation;
    }

    /**
     * @return the body of the SQL insert statement. This should not be a valid
     *         SQL statement on its own.
     */
    public String getStatement() {
        return statement;
    }

    /**
     * @return parameters used by the SQL statement. May be null.
     */
    public Object[] getParameters() {
        return parameters;
    }
}
