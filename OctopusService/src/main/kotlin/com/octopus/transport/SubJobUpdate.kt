package com.octopus.transport

import com.fasterxml.jackson.annotation.JsonProperty
import com.octopus.core.enums.JobType

class SubJobUpdate(
        @JsonProperty val id: Int? = null,
        @JsonProperty val subjobUpdateType: SubJobUpdateType? = null,
        @JsonProperty val subjobType: JobType? = null
)