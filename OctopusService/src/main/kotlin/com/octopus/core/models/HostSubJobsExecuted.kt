package com.octopus.core.models

import com.octopus.core.enums.JobType
import com.octopus.core.enums.PaymentStatus

// TODO: Better Name
class HostSubJobsExecuted(val hostId: Int, val macId: String, val subJobId: Int, val jobType: JobType, val jobId: Int, val accountId: Int, val paymentStatus: PaymentStatus) {
    override fun toString(): String {
        return "HostSubJobsExecuted(hostId=$hostId, macId='$macId', subJobId=$subJobId, jobType=$jobType, jobId=$jobId, accountId=$accountId, paymentStatus=$paymentStatus)"
    }
}

