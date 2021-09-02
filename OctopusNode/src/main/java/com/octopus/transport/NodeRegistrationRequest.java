package com.octopus.transport;

/**
 * Sent by the Node to register with the service.
 */
public class NodeRegistrationRequest {
    private final String macId;
    private final String accountUsername;

    public NodeRegistrationRequest(String accountUsername, String macId) {
        this.accountUsername = accountUsername;
        this.macId = macId;
    }

    public String getMacId() {
        return macId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }
}
