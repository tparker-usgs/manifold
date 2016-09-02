package org.usgs.manifold.packet.earthworm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents an Earthworm TraceBuffer (a data element relating to waveforms).
 * Used to communicate sensor web data to Earthworm devices that are connected
 *
 */
public class TraceBuffer2Packet extends EarthwormPacket {

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
    public TraceBuffer2Packet() {
        super(EarthwormPacket.TYPE_TRACEBUF2);
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
    public TraceBuffer2Packet(int stationNum, String networkID,
            String channelName, double startTime, double endTime,
            double sampleRate, int[] data) {
        super(EarthwormPacket.TYPE_TRACEBUF2);

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

    public TraceBuffer2Packet(String stationName, String networkID, String Location,
            String type, double startTime, double endTime,
            double sampleRate, int[] data) {
        super(EarthwormPacket.TYPE_TRACEBUF2);

        //Construct global station table if necessary
        if (!initialized) {
            initialize();
        }
        //Set NetworkID from given networkID
        this.network = networkID.toUpperCase();
        //Set station name
        this.station = stationName.toUpperCase();
        //Set the type
        this.channelName = type.toUpperCase();
        //Set the location
        this.location = Location.toUpperCase();

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

    
    private byte [] intTolittleEndian(int i) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        
        out.writeInt(i);        
        byte [] ba = baos.toByteArray();
        baos.reset();
        for(int j = ba.length -1; j >= 0; --j){
            out.write(ba[j]);
        }
        //System.out.println("size = " + baos.toByteArray().length);
        return baos.toByteArray();
    }

    private byte [] intToBigEndian(int i) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        
        out.writeInt(i);        
        byte [] ba = baos.toByteArray();
        baos.reset();
        for(int j = 0; j <= ba.length -1; ++j){
            out.write(ba[j]);
        }
        return baos.toByteArray();
    }
    
    
    private byte [] doubleTolittleEndian(double d) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeDouble(d);
        byte [] ba = baos.toByteArray();
        baos.reset();
        for(int j = ba.length -1; j >= 0; --j){
            out.write(ba[j]);
        }
        //System.out.println("size = " + baos.toByteArray().length);
        return baos.toByteArray();
    }
    
    private byte [] doubleToBigEndian(double d) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeDouble(d);
        byte [] ba = baos.toByteArray();
        baos.reset();
        for(int j = 0; j <= ba.length -1; ++j){
            out.write(ba[j]);
        }
        //System.out.println("size = " + baos.toByteArray().length);
        return baos.toByteArray();
    }
    
    /**
     * Converts the fields and data contained in this packet into a flat byte
     * array for transmission via TCP. Creation of flags and tacking on the
     * appropriate header is handled by the superclass. All this method
     * returns is the acutal payload of data.
     *
     * @return the payload of this tracebuffer2
     */
    @Override
    public byte[] getMessageBody() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            DataOutputStream out = new DataOutputStream(baos);
            
            // Write initial information
            //out.writeInt(pin);
            out.write(intTolittleEndian(pin));
            
            //out.writeInt(numSamples);
            out.write(intTolittleEndian(numSamples));
            
            //out.writeDouble(startTime);
            out.write(doubleTolittleEndian(startTime));
            
            //out.writeDouble(endTime);
            out.write(doubleTolittleEndian(endTime));
            
            //out.writeDouble(sampleRate);
            out.write(doubleTolittleEndian(sampleRate));

            // Write SNCL data
            //station field is 7 bytes chars long
            int len = 7 - station.length();
            out.writeBytes(station);
            for (int i = 0; i < len; i++) {
                out.write((byte) 0);
            }
            // network field is 9 bytes long
            len = 9 - network.length();
            out.writeBytes(network);
            for (int i = 0; i < len; i++) {
                out.write((byte) 0);
            }

            //channel field is 4 bytes long
            //changed the channel feild size from 9 to 4
            len = 4 - channelName.length();
            out.writeBytes(channelName);
            for (int i = 0; i < len; i++) {
                out.write((byte) 0);
            }

            //Chris Lockett
            //added location to make compatable with tracebuff2
            //location location field is 3 chars long
            len = 3 - location.length();            
            out.writeBytes(location);
            for(int i = 0; i < len; i++) {
                out.write((byte) 0);
            }

            //7-19-2013
            // changed version from 
            //out.write((byte)'0');
            //out.write((byte)'0');
            //to 
            
            //version
            out.write((byte)'2');
            out.write((byte)'0');            
            
            // Write data format (data type)
            //out.writeBytes("s4");   //four bytes in Sun byte order (big-endian)
            out.writeBytes("i4");   //four bytes in i byte order (little-endian)
            
            out.write((byte) 0);
            
            //7-19-2013 added quality and pad and changed version
            //quality
            out.write((byte)'0');
            out.write((byte)'0');
            //pad
            out.write((byte)0);
            out.write((byte)0);

            // Write actual data
            for (int i = 0; i < data.length; i++) {
                //out.writeInt(data[i]);
                out.write(intTolittleEndian(data[i]));
            }

            //I don't know why this was placed here but I caused an error in WWS
            //for (int i = 0; i < 30; ++i) {
            //    out.write((byte) 0);
            //}
            //out.write((byte) 0);

            // Return as byte array
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
