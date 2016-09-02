package org.usgs.manifold.packet.data;

import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.usgs.manifold.packet.DataPacket;
import org.usgs.manifold.packet.DefaultHeader;
import org.usgs.manifold.utilities.Time;

/**
 *
 */
public class DataRainGadge extends DataPacket{


    /** Timestamp in milliseconds since the 1970 epoch. */
    private final double timeStamp;

    /** The collected data. */
    private final int[] data;

    /** The number of unsigned shorts. */
    private final int DATA_BLOCKS = 1;

    {   // Set type name
        this.typeName = "RAIN";
    }

    /**
     * Read the scan data information from the given ChannelBuffer. This
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
    public DataRainGadge(String networkID, DefaultHeader header,
            ChannelBuffer buffer) throws CorruptedFrameException {

        try {
            this.networkID = networkID;
            this.header = header;

            // Get the timeString string.
            Charset ascii = Charset.forName("UTF-8");
            String timeString = buffer.readBytes(18).toString(ascii);

            // Convert the timeString into milliseconds since the 1970 epoch.
            timeStamp = Time.parsePacketTime(timeString);

            // Get the data.
            data = new int[DATA_BLOCKS];
            for (int i = 0; i < DATA_BLOCKS; i++) {
                data[i] = buffer.readUnsignedShort();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Not enough bytes to parse "
                    + this.typeName + " packet.");
        }
    }

    /** {@inheritDoc} */
    public void sendOut() {
        //TODO: Remove
        //System.out.println(this);
        SendRainGadge.sendToQueue(this);
    }

    /**
     * @return the time stamp in milliseconds since the 1970 epoch.
     */
    public double getTime() {
        return timeStamp;
    }

    /**
     * @return the collected data.
     */
    public int[] getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(header);
        builder.append("Timestamp         = ").append(timeStamp);
        builder.append("\n");

        // Pretty up the block of data
        for (int i = 0; i < data.length; i++) {
            builder.append(data[i]);
        }
        return builder.toString();
    }

}
