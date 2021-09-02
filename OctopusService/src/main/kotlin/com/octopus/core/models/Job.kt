package com.octopus.core.models

import com.octopus.core.enums.JobType
import java.util.*

class Job(var subJobs: List<SubJob>,
          val jobType: JobType,
          var accountId: Int) {
    var id: Int = 0
    val uuid = UUID.randomUUID().toString()
}