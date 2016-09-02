package org.usgs.manifold.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This task attempts to connect to the "remoteAddress" specified in the
 * bootstrap options.
 *
 */
public class ReconnectTimerTask implements TimerTask {

    private static Logger log =
            LoggerFactory.getLogger(ReconnectTimerTask.class);

    /** Initialized bootstrap for the client connection. */
    private ClientBootstrap bootstrap;
    
    /** The remote address that the client is connecting to */
    private String remoteAddress;

    /**
     * @param bootstrap the ClientBootstrap that will be used to connect.
     * @param remoteAddress used only for message output.
     */
    public ReconnectTimerTask(ClientBootstrap bootstrap, String remoteAddress) {
        this.bootstrap = bootstrap;
        this.remoteAddress = remoteAddress;
    }

    /** {@inheritDoc} */
    public void run(Timeout timeout) {
        log.info("Reconnecting to '{}'", remoteAddress);
        bootstrap.connect();
    }
}
