package org.usgs.manifold.packet.earthworm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents an Earthworm TraceBuffer (a data element relating to waveforms).
 * Used to communicate sensor web data to Earthworm devices that are connected
 * to this server, especially the database importer (ImportEW).
 */
public class TraceBufferPacket extends EarthwormPacket {

    private String station = "---";      //Represents source of data
    private String location = "--";
    private String channelName = "---";  //Represents type of data
    private int[] data;                  //Stores actual data
    private int pin;                     //Unused field of trace buffer
    private int numSamples;              //Size of data
    private double startTime;            //Time of first data point
    private double endTime;              //Time of last data point
    private double sampleRate;           //Sampling rate (in Hz)
    private static Map<Integer, String> stationMap; //Used to match stations
    // to id numbers
    public String network = "--";    		    //Network string
    private static boolean initialized = false;     //Initialize only once

    /**
     * Construct a default TraceBufferPacket with test data to check
     * transmission and message construction functionality.
     */
    public TraceBufferPacket() {
        super(EarthwormPacket.TYPE_TRACEBUF);
        numSamples = 3;
        endTime = System.currentTimeMillis() / 1000.;
        startTime = endTime - .16;
        sampleRate = 80;
        pin = 1;
        data = new int[3];
        data[0] = 50;
        data[1] = 0;
        data[2] = -50;
    }

    /**
     * Construct a TraceBufferPacket from actual data obtained from the sensor
     * web. This is the standard constructor for real (i.e. non-test)
     * trace buffer packets.
     *
     * @param stationNum  Source of this waveform
     * @param channelName Channel of this waveform
     * @param startTime Time of first sample point
     * @param endTime Time of last sample point
     * @param sampleRate Sampling frequency (in Hz)
     * @param data The data samples
     */
    public TraceBufferPacket(int stationNum, String networkID,
            String channelName, double startTime, double endTime,
            double sampleRate, int[] data) {
        super(EarthwormPacket.TYPE_TRACEBUF);

        //Construct global station table if necessary
        if (!initialized) {
            initialize();
        }
        //Set NetworkID from given networkID
        this.network = networkID;
        //Get or calculate station name
        station = getStation(stationNum);
        // Fill in fields of this packet from data provided
        numSamples = data.length;
        this.channelName = channelName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sampleRate = sampleRate;
        pin = 1;
        this.data = data;
    }

    public TraceBufferPacket(String stationName, String networkID, String Location,
            String type, double startTime, double endTime,
            double sampleRate, int[] data) {
        super(EarthwormPacket.TYPE_TRACEBUF);

        //Construct global station table if necessary
        if (!initialized) {
            initialize();
        }
        //Set NetworkID from given networkID
//        this.network = networkID;
        //Set station name
        this.station = stationName;
        //Set the type
        this.channelName = type;
        //Set the location
        this.location = Location;

        // Fill in fields of this packet from data provided
        numSamples = data.length;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sampleRate = sampleRate;
        pin = 1;
        this.data = data;
    }


    /**
     * Creates a global map of station names to ID numbers
     */
    public static void initialize() {
        stationMap = new HashMap<Integer, String>();
        initialized = true;

        //TODO: Read from configuration file which numbers should be associated
        //  with names and add them
     }

    /**
     * Retrieves the 3-letter string representing the station (source of the
     * waveform). If the station number has been assigned a name in the
     * station map, that will be returned, otherwise a default name of
     * N + stationNum will be returned.
     *
     * @param stationNum the id number of the waveform source
     * @return the appropriate 3-length String to represent the station
     */
    public static String getStation(int stationNum) {
        if (!initialized) {
            initialize();
        }
        if (!stationMap.containsKey(stationNum)) {
            return "N" + (stationNum < 10 ? "0" : "") + stationNum;
        }
        return stationMap.get(stationNum);
    }

    /**
     * Converts the fields and data contained in this packet into a flat byte
     * array for transmission via TCP. Creation of flags and tacking on the
     * appropriate header is handled by the superclass. All this method
     * returns is the acutal payload of data.
     *
     * @return the payload of this tracebuffer
     */
    @Override
    public byte[] getMessageBody() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            // Write initial information
            out.writeInt(pin);
            out.writeInt(numSamples);
            out.writeDouble(startTime);
            out.writeDouble(endTime);
            out.writeDouble(sampleRate);

            // Write SNC data
            int p = 7 - station.length();
            out.writeBytes(station);
            for (int i = 0; i < p; i++) {
                out.write((byte) 0);
            }
            p = 9 - network.length();
            out.writeBytes(network);
            for (int i = 0; i < p; i++) {
                out.write((byte) 0);
            }
            p = 9 - channelName.length();
            out.writeBytes(channelName);
            for (int i = 0; i < p; i++) {
                out.write((byte) 0);
            }

            // Write data format
            out.writeBytes("s4");   //four bytes in Sun byte order (big-endian)
            for (int i = 0; i < 5; i++) {
                out.write((byte) 0);
            }

            // Write actual data
            for (int i = 0; i < data.length; i++) {
                out.writeInt(data[i]);
            }

            for (int i = 0; i < 30; ++i) {
                out.write((byte) 0);
            }
            out.write((byte) 0);

            return baos.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the end time contained in this packet (for sanity checks)
     *
     * @return the end time as seen by this packet.
     */
    public double getEndTime() {
        return endTime;
    }
}
