package com.octopus.node;

import com.octopus.node.connection.MessageHandlerHelper;
import com.octopus.node.connection.RegistrationService;
import com.octopus.node.connection.NodeClient;
import com.octopus.node.service.JobExecutorService;
import com.octopus.node.service.JobExecutors;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class OctopusNode {
    private final static Logger LOG = Logger.getLogger(OctopusNode.class);

    public static void main(String[] args) {
        try {
            final Properties config = OctopusNodeConfig.get();
            final String endpoint = config.getProperty(OctopusNodeConfig.ConfigKeys.WEBSOCKET_ENDPOINT);
            final String accountUsername = config.getProperty(OctopusNodeConfig.ConfigKeys.NODE_ACCOUNT);

            final JobExecutors allJobExecutors = new JobExecutors();
            final JobExecutorService jobExecutorService = new JobExecutorService(allJobExecutors);
            final RegistrationService registrationService = new RegistrationService(accountUsername);
            final MessageHandlerHelper messageHandlerHelper = new MessageHandlerHelper(jobExecutorService, registrationService);

            final NodeClient nodeClient = new NodeClient(new URI(endpoint), messageHandlerHelper);

            LOG.info("Starting the OctopusNode as: " + accountUsername);
            nodeClient.start();
            shutdownOnSignal(nodeClient);
        } catch (IOException | URISyntaxException e) {
            LOG.error("Failed to start client. Unable to read config.", e);
        }
    }

    /**
     * Adds a shutdown hook to shut down the {@link NodeClient} gracefully.
     * The shutdown hook is invoked in case the program receives a SIGTERM or SIGINT signal.
     *
     * @param nodeClient {@link NodeClient}
     */
    private static void shutdownOnSignal(NodeClient nodeClient) {
        Runtime.getRuntime().addShutdownHook(new Thread(nodeClient::stop));
    }
}
