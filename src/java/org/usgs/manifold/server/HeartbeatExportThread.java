package org.usgs.manifold.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.group.ChannelGroup;
import org.usgs.manifold.packet.earthworm.HeartBeatPacket;

/**
 * Thread to write a required "heartbeat" to a group of earthworm connections 
 * at specified intervals. This keeps the earthworm connection alive and allows
 * the importer on the other end to detect when this exporter has crashed / lost
 * its link.
 *
 */
public class HeartbeatExportThread extends Thread {

    /** ChannelGroup that holds earthworm connections. */
    private final ChannelGroup earthWormChannels;

    /** Amount of time delay, in milliseconds, between heartbeats. */
    private final int heartbeatTimeDelay;

    /** The heartbeat message. */
    private final ChannelBuffer message;

    /** Specifies if the thread should be stopped. */
    private boolean run = true;

    /**
     * The heartbeat thread is used to send required "heartbeats" to all of 
     * the earthworm connections connected to the server in order to keep them
     * alive.
     *
     * @param earthWormChannels a {@code ChannelGroup} that holds all the
     *        earthworm connections.
     * @param heartbeatTimeDelay the delay in milliseconds between each
     *        "heartbeat."
     */
    public HeartbeatExportThread(ChannelGroup earthWormChannels,
            int heartbeatTimeDelay, String beatMessage) {

        super("HeartbeatExportThread");
        this.earthWormChannels = earthWormChannels;
        this.heartbeatTimeDelay = heartbeatTimeDelay;

        // Generate heartbeat message
        HeartBeatPacket heartbeat = new HeartBeatPacket(beatMessage);
        this.message = ChannelBuffers.wrappedBuffer(heartbeat.getMessage());
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        while (run) {
            // Write heartbeat message to all earthworm channels.
            earthWormChannels.write(message);
            try {
                //Sleep until we need to write again.
                Thread.sleep(heartbeatTimeDelay);
            } catch (InterruptedException e) {/*Don't care*/
            }
        }
    }

    /**
     * Stop the thread.
     */
    public void stopRun() {
        run = false;
    }
}
