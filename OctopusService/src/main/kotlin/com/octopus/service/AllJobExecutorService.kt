package com.octopus.service

import com.octopus.core.models.ExecutableJob
import com.octopus.core.models.UserJob
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.node.NodeService
import com.octopus.transport.SubJobResult
import com.octopus.transport.SubJobUpdate
import com.octopus.transport.SubJobUpdateType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.websocket.Session

class AllJobExecutorService(private val jobExecutors: JobExecutors, private val subJobDao: SubJobDao, private val jobDao: JobDao) {
    private val logger: Logger = LoggerFactory.getLogger(AllJobExecutorService::class.java)

    fun execute(executableJob: ExecutableJob): UserJob {
        logger.info("Executing ${executableJob.jobType}")
        return jobExecutors.get(executableJob.jobType).execute(executableJob)
    }

    fun handleSubJobResult(subJobResult: SubJobResult, session: Session) {
        jobExecutors.get(subJobResult.subJobType!!).join(subJobResult, session)
    }

    fun handleSubJobStartedAt(session: Session, subJobUpdate: SubJobUpdate) {
        val host = NodeService.getHostBySession(session)
        when (subJobUpdate.subjobUpdateType) {
            SubJobUpdateType.STARTED -> {
                logger.info("Setting the start time for SubJob (${subJobUpdate.id}).")
                subJobDao.updateStartedAt(subJobUpdate.id!!, host!!.id!!)
                val jobId = jobDao.getJobIdBySubJobId(subJobUpdate.id)
                logger.info("Setting the start time for Job (${jobId}).")
                jobDao.updateJobStartedAtIfNotUpdated(jobId)
            }
        }
    }
}
