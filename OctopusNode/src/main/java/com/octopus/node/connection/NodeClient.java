package com.octopus.node.connection;

import org.apache.log4j.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class NodeClient {
    private final static Logger LOG = Logger.getLogger(NodeClient.class);
    private final List<Runnable> stoppables;
    private final java.util.concurrent.ExecutorService threadExecutor;
    private final MessageHandlerHelper messageHandlerHelper;
    private final URI uriEndpoint;

    public NodeClient(URI uriEndpoint, MessageHandlerHelper messageHandlerHelper) {
        this.uriEndpoint = uriEndpoint;
        this.messageHandlerHelper = messageHandlerHelper;
        stoppables = new ArrayList<>();
        threadExecutor = Executors.newSingleThreadExecutor();
        stoppables.add(threadExecutor::shutdown);
    }

    public void start() {
        threadExecutor.execute(() -> {
            try {
                LOG.info("Starting...");
                openSession();
            } catch (Exception e) {
                LOG.error("Could not open socket.", e);
                handleAbnormalSocketClose(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, ""));
            }
        });
    }

    public void stop() {
        LOG.info("Stopping node client...");
        stoppables.forEach(Runnable::run);
        LOG.info("Node stopped.");
    }

    public void handleAbnormalSocketClose(CloseReason reason) {
        if (CloseReason.CloseCodes.VIOLATED_POLICY.equals(reason.getCloseCode())) {
            LOG.error("Violated policy. Closing client. Reason: " + reason.getReasonPhrase());
            stop();
        }

        try {
            LOG.info("Attempting to reconnect to the service...");
            openSession();
        } catch (Exception e) {
            LOG.error("Failed to reconnect.", e);
            tryToSleep(1000);
            handleAbnormalSocketClose(new CloseReason(CloseReason.CloseCodes.NO_STATUS_CODE, ""));
        }
    }

    private void openSession() throws Exception {
        final WebSocketClientEndpoint clientEndPoint = new WebSocketClientEndpoint(this, uriEndpoint);
        messageHandlerHelper.setupMessageHandlers(clientEndPoint);

        // Close the socket before shutting down the threads
        stoppables.add(0, closeSession(clientEndPoint.session));
    }

    private Runnable closeSession(Session session) {
        return () -> {
            try {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                LOG.warn("Attempted to close already closed session.", e);
            }
        };
    }

    /**
     * Will attempt to sleep but won't throw any errors on failure.
     *
     * @param millis time to sleep in milliseconds
     */
    private void tryToSleep(long millis) {
        try {
            sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
