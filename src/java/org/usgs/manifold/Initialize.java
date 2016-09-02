package org.usgs.manifold;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.dbutils.DbUtils;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.usgs.ms.config.Configuration;

import org.usgs.manifold.database.DatabaseInsertStatement;
import org.usgs.manifold.database.DatabaseInsertThread;
import org.usgs.manifold.client.CVOClient;

import org.usgs.manifold.packet.data.SendGPS;
import org.usgs.manifold.packet.data.SendOneSecondScan;
import org.usgs.manifold.packet.data.SendRainGadge;
import org.usgs.manifold.server.CVOServer;
import org.usgs.manifold.server.HeartbeatExportThread;

/**
 * Set up all the needed variables for the data types as well as starting the
 * client/server and other threads.
 *
 * <p>
 * Four steps to creating a new datatype.
 * </pre>
 * 1. Create Class: Data'Type'.java in org.usgs.ms.exportcvo.packet.data
 * This class should be used to parse and store the data from a given frame.
 *
 * 2. Create Class: Send'Type'.java in org.usgs.ms.exportcvo.packet.data
 * This class should be used to send out the data to where it needs to go.
 *
 * 3. Modify Class: PacketFactory.java in org.usgs.ms.exportcvo.packet
 * A simple if statement should be added telling the factory about the new data
 * type.
 *
 * 4. Modify Class: This Initialize class (if needed)
 * Set up variables required by the helper class to send out the data to where
 * it needs to go.
 * </pre>
 *
 */
public class Initialize {

    private static Logger log = LoggerFactory.getLogger(Initialize.class);

    private static ConfigurationGeneral generalConfig = null;
    private static ConfigurationPacket  packetConfig  = null;

    public static void main(String[] args) {

        // Load and check configurations
        try {
            generalConfig = new ConfigurationGeneral();
            packetConfig  = new ConfigurationPacket();

            checkConfig(getGeneralConfig());
            checkConfig(getPacketConfig());
            
        } catch (IOException e) {
            System.err.println("Error loading a configuration file.\n" + e);
            System.exit(-1);
        }

        // Create the heartbeat thread that keeps earthworm connections alive.
        HeartbeatExportThread heartbeat = new HeartbeatExportThread(
                packetConfig.SEISMIC_EARTHWORM_CHANNELS,
                packetConfig.SEISMIC_EARTHWORM_HEARTBEAT_TIME,
                packetConfig.SEISMIC_EARTHWORM_HEARTBEAT_MESSAGE);
        heartbeat.start();

        // Create the mapping of ports to channel groups.
        Map<Integer, ChannelGroup> portChannelMap = preparePortChannelMap();

        //Create the mapping of ports to interfaces
        Map<Integer, String> portInterfaceMap = preparePortInterfaceMap();
        
        // Specify the PortChannelMap to use
        SendGPS.setChannelMap(portChannelMap);
        if (loadDriver(getGeneralConfig().CONNECTION_DRIVER)) {

            // Queue to pass around DatabaseInsertStatements.
            final BlockingQueue<DatabaseInsertStatement> insertStatements =
                    new ArrayBlockingQueue<DatabaseInsertStatement>(100);

            // Specify the queue to use.
            SendGPS.setStatementQueue(insertStatements);
            SendOneSecondScan.setStatementQueue(insertStatements);
            SendRainGadge.setStatementQueue(insertStatements);

            // Create a thread to execute the SQL statments on the queue.
            DatabaseInsertThread databaseImport = new DatabaseInsertThread(
                    insertStatements, getGeneralConfig().CONNECTION_URL);

            databaseImport.start();
        }

        // Set the logger for Netty
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        // Start up the server to send out packets.
        CVOServer.startServer(portChannelMap, portInterfaceMap);

        // Start up the client connections to the moxa to get packets.
        for (int i = 0; i < getGeneralConfig().NETWORK_ID.size(); i++) {
          System.out.println("TOMP::: connecting to " + generalConfig.IP_ADDRESS.get(i) + ":" + generalConfig.PORT.get(i));
            CVOClient.startClient(generalConfig.IP_ADDRESS.get(i),
                    generalConfig.PORT.get(i), generalConfig.NETWORK_ID.get(i));
        }
    }

    
    private static Map<Integer, String> preparePortInterfaceMap() {
        final Map<Integer, String> interfacemap = new HashMap<Integer, String>();
        
        // Register earthworm interface if needed
        String earthwormInterface = getPacketConfig().SEISMIC_EARTHWORM_INTERFACE;
        if (earthwormInterface != null) 
          interfacemap.put(packetConfig.SEISMIC_EARTHWORM_PORT, earthwormInterface);
        
         // make pair ports with interfaces.
        for (Integer key : getPacketConfig().GPS_STATION_TO_PORT_MAP.keySet()) {
            interfacemap.put(getPacketConfig().GPS_STATION_TO_PORT_MAP.get(key),
                    getPacketConfig().GPS_STATION_TO_INTERFACE.get(key));
        }

        return Collections.unmodifiableMap(interfacemap);
        
    }
    
    
    /**
     * Prepares a mapping of ports to {@link ChannelGroup} objects so that
     * incoming connections to the server can organized into the groups.
     *
     * @return the prepared mapping of ports to channel groups.
     */
    private static Map<Integer, ChannelGroup> preparePortChannelMap() {

        final Map<Integer, ChannelGroup> portChannelMap =
                new HashMap<Integer, ChannelGroup>();

        // Register the earthworm port with a channel group.
        portChannelMap.put(packetConfig.SEISMIC_EARTHWORM_PORT,
                packetConfig.SEISMIC_EARTHWORM_CHANNELS);

        // Register the GPS ports with channel groups.
        for (Integer port : getPacketConfig().GPS_STATION_TO_PORT_MAP.values()) {
            portChannelMap.put(port, new DefaultChannelGroup());
        }

        return Collections.unmodifiableMap(portChannelMap);
    }

    /**
     * <p>Load a database driver so that any {@link DatabaseInsertThread}s that
     * depend on the driver for a connection will be able to connect.
     */
    private static boolean loadDriver(String driver) {
        if (!DbUtils.loadDriver(driver)) {
            log.error("Database driver '" + driver + "' could not be loaded. "
                    + "Any database output relying on the driver will fail.");
            return false;
        }
        return true;
    }

    /**
     * Creates a configuration and does basic error checking on it. Logs any
     * errors and calls System.exit() if there were any serious problems.
     *
     * @param config the loaded configuration to check.
     */
    private static void checkConfig(Configuration config) {

        boolean error = false;

        // Log errors and quit.
        if (config.getErrors().length() > 0) {
            log.error("Configuration errors:\n" + config.getErrors());
            error = true;
        }

        // It's not important to quit if there are configuration warnings.
        if (config.getWarnings().length() > 0) {
            log.warn("Configuration warnings:\n" + config.getWarnings());
        }

        // Additional configuration info.
        if (config.getInfo().length() > 0) {
            log.info("Configuration info:\n" + config.getInfo());
        }

        if (error) {
            System.exit(-1);
        }
    }

    /**
     * @return the general configuration
     */
    public static ConfigurationGeneral getGeneralConfig() {
        return generalConfig;
    }

    /**
     * @return the packet configuration
     */
    public static ConfigurationPacket getPacketConfig() {
        return packetConfig;
    }
}
