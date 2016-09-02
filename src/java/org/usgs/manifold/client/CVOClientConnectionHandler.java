package org.usgs.manifold.client;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code SimpleChannelUpstreamHandler} that handles connection attempts and
 * errors from the client when making a connection to the server.
 *
 */
public class CVOClientConnectionHandler extends SimpleChannelUpstreamHandler {

    private static Logger log =
            LoggerFactory.getLogger(CVOClientConnectionHandler.class);

    /** Initialized bootstrap for the client connection. */
    private final ClientBootstrap bootstrap;

    /** A timer used to trigger the reconnection task. */
    private final Timer timer;

    /** A timer task used for reconnection attempts. */
    private final TimerTask reconnectTask;

    /** A buffer factory that creates buffers in little endian byte order. */
    private final ChannelBufferFactory bufferFactory =
            new HeapChannelBufferFactory(ByteOrder.LITTLE_ENDIAN);

    /**
     * @param bootstrap the bootstrap used for the connection.
     * @param timer the timer object to be used for delaying reconnection
     *        attempts.
     */
    public CVOClientConnectionHandler(ClientBootstrap bootstrap, Timer timer,
            int maxConnectionAttempts) {

        this.bootstrap = bootstrap;
        this.timer = timer;
        reconnectTask = new ReconnectTimerTask(bootstrap, getRemoteAddress());
    }

    /**
     * @return the remote address that this client is connecting to.
     */
    private String getRemoteAddress() {
        return ((InetSocketAddress) bootstrap.getOption("remoteAddress")).toString();
    }

    /** {@inheritDoc} */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e) {
        log.warn("Disconnected from '{}'", getRemoteAddress());
    }

    /** {@inheritDoc} */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        log.warn("Connection failed: reconnecting in {} seconds.",
                CVOClient.RECONNECT_DELAY);

        // Set the time ticking for the reconnection timer task.
        timer.newTimeout(reconnectTask, CVOClient.RECONNECT_DELAY,
                TimeUnit.SECONDS);
    }

    /** {@inheritDoc} */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Set up the buffer factory for the channel
        ctx.getChannel().getConfig().setBufferFactory(bufferFactory);

        log.info("Connected to '{}'", getRemoteAddress());
    }

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Throwable cause = e.getCause();
        if (cause instanceof ConnectException) {
            log.warn("Connection error: " + cause);
            ctx.getChannel().close();
        } else if (cause instanceof ReadTimeoutException) {
            log.warn("No data received from the connection in {} seconds.",
                    CVOClient.READ_TIMEOUT);

            if (ctx.getChannel().isConnected()) {
                ctx.getChannel().close();
            }
        } else {
            // Pass the exception further up the chain.
            Channels.fireExceptionCaught(ctx, cause);
        }
    }
}
