package org.usgs.manifold.utilities;
import java.util.Map;

import org.usgs.manifold.packet.DataPacket;

public class DatabaseHelper {

    /**
     * Create a table name by combining a networkID a station number and the
     * type of data.
     *
     * @param packet the
     * @return the name of the table.
     */
    public static String getTableName(DataPacket packet) {

        int stationNumber = packet.getStationNumber();

        if (stationNumber < 0) {
            throw new IllegalArgumentException("Station number can't be less "
                    + "than 0.");
        }

        return packet.getNetworkID() + "_n" + (stationNumber < 10 ? "0" : "")
                + stationNumber + packet.getTypeName();
    }

    public static String getTableName(DataPacket packet, Map<Integer, String> sm) {

        int stationNumber = packet.getStationNumber();

        if (stationNumber < 0) {
            throw new IllegalArgumentException("Station number can't be less "
                    + "than 0.");
        }

        return sm.get(packet.getStationNumber());
    }

}