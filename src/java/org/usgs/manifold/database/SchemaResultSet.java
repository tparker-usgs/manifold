package org.usgs.manifold.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

public class SchemaResultSet implements ResultSetHandler<Boolean> {

    public Boolean handle(ResultSet data) throws SQLException {
        if (!data.next()) {
            return false;
        }
        return true;
    }
}
