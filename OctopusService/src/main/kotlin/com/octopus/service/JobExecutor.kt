package com.octopus.service

import com.octopus.core.enums.JobType
import com.octopus.core.models.ExecutableJob
import com.octopus.core.models.SubJob
import com.octopus.core.models.UserJob
import com.octopus.transport.SubJobResult
import java.io.File
import javax.websocket.Session

interface JobExecutor {
    fun execute(executableJob: ExecutableJob) : UserJob
    fun execute(subJob: SubJob) : Boolean
    fun join(subJobResult: SubJobResult, session: Session)
    fun getResultFile(jobUUID: String): File
    val jobType: JobType
}
