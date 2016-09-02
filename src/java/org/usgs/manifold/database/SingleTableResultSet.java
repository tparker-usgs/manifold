package org.usgs.manifold.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Checks a table to see if it has all the required column names and types.
 *
 * <p>
 * Expects a result set with two columns. The first being a column name in the
 * table, the second being the type of said column.
 *
 */
public class SingleTableResultSet implements ResultSetHandler<Boolean> {

    /** Mapping of column names to column types that define the table. */
    private Map<String, String> tableDefinition;
    /** Column number which contains the column name from the table. */
    private final int columnName = 1;
    /** Column number which contains the column type from the table. */
    private final int columnType = 2;

    /**
     * @param tableDefinition the 'definition' of the table. A mapping of column
     *        names to column types that should exist in the table.
     */
    public SingleTableResultSet(Map<String, String> tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    public Boolean handle(ResultSet data) throws SQLException {

        // Keeps count of the number of good columns found in the table.
        int goodColumns = 0;

        while (data.next()) {

            String name = data.getString(columnName);
            String type = data.getString(columnType);

            /* If the given column name with it's associated type is found
             * in the table definition then add to the count of good
             * columns.*/
            if (tableDefinition.containsKey(name)
                    && tableDefinition.get(name).equalsIgnoreCase(type)) {
                goodColumns++;
            } else {
                return false;
            }
        }

        if (goodColumns == tableDefinition.size()) {
            return true;
        }

        return false;
    }
}
