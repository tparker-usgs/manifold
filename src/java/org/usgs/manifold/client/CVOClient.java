package org.usgs.manifold.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import org.usgs.manifold.packet.PacketFactory;
import org.usgs.manifold.packet.DataPacket;

/**
 * A client that connects to a server and reads in data parsing it into
 * {@link DataPacket}s as specified in the {@link PacketFactory}. After the data 
 * is parsed into {@link DataPacket}s it is then fanned out using the
 * {@link DataPacket}s sendOut() method.
 *
 */
public class CVOClient {

    /** Time in seconds before a reconnection should be attempted. */
    static final int RECONNECT_DELAY = 5;

    /** Time in seconds specifying that a read timeout exception should be fired
    if no data is read from the connection over the time period. */
    static final int READ_TIMEOUT = 30;

    /**
     * Start a CVOClient trying to connect to the given address on the given
     * port.
     *
     * @param ipAddress the ipAddress to connect to.
     * @param port the port to connect on.
     * @param networkID an ID which is used to generate table names and for
     *        earthworm connections.
     */
    public static void startClient(String ipAddress, int port,
            String networkID) {

        // Initialize the timer that schedules subsequent reconnection attempts.
        final Timer timer = new HashedWheelTimer();

        // Create a basic factory with thread pools.
        final ChannelFactory factory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        // Set up the client.
        final ClientBootstrap bootstrap = new ClientBootstrap(factory);

        // Set up the event pipeline factory for the MOXA connection.
        bootstrap.setPipelineFactory(
                new CVOClientPipelineFactory(timer, bootstrap, networkID));

        // The address to the MOXA.
        InetSocketAddress connectionAddress = new InetSocketAddress(
                ipAddress, port);

        // Keep the connection alive.
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        // Adress to connect to when bootstrap.connect() is called.
        bootstrap.setOption("remoteAddress", connectionAddress);

        // Attempt to connect the client to the MOXA.
        bootstrap.connect();
    }
}
