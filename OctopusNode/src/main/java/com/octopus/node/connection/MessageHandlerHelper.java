package com.octopus.node.connection;

import com.octopus.node.service.JobExecutorService;
import com.octopus.transport.ServerMessage;
import com.octopus.transport.SubJob;
import org.apache.log4j.Logger;

public class MessageHandlerHelper {
    private final static Logger LOG = Logger.getLogger(MessageHandlerHelper.class);

    public static CustomMessageHandler DEFAULT = (message) -> {
        LOG.error("Received message without a handler. Message type: " + message.getClass());
    };

    @FunctionalInterface
    public interface CustomMessageHandler<T> {
        void handle(T message);
    }

    private final JobExecutorService jobExecutorService;
    private final RegistrationService registrationService;
    private WebSocketClientEndpoint clientEndpoint;

    public MessageHandlerHelper(JobExecutorService jobExecutorService, RegistrationService registrationService) {
        this.jobExecutorService = jobExecutorService;
        this.registrationService = registrationService;
    }

    public void setupMessageHandlers(WebSocketClientEndpoint clientEndpoint) {
        this.clientEndpoint = clientEndpoint;

        // Add handler for SubJobs
        clientEndpoint.addMessageHandler(SubJob.class, jobExecutorService.getSubJobHandler(clientEndpoint));

        // Add handler for ServerMessages
        clientEndpoint.addMessageHandler(ServerMessage.class, getServerMessageHandler());
    }

    /**
     * Handles all {@link ServerMessage}s sent by the Server
     */
    private MessageHandlerHelper.CustomMessageHandler<ServerMessage> getServerMessageHandler() {
        return (serverMessage) -> {
            switch (serverMessage.getMessageType()) {
                case NOT_REGISTERED:
                    LOG.info("Node is not registered.");
                    registrationService.registerNode(clientEndpoint);
                    break;
                case REGISTERED:
                    LOG.info("Node is now registered.");
                    break;
            }
        };
    }
}
