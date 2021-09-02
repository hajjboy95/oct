package com.octopus.service

import com.octopus.core.models.ExecutableJob

import com.octopus.core.models.SubJob
import com.octopus.core.models.UserJob
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.node.NodeService
import com.octopus.transport.SubJobResult
import com.octopus.utils.PersistentMutableMap
import org.slf4j.Logger
import javax.websocket.Session

abstract class BaseExecutorService(
        private val logger: Logger,
        private val jobMaker: JobMaker,
        private val jobCollector: JobCollector,
        private val subJobDao: SubJobDao,
        private val jobDao: JobDao) : JobExecutor {

    private val subJobDataStore = "src/main/resources/store/subjob_data"
    private val persistentSubJobDataMap = PersistentMutableMap<String, Any>(subJobDataStore, false)

    override fun execute(executableJob: ExecutableJob): UserJob {
        val job = jobMaker.makeJob(executableJob.accountId!!, executableJob.data)

        // Add job to collector to start accept results
        jobCollector.addNewJob(job)
        for (subJob in job.subJobs) {
            // Update data map
            persistentSubJobDataMap.put(subJob.uuid, subJob.data)

            if (!sendSubJob(subJob)) {
                logger.warn("Unable to send SubJob ${subJob.id} with parentId - ${subJob.parentUUID}")
            }
        }
        // It is better to persist the map after every job instead of SubJob to reduce file writes
        persistentSubJobDataMap.persist()
        return jobDao.getByUUID(job.uuid)!!
    }

    override fun execute(subJob: SubJob): Boolean {
        val subJobData = persistentSubJobDataMap.get(subJob.uuid)
        if (subJobData == null) {
            logger.error("No data for SubJob ${subJob.id} found")
            return false
        }

        return sendSubJob(subJob.withData(subJobData))
    }

    private fun sendSubJob(subJob: SubJob): Boolean {
        checkNotNull(subJob.data)

        val node = NodeService.getNextNode()

        if (node == null) {
            logger.warn("No nodes found. Skipping SubJob (${subJob.uuid}) for job id ${subJob.parentUUID}")
            return false
        }

        logger.info("Sending SubJob ${subJob.id} for job ${subJob.parentUUID} / ${subJob.sequenceNumber} to node $node")
        subJobDao.addSubJobExecutionAndUpdateSubjob(subJob.id, node.host.id!!)
        node.send(subJob)
        return true
    }

    override fun join(subJobResult: SubJobResult, session: Session) {
        // TODO: We should probably have the SubJob UUID in the SubJobResult object
        val joinedSubJobUUID = jobCollector.join(subJobResult, session)
        if (joinedSubJobUUID != null) {
            persistentSubJobDataMap.remove(joinedSubJobUUID)
        }
    }
}
