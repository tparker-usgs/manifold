package org.usgs.manifold.packet.data;

import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.usgs.manifold.packet.DataPacket;
import org.usgs.manifold.packet.DefaultHeader;
import org.usgs.manifold.utilities.Time;

/**
 * Defines single seismic wave form data.
 * <pre> <code>
 * Definition:
 * sampleRate   4 bytes float             number of samples per second
 * samples      2 bytes unsigned short    number of samples
 * time         18 bytes ascii string     start time in YYYYMMDDHHmmSS.sss
 * data         samples * 2 bytes         collected data
 * </code> </pre>
 *
 */
public class DataSingleSeismic extends DataPacket {

    /** Number of samples taken per second. */
    private final float sampleRate;

    /** Number of samples collected. */
    private final int samples;

    /** The start time, in seconds since the 1970 epoch, of when sample
    collection began. */
    private final double startTime;

    /** The time, in seconds since the 1970 epoch, at which the last sample was
    collected. */
    private final double endTime;
    
    /** Sampled data. */
    private final int[] data;

    {   // Set type name
        this.typeName = "EHZ";
    }

    /**
     * Read the seismic data information from the given ChannelBuffer. This
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
    public DataSingleSeismic(String networkID, DefaultHeader header,
            ChannelBuffer buffer) throws CorruptedFrameException {
        
        try {
            this.networkID = networkID;
            this.header = header;

            // Get the sample rate and the number of samples.
            sampleRate = buffer.readFloat();
            samples = buffer.readUnsignedShort();

            // Get the time string.
            Charset ascii = Charset.forName("UTF-8");
            String time = buffer.readBytes(18).toString(ascii);

            // Convert the time string into Seconds since the 1970 epoch.
            startTime = Time.parsePacketTime(time) / 1000l;

            //TODO: check that the end time is corect..
            // Use the start time, number of samples, and sample rate to
            // calculate the end time. The end time should be the time of the
            // last sample, not a time after the last sample was taken.
            Double diff = ((double) samples - 1) / (double) sampleRate;
            endTime = startTime + diff;

            // Get the data.
            data = new int[samples];
            for (int i = 0; i < data.length; i++) {
                //data[i] = buffer.readShort();

                data[i] = buffer.readUnsignedShort();
            }

        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Not enough bytes to parse "
                    + this.typeName + " packet.");
        }
    }

    public void sendOut() {
        //TODO: Remove
        //System.out.println(this);
        SendSingleSeismic.sendToEarthworm(this);
    }

    /**
     * @return the sample rate (samples/second).
     */
    public float getSampleRate() {
        return sampleRate;
    }

    /**
     * @return the number of samples collected.
     */
    public int getNumberOfSamples() {
        return samples;
    }

    /**
     * @return starting time, in seconds since the 1970 epoch, of the sample
     *         collection.
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * @return the time, in seconds since the 1970 epoch, at which the last
     *         sample was collected.
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * @return the collected samples.
     */
    public int[] getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append(header);
        builder.append("sample rate       = ").append(sampleRate);
        builder.append("\n");
        builder.append("start timestamp   = ").append(startTime);
        builder.append("\n");
        builder.append("end timestamp     = ").append(endTime);
        builder.append("\n");
        builder.append("number of samples = ").append(samples);
        builder.append("\n");
        
        // Pretty up the blocks of data
        for (int i = 0; i < data.length; i++) {
            builder.append("[").append(data[i]).append("]");
            if (i != samples - 1) {
                builder.append(",");
            }
            if (i % 10 == 0) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
