package com.octopus.core.models.responses

import com.octopus.core.enums.JobType
import com.octopus.core.models.UserJob
import java.sql.Timestamp

class JobInfoResponse(
        val uuid: String,
        val accountId: Int,
        val jobType: JobType,
        val status: JobStatus,
        val createdAt: Timestamp,
        val startedAt: Timestamp?,
        val finishedAt: Timestamp?,
        val resultLink: String
) {
    enum class JobStatus { SCHEDULED, STARTED, FINISHED }

    companion object {
        @JvmStatic
        fun from(job: UserJob): JobInfoResponse {
            // TODO: Create this link in a config somewhere
            val resultLink = "http://localhost:8000/results/${job.uuid}"

            val status: JobStatus = when {
                job.finishedAt != null -> JobStatus.FINISHED
                job.startedAt != null -> JobStatus.STARTED
                else -> JobStatus.SCHEDULED
            }

            return JobInfoResponse(job.uuid, job.accountId, job.jobType, status, job.createdAt, job.startedAt, job.finishedAt, resultLink)
        }
    }
}