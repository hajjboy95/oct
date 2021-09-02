package com.octopus.transport

import com.fasterxml.jackson.annotation.JsonProperty
import com.octopus.core.enums.JobType

class SubJobResult(
        @JsonProperty val id: Int? = null,
        @JsonProperty val result: Any? = null,
        @JsonProperty val subJobType: JobType? = null,
        @JsonProperty val parentUUID: String? = null,
        @JsonProperty val sequenceNumber: Int? = null
)
