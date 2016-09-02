package org.usgs.manifold.packet.data;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.usgs.manifold.ConfigurationPacket;
import org.usgs.manifold.Initialize;

import org.usgs.manifold.packet.earthworm.TraceBufferPacket;

/**
 * Static methods to help with sending seismic data to where it needs to
 * go.
 *
 */
public class SendSingleSeismic {

    private static ConfigurationPacket config = Initialize.getPacketConfig();

    // Prevent instantiation
    private SendSingleSeismic() {
    }

    /**
     * Write out the DataSingleSeismic to all connected earthworms.
     *
     * @param seismicData the packet to be sent out.
     * @throws IllegalStateException if the required variables have not been
     *         channelSetup.
     */
    public static synchronized void sendToEarthworm(
            DataSingleSeismic seismicData) {

        // Create trace buffer data to send out.
        byte[] traceBufferData = new TraceBufferPacket(
                seismicData.getStationNumber(),
                seismicData.getNetworkID(),
                seismicData.getTypeName(),
                seismicData.getStartTime(),
                seismicData.getEndTime(),
                seismicData.getSampleRate(),
                seismicData.getData()).getMessage();

        // Wrap the byte[] with a ChannelBuffer so it can be sent to a channel.
        ChannelBuffer output =
                ChannelBuffers.wrappedBuffer(traceBufferData);

        config.SEISMIC_EARTHWORM_CHANNELS.write(output);

        //TODO: Remove
        System.out.println("Seismic: " +  seismicData.getNetworkID() + " " +
                            seismicData.getStationNumber());
        System.out.println( seismicData.getStartTime() + " to "
                            + seismicData.getEndTime());
    }
}