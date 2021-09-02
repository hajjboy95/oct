package com.octopus.transport

/**
 * General message sent by the server. This will be used for basic communication between the server and the node.
 */
class ServerMessage(val messageType: MessageType, val message: String) {
    enum class MessageType {
        NOT_REGISTERED,
        REGISTERED
    }
}
