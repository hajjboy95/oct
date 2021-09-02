package com.octopus.core.models

import com.fasterxml.jackson.annotation.JsonProperty

class Account(
        @JsonProperty val id: Int? = null,
        @JsonProperty val username: String? = null,
        @JsonProperty val email: String? = null,
        @JsonProperty val password: String? = null
)
