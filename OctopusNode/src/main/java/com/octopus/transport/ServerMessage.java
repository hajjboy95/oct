package com.octopus.transport;

/**
 * General message sent by the server. This will be used for basic communication between the server and the node.
 */
public class ServerMessage {

    public enum MessageType {
        NOT_REGISTERED,
        REGISTERED
    }

    private final MessageType messageType;
    private final String message;

    public ServerMessage(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }
}
