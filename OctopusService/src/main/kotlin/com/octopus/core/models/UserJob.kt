package com.octopus.core.models

import com.octopus.core.enums.JobType
import java.sql.Timestamp

class UserJob(
        val id: Int,
        val uuid: String,
        val accountId: Int,
        val jobType: JobType,
        val createdAt: Timestamp,
        val updatedAt: Timestamp,
        val startedAt: Timestamp?,
        val finishedAt: Timestamp?
) {
    companion object {
        @JvmStatic
        val EMPTY = UserJob(0, "", 0, JobType.NONE, Timestamp(0), Timestamp(0), Timestamp(0), Timestamp(0))
    }
}