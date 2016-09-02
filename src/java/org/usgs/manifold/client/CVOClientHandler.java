package org.usgs.manifold.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.usgs.manifold.packet.PacketFactory;
import org.usgs.manifold.packet.DataPacket;

/**
 * A {@code SimpleChannelUpstreamHandler} that takes a frame of data, parsing
 * it into a {@link DataPacket} as specified in the {@link PacketFactory}. After
 * the data is parsed into a {@link DataPacket} it is then fanned out using the
 * {@link DataPacket}s sendOut() method.
 */
public class CVOClientHandler extends SimpleChannelUpstreamHandler
{
    private static Logger log = LoggerFactory.getLogger(CVOClientHandler.class);

    /** Used to generate table names and for earthworm connections. */
    private final String networkID;

    /**
     * @param networkID an ID which is used to generate table names and for
     *        earthworm connections.
     */
    public CVOClientHandler(String networkID) {
        this.networkID = networkID;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Create a {@link DataPacket} from the frame and then send out the.
     * data.
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    {
        // Check to see if it's a ChannelBuffer that was passed up.
        Object frame = e.getMessage();
        if (!(frame instanceof ChannelBuffer))
        {
            ctx.sendUpstream(e);
            return;
        }

        // Create the data packet.
        DataPacket data = PacketFactory.createPacket((ChannelBuffer)frame,
                networkID);

        // Send the data packet to where it needs to go.
        if (data != null)
        {
            data.sendOut();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Just log the exception.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    {
        Throwable cause = e.getCause();
        log.warn("Exception Caught: ", cause);
    }
}
