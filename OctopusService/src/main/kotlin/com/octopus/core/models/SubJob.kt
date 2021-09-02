package com.octopus.core.models

import com.octopus.core.enums.JobType
import java.util.*

class SubJob(
        val parentUUID: String,
        var data: Any,
        var sequenceNumber: Int,
        var subJobType: JobType
) {
    var id: Int = 0
    var uuid: String = UUID.randomUUID().toString()
    var retries: Int = 0

    constructor(id: Int, uuid: String, parentUUID: String, data: Any, sequenceNumber: Int, subJobType: JobType, retries: Int):
            this(parentUUID, data, sequenceNumber, subJobType) {
        this.id = id
        this.uuid = uuid
        this.retries = retries
    }

    override fun toString(): String {
        return "SubJob(parentUUID='$parentUUID', data=$data, sequenceNumber=$sequenceNumber, subJobType=$subJobType, id=$id, uuid='$uuid')"
    }

    fun withData(data: Any) : SubJob {
        this.data = data
        return this
    }
}