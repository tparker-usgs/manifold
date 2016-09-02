package org.usgs.manifold.packet.data;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.usgs.manifold.ConfigurationPacket;
import org.usgs.manifold.Initialize;

import org.usgs.manifold.packet.earthworm.TraceBufferPacket;
import org.usgs.manifold.packet.earthworm.TraceBuffer2Packet;

/**
 *
 */
public class SendMulti {

private static ConfigurationPacket config = Initialize.getPacketConfig();

    // Prevent instantiation
    private SendMulti() {
    }

    /**
     * Write out the DataSingleSeismic to all connected earthworms.
     *
     * @param seismicData the packet to be sent out.
     * @throws IllegalStateException if the required variables have not been
     *         channelSetup.
     */
    public static synchronized void sendToEarthworm(
            DataMulti seismicData) {

        //check for nulls
        if(config.MULTI_STATION_MAP.get((seismicData.getStationNumber() * 10 + seismicData.getChannel()) ) == null ||
        config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel() ) == null ||
        config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel() ) == null ) {
            System.out.println("no translation for " + ((Integer)seismicData.getStationNumber() + "." + seismicData.getChannel()) );
            return;
        }

        //switched to use a tracebuffer2
        // Chris Lockett
        
        // Create trace buffer data to send out.
        //byte[] traceBufferData = new TraceBufferPacket(
        //        config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        seismicData.getNetworkID(),
        //        config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        seismicData.getStartTime(),
        //        seismicData.getEndTime(),
        //        seismicData.getSampleRate(),
        //        seismicData.getData()).getMessage();

         byte[] traceBufferData = new TraceBuffer2Packet(
                config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                seismicData.getNetworkID(),
                config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                seismicData.getStartTime(),
                seismicData.getEndTime(),
                seismicData.getSampleRate(),
                seismicData.getData()).getMessage();

        // Wrap the byte[] with a ChannelBuffer so it can be sent to a channel.
        ChannelBuffer output =
                ChannelBuffers.wrappedBuffer(traceBufferData);

        config.SEISMIC_EARTHWORM_CHANNELS.write(output);

        //TODO: Remove
        System.out.println("Seismic: time =" + seismicData.getStartTime()
            + ", station = " + config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel())
            + ", type = " + config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel())
            + ", location = " + config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()));
    }
}
