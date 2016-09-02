package org.usgs.manifold.packet.earthworm;

import java.nio.charset.Charset;

/**
 * Class to represent an earthworm heartbeat packet. These are used to make
 * the earthworm connection happy.
 *
 */
public class HeartBeatPacket extends EarthwormPacket {

    /** The "heartbeat" message that is sent. */
    private final String beatMessage;

    public HeartBeatPacket(String message) {
        super(EarthwormPacket.TYPE_HEARTBEAT);
        this.beatMessage = message;
    }

    /**
     * @return the bytes representing the beatMessage.
     */
    @Override
    public byte[] getMessageBody() {
        Charset ascii = Charset.forName("UTF-8");
        return beatMessage.getBytes(ascii);
    }
}
