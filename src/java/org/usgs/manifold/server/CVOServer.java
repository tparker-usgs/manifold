package org.usgs.manifold.server;

import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * A simple server that organizes connections into {@code channelGroup}s based
 * on the port the connection connected on.
 *
 */
public class CVOServer {

    public static void startServer(Map<Integer, ChannelGroup> portChannelMap, 
            Map<Integer, String> portInterfaceMap) {

        // The ChannelFactory handles I/O requests and sets up channels. The
        // factory acquires threads from the given thread pools.
        ChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        // Create a server.
        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        // Set up the event pipeline factory. A new pipeline is created for
        // each connection.
        bootstrap.setPipelineFactory(
                new CVOServerPipelineFactory(portChannelMap));

        // Keep connections alive.
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        // Bind ports to the server
        for (Integer port : portChannelMap.keySet()) {

            if (port != null) {
                if(portInterfaceMap.get(port) != null) {
                    bootstrap.bind(new InetSocketAddress(portInterfaceMap.get(port),port));
               } else {                
                    bootstrap.bind(new InetSocketAddress(port));
               }
            }

        }

    }
}
