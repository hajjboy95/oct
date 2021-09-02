package com.octopus.transport

import java.lang.RuntimeException
import javax.websocket.Decoder
import javax.websocket.EndpointConfig
import kotlin.reflect.KClass

/**
 * Server web sockets can only use one decoder.
 * We use this decoder to decode different types of messages.
 */
class UniversalDecoder : Decoder.Text<Any> {
    companion object {
        // All new decoders should be added to this map
        private val decoders = mutableMapOf<KClass<*>, Decoder.Text<*>>(
                Pair(NodeRegistrationRequest::class, NodeRegistrationRequestDecoder()),
                Pair(SubJobResult::class, SubJobResultDecoder()),
                Pair(SubJobUpdate::class, SubJobStartedDecoder())
        )
    }

    // This is used to know which type the object is
    private var valueType: KClass<*>? = null

    override fun willDecode(s: String): Boolean {
        var canDecode = false

        decoders.forEach { entry ->
            run {
                val decoder = entry.value
                if (decoder.willDecode(s)) {
                    // This decoder works. Update the type of the object to know which decoder to use.
                    valueType = entry.key
                    canDecode = true
                    return@forEach
                }
            }
        }

        return canDecode
    }

    override fun decode(s: String): Any {
        val decodedObj = decoders[valueType]?.decode(s)
        if (decodedObj != null) return decodedObj else throw RuntimeException("Failed to decode object $s of type $valueType")
    }

    override fun init(config: EndpointConfig) {}
    override fun destroy() {}
}
