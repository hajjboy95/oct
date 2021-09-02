package com.octopus.transport

import com.fasterxml.jackson.databind.ObjectMapper
import javax.websocket.Decoder
import javax.websocket.EndpointConfig

class SubJobResultDecoder : Decoder.Text<SubJobResult> {
    companion object {
        private val mapper = ObjectMapper()
    }

    override fun willDecode(s: String): Boolean {
        return try {
            val subJobResult: SubJobResult = mapper.readValue(s, SubJobResult::class.java)
            subJobResult.result != null
        } catch (e: Exception) {
            false
        }
    }

    override fun decode(s: String): SubJobResult {
        return mapper.readValue(s, SubJobResult::class.java)
    }

    override fun init(config: EndpointConfig) {}
    override fun destroy() {}
}
