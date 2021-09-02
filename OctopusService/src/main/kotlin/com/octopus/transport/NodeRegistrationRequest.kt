package com.octopus.transport

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Sent by the Node to register with the service.
 */
class NodeRegistrationRequest(@JsonProperty val accountUsername: String? = null, @JsonProperty val macId: String? = null)
