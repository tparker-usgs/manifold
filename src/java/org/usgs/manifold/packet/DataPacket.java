package org.usgs.manifold.packet;

/**
 * Defines a basic packet structure for data.
 */
public abstract class DataPacket {

    /** The default packet header. */
    protected DefaultHeader header;

    /** The networkID of where the packet came from. */
    protected String networkID;

    /** The {@link String} representation of the type of the packet. */
    protected String typeName;

    /**
     * @return the default packet header.
     */
    public DefaultHeader getHeader() {
        return header;
    }

    /**
     * @return the the station number this data came from.
     */
    public int getStationNumber() {
        return header.getStationNumber();
    }

    /**
     * @return the networkID of where the packet came from.
     */
    public String getNetworkID() {
        return networkID;
    }

    /**
     * @return the {@link String} representation of the type of the packet.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Send the data to where it needs to go.
     */
    abstract public void sendOut();
}
