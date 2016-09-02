package org.usgs.manifold.server;

import java.util.Map;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;

/**
 * A very simple {@code ChannelPipelineFactory} that sets up a single handler
 * for each client connection to the server.
 *
 */
public class CVOServerPipelineFactory implements ChannelPipelineFactory {

    /** Holds a mapping of ports to channel groups. */
    private Map<Integer, ChannelGroup> portChannelMap;

    /**
     * @param portChannelMap Holds a mapping of ports to channel groups and is 
     *        used to sort connections.
     */
    public CVOServerPipelineFactory(Map<Integer, ChannelGroup> portChannelMap) {
        this.portChannelMap = portChannelMap;
    }

    /**
     * Creates a very simple pipeline to sort connected clients into groups.
     * Any messages sent from the clients are ignored and data can be written to
     * the clients if their write buffer is not full.
     * 
     * @return the {@link ChannelPipeline} created.
     */
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("handler", new CVOServerHandler(portChannelMap));

        return pipeline;
    }
}
