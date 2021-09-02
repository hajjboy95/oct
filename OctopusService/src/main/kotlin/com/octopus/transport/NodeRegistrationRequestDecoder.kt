package com.octopus.transport

import com.fasterxml.jackson.databind.ObjectMapper
import javax.websocket.Decoder
import javax.websocket.EndpointConfig

class NodeRegistrationRequestDecoder : Decoder.Text<NodeRegistrationRequest> {
    companion object {
        private val mapper = ObjectMapper()
    }

    override fun willDecode(s: String): Boolean {
        return try {
            val nodeRegistrationRequest: NodeRegistrationRequest = mapper.readValue(s, NodeRegistrationRequest::class.java)
            nodeRegistrationRequest.macId != null
        } catch (e: Exception) {
            false
        }
    }

    override fun decode(s: String): NodeRegistrationRequest {
        return mapper.readValue(s, NodeRegistrationRequest::class.java)
    }

    override fun init(config: EndpointConfig) {}
    override fun destroy() {}
}
