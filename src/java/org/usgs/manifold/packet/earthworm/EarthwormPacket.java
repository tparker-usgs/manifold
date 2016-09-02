package org.usgs.manifold.packet.earthworm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.usgs.manifold.ConfigurationPacket;
import org.usgs.manifold.Initialize;

/** Represents a packet in the Earthworm format. All packets start with
 *  the start flag (0x02), then the string sequence: " 99 1  " followed
 *  by the message type, as defined in Earthworm specs. Also note that
 *  the byte value 0x03 is flag for the end of the packet, and so must
 *  be escaped whenever it occurs in the course of data. This base
 *  class handles the byte stuffing, and adding the flags at the beginning
 *  and end as well as the packet header.
 *
 */
abstract public class EarthwormPacket {

    /** Copied from Valve source - Earthworm\gov.usgs.earthworm.MessageLogo */
    public static final int TYPE_WILDCARD = 0;			// wildcard value - DO NOT CHANGE!!!
    public static final int TYPE_ADBUF = 1;			// multiplexed waveforms from DOS adsend
    public static final int TYPE_ERROR = 2;			// error
    public static final int TYPE_HEARTBEAT = 3;			// heartbeat
    public static final int TYPE_NANOBUF = 5;			// single-channel waveforms from nanometrics
    public static final int TYPE_PICK2K = 10;			// P-wave arrival time (with 4 digit year)
    public static final int TYPE_CODA2K = 11;			// coda info (plus station code) from pick_ew
    //	public static final int TYPE_PICK2 = 12;		// P-wave arrival time from picker & pick_ew
    //	public static final int TYPE_CODA2 = 13;		// coda info from picker & pick_ew
    public static final int TYPE_HYP2000ARC = 14;		// hyp2000 (Y2K hypoinverse) event archive
    public static final int TYPE_H71SUM2K = 15;			// hypo71-format hypocenter summary msg (with 4-digit year) from eqproc/eqprelim
    //	public static final int TYPE_HINVARC = 17;		// hypoinverse event archive msg from eqproc/eqprelim
    //	public static final int TYPE_H71SUM = 18;		// hypo71-format summary msg from eqproc/eqprelim
    public static final int TYPE_TRACEBUF2 = 19;		// single-channel waveforms from NT adsend, getdst2, nano2trace, rcv_ew, import_ida...
    public static final int TYPE_TRACEBUF = 20;			// single-channel waveforms from NT adsend, getdst2, nano2trace, rcv_ew, import_ida...
    public static final int TYPE_LPTRIG = 21;			// single-channel long-period trigger from lptrig & evanstrig
    public static final int TYPE_CUBIC = 22;			// cubic-format summary msg from cubic_msg
    public static final int TYPE_CARLSTATRIG = 23;		// single-channel trigger from carlstatrig
    //	public static final int TYPE_TRIGLIST = 24;		// trigger-list msg (used by tracesave modules) from arc2trig, trg_assoc, carlsubtrig
    public static final int TYPE_TRIGLIST2K = 25;		// trigger-list msg (with 4-digit year) used  by tracesave modules from arc2trig, trg_assoc, carlsubtrig
    public static final int TYPE_TRACE_COMP_UA = 26;            // compressed waveforms from compress_UA
    public static final int TYPE_STRONGMOTION = 27;		// single-instrument peak accel, peak velocity, peak displacement, spectral acceleration
    public static final int TYPE_MAGNITUDE = 28;		// event magnitude: summary plus station info
    public static final int TYPE_STRONGMOTIONII = 29;
    private static final byte ESC = 27;
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    /* End of copied code */
    private byte[] messageHeader;

    /** Used to store the header bytes for this message*/
    /** Construct a new earthworm message of the appropriate type. Builds the
     * packet header for later use.
     *
     * @param msgType the type of message (as defined above)
     */
    public EarthwormPacket(int msgType) {
        //For now, limit it to the two types we expect
        if (msgType != TYPE_HEARTBEAT && msgType != TYPE_TRACEBUF &&
                msgType != TYPE_TRACEBUF2) {
            throw new RuntimeException("Unsupported Packet Type");
        }
        
        //get the configuration
        ConfigurationPacket config = Initialize.getPacketConfig();
        
        //Build header string
        //String s = " 26 211" + Integer.toString(msgType);

        String intstr = Integer.toString(config.SEISMIC_EARTHWORM_INTALLATION);
        
        //Make it the right length
        while (intstr.length() < 3) {
            intstr = " " + intstr;
        }
        
        String modstr = Integer.toString(config.SEISMIC_EARTHWORM_MODULE);
                
        //Make it the right length
        while (modstr.length() < 3) {
            modstr = " " + modstr;
        }
        
        String typstr = Integer.toString(msgType);
        
        //Make it the right length
        while (typstr.length() < 3) {
            typstr = " " + typstr;
        }
        
        String s = " " + intstr + modstr + typstr;
        
        //System.out.println("String s = {" + s + "}");
                
        //Make it the right length
        while (s.length() < 10) {
            s = s.concat(" ");
        }
        
        //System.out.println("String s = {" + s + "}");
        
        //Add STX flag
        messageHeader = s.getBytes();
        messageHeader[0] = STX;
        
    }

    /** Retrieves the bytes that make up the body of the message, between
     * the header and the ETX flag.
     *
     * @return the payload of the Earthworm packet
     */
    public abstract byte[] getMessageBody();

    /** Retrieves the byte sequence that makes up the entire message, stuffed
     * and flagged and ready for transmission.
     *
     * @return the complete Earthworm message as a byte array
     */
    public byte[] getMessage() {
        //Retrieve payload
        byte[] messageBody = getMessageBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            //Write the header into the buffer
            for (byte b : messageHeader) {
                //Escape as necesssary
                if (b == ETX || b == ESC) {
                    out.writeByte(ESC);
                }
                //Write actual byte
                out.writeByte(b);
            }

            //Write the payload into the buffer
            for (byte b : messageBody) {
                //Escape as necssary
                if (b == ETX || b == ESC || b == STX) {
                    out.writeByte(ESC);
                }

                //Write actual byte
                out.writeByte(b);
            }

            //Write end flag
            out.writeByte(ETX);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert the byte buffer to a byte array and return it
        return baos.toByteArray();
    }
}
