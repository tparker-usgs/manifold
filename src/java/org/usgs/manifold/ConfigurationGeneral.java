package org.usgs.manifold;

import java.io.IOException;
import java.util.List;

import org.usgs.ms.config.Configuration;
import org.usgs.ms.config.PropertiesReader;
import org.usgs.ms.config.ConfigStats;
import org.usgs.ms.config.converters.GeneralConverterSingle;
import org.usgs.ms.config.converters.GeneralConverterList;


public class ConfigurationGeneral extends Configuration {
    
    /** The default path to the configuration file. */
    public static final String DEFAULT_PATH = "/general.properties";

    /** Used to generate table names and for earthworm connections. */
    public final List<String> NETWORK_ID;
    /** The IP address of the MOXA to connect to. */
    public final List<String> IP_ADDRESS;
    /** The port to use for the MOXA connection */
    public final List<Integer> PORT;

    /** The driver to use to connect to the database. */
    public final String CONNECTION_DRIVER;
    /** The connection string to log into the database. */
    public final String CONNECTION_URL;

    public ConfigurationGeneral() throws IOException {
        this(DEFAULT_PATH);
    }

    public ConfigurationGeneral(String resourcePath) throws IOException {
        this.stats = new ConfigStats();
        PropertiesReader config = new PropertiesReader(stats, resourcePath);

        GeneralConverterSingle<String> stringConverter =
                new GeneralConverterSingle<String>(String.class);
        GeneralConverterList<String> stringListConverter =
                new GeneralConverterList<String>(String.class);
        GeneralConverterList<Integer> integerListConverter =
                new GeneralConverterList<Integer>(Integer.class);

        // Load network properties
        NETWORK_ID = config.getValue("network.id",
                PropertiesReader.REQUIRED, stringListConverter);
        IP_ADDRESS = config.getValue("network.ip",
                PropertiesReader.REQUIRED, stringListConverter);
        PORT = config.getValue("network.port",
                PropertiesReader.REQUIRED, integerListConverter);

        // Load database properties
        CONNECTION_DRIVER = config.getValue("connection.driver",
                PropertiesReader.REQUIRED, stringConverter);
        CONNECTION_URL = config.getValue("connection.url",
                PropertiesReader.REQUIRED, stringConverter);

        if(stats.getErrors().isEmpty()) {
            runTests();
        }
    }

    /**
     * Run extra tests.
     */
    private void runTests() {
        if (NETWORK_ID.size() != IP_ADDRESS.size()
                || NETWORK_ID.size() != PORT.size()) {

            stats.addError("The given number of network ID's do not "
                    + "match the given number of ip addresses and/or the "
                    + "given number of ports.");
        }
    }
}
