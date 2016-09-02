package org.usgs.manifold.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.Timer;
import org.usgs.manifold.packet.DataPacket;
import org.usgs.manifold.packet.DefaultFooter;

/**
 * A {@code ChannelPipelineFactory} that sets up a client server connection.
 * The factory specifies a {@code ReadTimeoutHandler} to trigger if no data has
 * been read for a time interval and a {@link CVOClientHandler} to manage any
 * reconnection attempts.
 *
 * <p>
 * A {@code DelimiterBasedFrameDecoder} is used to parse the data into frames
 * which are sent to the {@link CVOClientHandler} to be parsed into
 * {@link DataPacket}s.
 *
 */
public class CVOClientPipelineFactory implements ChannelPipelineFactory {

    /** A timer used to trigger a reconnection attempt. */
    private final Timer timer;

    /** Initialized bootstrap for the client connection. */
    private final ClientBootstrap bootstrap;

    /** Used to generate table names and for earthworm connections. */
    private final String networkID;

    /** The max size for any given frame of data. This is set absurdly high.*/
    //private final int MAX_FRAME_SIZE = 2048;

    /** Not high enuff for 1000 S/s or higher -CGL */
    private final int MAX_FRAME_SIZE =  9216;//2048;

    /**
     * @param timer a Timer Object used to time reconnection attempts.
     * @param bootstrap the ClientBootsrap to use for reconnection attempts.
     * @param networkID an ID which is used to generate table names and for
     *        earthworm connections.
     */
    public CVOClientPipelineFactory(Timer timer, ClientBootstrap bootstrap,
            String networkID) {

        this.timer = timer;
        this.bootstrap = bootstrap;
        this.networkID = networkID;
    }

    /**
     * Set up a pipeline to be used by a channel.
     *
     * @return the {@link ChannelPipeline} that was created.
     */
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        // Throws a ReadTimeoutException if no data can be read from the
        // connection in the specified time.
        pipeline.addLast("timeOutHandler",
                new ReadTimeoutHandler(timer, CVOClient.READ_TIMEOUT));

        // Handle the connect and reconect attempts.
        pipeline.addLast("connectionHandler",
                new CVOClientConnectionHandler(bootstrap, timer, 2));

        // Split the stream into frames using the header as a delimeter. The
        // first frame should always be blank.
        pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(
                MAX_FRAME_SIZE, DefaultFooter.getDelimeter()));

        // Parse the frames into packets and send them out.
        pipeline.addLast("mainHandler", new CVOClientHandler(networkID));

        return pipeline;
    }
}
