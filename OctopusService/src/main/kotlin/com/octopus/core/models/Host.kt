package com.octopus.core.models

import com.fasterxml.jackson.annotation.JsonProperty

class Host(
        @JsonProperty val id: Int? = null,
        @JsonProperty val macId: String? = null,
        @JsonProperty val accountId: Int? = null)