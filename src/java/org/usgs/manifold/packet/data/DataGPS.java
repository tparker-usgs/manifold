package org.usgs.manifold.packet.data;

import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.usgs.manifold.packet.DataPacket;
import org.usgs.manifold.packet.DefaultHeader;
import org.usgs.manifold.utilities.Time;

/**
 * <p>Defines the GPS data type.</p>
 * <pre> <code>
 * Definition:
 * timeStamp    18 bytes ascii string     start timeString in YYYYMMDDHHmmSS.sss
 * dataSize     2 bytes unsigned short    number of bytes in the data block
 * data         dataSize * bytes          collected data
 * </code> </pre>
 *
 */
public class DataGPS extends DataPacket {

    /** Timestamp in milliseconds since the 1970 epoch. */
    private final double timeStamp;

    /** Amount of data in bytes. */
    private final int dataSize;

    /** The collected data. */
    private final byte[] data;

    {   // Set type name
        this.typeName = "GPS";
    }

    /**
     * Read the GPS data information from the given ChannelBuffer. This
     * method modifies the ChannelBuffers reader index. The data must be located
     * at the current reader index of the ChannelBuffer.
     *
     * @param networkID an ID which is used to generate table names and for
     *        earthworm connections.
     * @param header the header for this packet.
     * @param buffer the ChannelBuffer that contains the required data.
     * @throws CorruptedFrameException if there are not enough bytes in the
     *         ChannelBuffer.
     */
    public DataGPS(String networkID, DefaultHeader header, ChannelBuffer buffer)
            throws CorruptedFrameException {
        
        try {
            this.networkID = networkID;
            this.header = header;

            // Get the timeString string.
            Charset ascii = Charset.forName("UTF-8");
            String timeString = buffer.readBytes(18).toString(ascii);

            // Convert the timeString into milliseconds since the 1970 epoch.
            timeStamp = Time.parsePacketTime(timeString);

            // Get the size of the data block
            dataSize = buffer.readUnsignedShort();

            // Get the data.
            data = new byte[dataSize];
            buffer.readBytes(data);

        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Not enough bytes to parse "
                    + this.typeName + " packet.");
        }
    }

    public void sendOut() {
        //TODO: Remove
        //System.out.println(this);
        SendGPS.sendToQueue(this);
        SendGPS.sendToChannel(this);
    }

    /**
     * @return the time stamp in milliseconds since the 1970 epoch.
     */
    public double getTime() {
        return timeStamp;
    }

    /**
     * @return the amount of data in bytes.
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * @return the collected data.
     */
    public byte[] getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(header);
        builder.append("Timestamp         = ").append(timeStamp);
        builder.append("\n");
        builder.append("Data Size         = ").append(dataSize);
        builder.append("\n");
        
        // Pretty up the block of data
        for (int i = 0; i < data.length; i++) {
            builder.append((char) data[i]);
            if (i % 50 == 0) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
