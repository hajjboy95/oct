package com.octopus.service

import com.octopus.core.enums.JobType
import com.octopus.core.models.ExecutableJob
import com.octopus.core.models.SubJob
import com.octopus.core.models.UserJob

import com.octopus.transport.SubJobResult
import org.apache.log4j.Logger
import java.io.File
import javax.websocket.Session

class NullJobExecutor : JobExecutor {
    private val logger = Logger.getLogger(NullJobExecutor::class.java)
    override val jobType: JobType = JobType.NONE

    override fun execute(executableJob: ExecutableJob): UserJob {
        logger.warn("The null job executor was called.")
        return UserJob.EMPTY
    }

    override fun execute(subJob: SubJob): Boolean {
        logger.warn("The null job executor was called to send (${subJob.parentUUID}#${subJob.id}#${subJob.sequenceNumber})")
        return false
    }

    override fun join(subJobResult: SubJobResult, session: Session) {
        logger.warn("The null job executor was called to join (${subJobResult.parentUUID}#${subJobResult.id}#${subJobResult.sequenceNumber})")
    }

    override fun getResultFile(jobUUID: String): File {
        return File("src/main/resources/empty.txt")
    }
}