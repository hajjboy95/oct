package com.octopus.service

import com.octopus.core.models.Job
import com.octopus.core.enums.JobType
import com.octopus.core.models.SubJob

interface JobMaker {
    fun makeJob(accountId: Int, data: Any): Job
    val jobType: JobType
}