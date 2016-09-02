package org.usgs.manifold.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code SimpleChannelHandler} that organizes connections into
 * {@code ChannelGroups}, writes {@code ChannelBuffer}s to connections, ignores
 * any incoming data from the connections and closes connections if an
 * {@code SQLException} is thrown.
 *
 */
public class CVOServerHandler extends SimpleChannelHandler {

    private static Logger log = LoggerFactory.getLogger(CVOServerHandler.class);

    /** A mapping of ports to channel groups. */
    private Map<Integer, ChannelGroup> portChannelMap;

    /**
     * @param portChannelMap Holds a mapping of ports to channel groups and is
     *        used to sort connections.
     */
    public CVOServerHandler(Map<Integer, ChannelGroup> portChannelMap) {
        this.portChannelMap = portChannelMap;
    }

    /**
     * Retrieves the local InetSocketAddress of the channel specified.
     *
     * @param channel the channel to retrieve the address from.
     * @return the local InetSocketAddress of the given channel.
     */
    public InetSocketAddress getLocalAddress(Channel channel) {
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * Retrieves the remote InetSocketAddress of the channel specified.
     *
     * @param channel the channel to retrieve the address from.
     * @return the remote InetSocketAddress of the given channel.
     */
    public InetSocketAddress getRemoteAddress(Channel channel) {
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sort the connected client into a channel group based on its port
     * connection.
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        int port = getLocalAddress(e.getChannel()).getPort();

        if (portChannelMap.containsKey(port)) {
            portChannelMap.get(port).add(e.getChannel());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ignore any messages received from the clients.
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * Write the data out to the client only if the buffer is not full.
     */
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) {
        Channel channel = e.getChannel();
        if (channel.isWritable()) {
            ctx.sendDownstream(e);
        } else {
            log.warn("Could not write to client '{}",
                    getRemoteAddress(channel));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Shut down the channel if there is an exception thrown.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            log.warn("Connection error: {}", cause.toString());
        } else {
            log.warn("Unexpected error.", e);
        }
        Channel ch = e.getChannel();
        ch.close();
    }
}
