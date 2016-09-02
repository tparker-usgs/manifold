package org.usgs.manifold.packet;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;

/**
 * Defines a starting delimeter as well as standard header information that
 * should always appear in every packet.
 * <pre> <code>
 * Definition:
 * messageType   1 byte  unsigned byte
 * stationNumber 2 bytes unsigned short
 * </code> </pre>
 */
public class DefaultHeader {

    /** The string representing the start of packet delimeter. */
    public static final String START_OF_PACKET = "\r\nSOP";

    /** The type of message the following body of data represents. */
    private short messageType;

    /** The station number that this data came from. */
    private int stationNumber;

    /**
     * Reads the header information from the given ChannelBuffer. This method
     * modifies the ChannelBuffers reader index.
     *
     * @param frame the ChannelBuffer that contains the required header
     *        information at the current reader index.
     * @throws CorruptedFrameException if there are not enough bytes in the
     *         ChannelBuffer.
     */
    public DefaultHeader(ChannelBuffer frame) throws CorruptedFrameException {
        
        try {
            // Get the data
            messageType = frame.readUnsignedByte();
            stationNumber = frame.readUnsignedShort();

        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Not enough bytes to parse "
                    + "packet header.");
        }
    }

    /**
     * @return the message type of the packet.
     */
    public short getMessageType() {
        return messageType;
    }

    /**
     * @return the serial number of the packet.
     */
    public int getStationNumber() {
        return stationNumber;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("station number    = ").append(stationNumber);
        builder.append("\n");
        builder.append("message type      = ").append(messageType);
        builder.append("\n");

        return builder.toString();
    }

    /**
     * @return the start of packet delimeter.
     */
    public static ChannelBuffer getDelimeter() {
        Charset ascii = Charset.forName("UTF-8");
        return ChannelBuffers.copiedBuffer(START_OF_PACKET.getBytes(ascii));
    }

    /**
     * Compare the starting delimeter to the start of frame to see if it has the
     * right starting delimeter. This modifies the frame by removing the number
     * of bytes expected to be in the delimiter.
     *
     * @param frame the ChannelBuffer that contains the ending delimeter.
     * @throws CorruptedFrameException if the ChannelBuffer does not match.
     */
    public static void checkDelimeter(ChannelBuffer frame)
            throws CorruptedFrameException {

        try {

            ChannelBuffer delimeter = getDelimeter();
            ChannelBuffer start = frame.readSlice(START_OF_PACKET.length());

            if (delimeter.compareTo(start) != 0) {
                throw new CorruptedFrameException("Start of packet does not "
                        + "match the required start of packet string.");
            }

        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Not enough bytes to parse "
                    + "start of packet string.");
        }
    }
}
