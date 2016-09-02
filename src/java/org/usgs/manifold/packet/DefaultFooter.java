package org.usgs.manifold.packet;

import java.nio.charset.Charset;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;

/**
 * Defines an ending delimeter that should always appear in every packet.
 */
public class DefaultFooter {

    /** The string representing the end of packet delimeter. */
    public static final String END_OF_PACKET = "EOP\r\n";

    /**
     * @return the end of packet delimeter.
     */
    public static ChannelBuffer getDelimeter() {
        Charset ascii = Charset.forName("UTF-8");
        return ChannelBuffers.copiedBuffer(END_OF_PACKET.getBytes(ascii));
    }

    /**
     * Compare the end of the delimeter to the contents in the frame. At this
     * point it is expected that the frame contains nothing but the ending
     * delimeter. This method does not modify the frame.
     *
     * @param buffer the ChannelBuffer that contains the ending delimeter.
     * @throws CorruptedFrameException if the ChannelBuffer does not match.
     */
    public static void checkDelimeter(ChannelBuffer buffer)
            throws CorruptedFrameException {

        // Get the ChannelBuffer for the ending delimeter.
        ChannelBuffer delimeter = getDelimeter();

        // Compare the contents.
        if (delimeter.compareTo(buffer) != 0) {
            throw new CorruptedFrameException("End of packet does not match "
                    + "the required end of packet string.");
        }
    }
}
