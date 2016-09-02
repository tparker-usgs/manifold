package org.usgs.manifold.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.usgs.manifold.ConfigurationPacket;
import org.usgs.manifold.Initialize;
import org.usgs.manifold.packet.data.DataOneSecondScan;
import org.usgs.manifold.packet.data.DataGPS;
import org.usgs.manifold.packet.data.DataSingleSeismic;
import org.usgs.manifold.packet.data.DataRainGadge;
import org.usgs.manifold.packet.data.DataMulti;


public class PacketFactory
{
    private static Logger log =  LoggerFactory.getLogger(PacketFactory.class);
    private static ConfigurationPacket config = Initialize.getPacketConfig();

    // Prevent instantiation
    private PacketFactory() {}
    
    /**
     * Create a packet from the given {@link ChannelBuffer} frame.
     * 
     * @param frame A frame designating a packet.
     * @param networkID an ID which is used to generate table names and for
     *        earthworm connections.
     * @return the parsed packet.
     */
    public static DataPacket createPacket(ChannelBuffer frame, String networkID)
    {
        try
        {
            DefaultHeader.checkDelimeter(frame);

            // Read the standard header from the frame.
            DefaultHeader header = new DefaultHeader(frame);
            short messageType = header.getMessageType();

            // Get the data based on the message type.
            DataPacket data = null;
            if (messageType == config.SEISMIC_MESSAGE_TYPE) {
                data = new DataSingleSeismic(networkID, header, frame);
            } else if (messageType == config.GPS_MESSAGE_TYPE) {
                data = new DataGPS(networkID, header, frame);
            } else if (messageType == config.SCAN_MESSAGE_TYPE) {
                data = new DataOneSecondScan(networkID, header, frame);
            } else if (messageType == config.MULTI_MESSAGE_TYPE) {
                data = new DataMulti(networkID, header, frame);
            } else if (messageType == config.RAIN_MESSAGE_TYPE) {
                data = new DataRainGadge(networkID, header, frame);
            } else {
                log.warn("Unknown message type '{}'", messageType);
                return null;
            }

            if(frame.readableBytes() > 0) {                
                System.err.println("frame has no data");
                log.warn("'{}' packet contains extra data.", data.typeName);
                if(messageType != config.GPS_MESSAGE_TYPE) {
                    return null;
                }
            }
                       
            return data;
            
        } 
        catch (CorruptedFrameException e)
        {
            System.err.println("Corrupted Frame");
            e.printStackTrace();
            log.warn(e.toString());
            return null;
        }
    }
}
