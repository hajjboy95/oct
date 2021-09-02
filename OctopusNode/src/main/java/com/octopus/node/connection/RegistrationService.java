package com.octopus.node.connection;

import com.octopus.node.utils.HardwareUtils;
import com.octopus.transport.NodeRegistrationRequest;
import org.apache.log4j.Logger;

public class RegistrationService {
    private static Logger LOG = Logger.getLogger(RegistrationService.class);
    private final String nodeAccount;

    public RegistrationService(String nodeAccount) {
        this.nodeAccount = nodeAccount;
    }

    public void registerNode(WebSocketClientEndpoint clientEndpoint) {
        if (clientEndpoint == null) {
            throw new RuntimeException("Provided clientEndpoint was null.");
        }

        LOG.info("Sending node registration request.");
        clientEndpoint.sendMessage(new NodeRegistrationRequest(nodeAccount, HardwareUtils.getComputerIdentifier()));
    }
}
