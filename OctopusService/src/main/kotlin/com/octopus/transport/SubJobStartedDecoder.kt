package com.octopus.transport

import com.fasterxml.jackson.databind.ObjectMapper
import javax.websocket.Decoder
import javax.websocket.EndpointConfig

class SubJobStartedDecoder : Decoder.Text<SubJobUpdate> {
    companion object {
        private val mapper = ObjectMapper()
    }

    override fun willDecode(s: String): Boolean {
        return try {
            val subjobUpdate: SubJobUpdate = mapper.readValue(s, SubJobUpdate::class.java)
            subjobUpdate.id != null
        } catch (e: Exception) {
            false
        }
    }

    override fun decode(s: String): SubJobUpdate {
        return mapper.readValue(s, SubJobUpdate::class.java)
    }

    override fun init(config: EndpointConfig) {}
    override fun destroy() {}
}
