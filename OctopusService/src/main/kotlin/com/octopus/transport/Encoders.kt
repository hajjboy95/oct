package com.octopus.transport

import com.octopus.core.models.SubJob

/**
 * A wrapper around all encoders.
 * To add a new encoder, follow the pattern.
 */
class Encoders {
    class SubJobEncoder: TextEncoder<SubJob>()
    class ServerMessageEncoder: TextEncoder<ServerMessage>()
}
