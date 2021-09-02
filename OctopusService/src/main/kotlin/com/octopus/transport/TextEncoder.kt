package com.octopus.transport

import com.fasterxml.jackson.databind.ObjectMapper
import javax.websocket.EncodeException
import javax.websocket.Encoder
import javax.websocket.EndpointConfig

open class TextEncoder<T> : Encoder.Text<T> {
    // Instantiated once per class
    companion object {
        private val mapper = ObjectMapper()
    }

    @Throws(EncodeException::class)
    override fun encode(obj: T): String {
        return mapper.writeValueAsString(obj)
    }

    override fun init(config: EndpointConfig) {}
    override fun destroy() {}
}