package com.octopus.core.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.octopus.core.enums.JobType

data class ExecutableJob(
        @JsonProperty("jobType") val jobType: JobType,
        @JsonProperty("data") val data: Any) {
    var accountId: Int? = null
}