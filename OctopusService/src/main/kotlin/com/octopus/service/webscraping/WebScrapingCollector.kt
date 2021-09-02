package com.octopus.service.webscraping

import com.octopus.core.models.Job
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.JobCollector
import com.octopus.service.node.NodeService
import com.octopus.transport.SubJobResult
import com.octopus.utils.PersistentMutableMap
import com.octopus.utils.prettyPrint
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.RuntimeException
import java.util.*
import javax.websocket.Session

class WebScrapingCollector(private val jobDao: JobDao, private val subJobDao: SubJobDao) : JobCollector {
    private val logger = LoggerFactory.getLogger(WebScrapingCollector::class.java)
    private val resultFolderRoot = "src/main/resources/results/web_scraping"
    private val jobResultsStore = "src/main/resources/store/web_scraping_collector"

    // Maps job id to list of results lists (since each SubJob is expected to return a list)
    // We use a persistent map here to avoid data loss in case the service restarts
    private val persistentJobResults = PersistentMutableMap<String, Array<List<HashMap<String, List<String>>>?>>(jobResultsStore, true)

    override fun getResultFolderRoot() : String {
        return resultFolderRoot
    }

    override fun addNewJob(job: Job) {
        // Add job to map with empty list of results
        persistentJobResults.put(job.uuid, arrayOfNulls(job.subJobs.size))
    }

    override fun join(subJobResult: SubJobResult, session: Session) : String? {
        // Check the SubJobResult is valid
        if (subJobResult.parentUUID == null || subJobResult.sequenceNumber == null) {
            throw RuntimeException("Received a malformed SubJobResult object: $subJobResult")
        }

        if (!persistentJobResults.containsKey(subJobResult.parentUUID)) {
            logger.warn("Collector has no Job entry for ${subJobResult.parentUUID}. Probably an old SubJob attempt succeeded after a retry.")
            return null
        }

        logger.info("Joining SubJobResult for #${subJobResult.sequenceNumber} for ${subJobResult.parentUUID}")

        val resultSet = subJobResult.result as List<HashMap<String, List<String>>>

        val subJob = subJobDao.getSubJobByParentUUIDAndSequenceNumber(subJobResult.parentUUID, subJobResult.sequenceNumber)
        val host = NodeService.getHostBySession(session)

        // Update the job result with this SubJob's result
        val collectedResultsForJob = persistentJobResults.get(subJobResult.parentUUID)!!
        collectedResultsForJob[subJobResult.sequenceNumber] = resultSet
        persistentJobResults.put(subJobResult.parentUUID, collectedResultsForJob)

        // Update the time the SubJob completed at
        subJobDao.updateExecutionAndSubJobFinishedAt(subJob.id, host!!.id!!)

        if (isJobCompleted(subJobResult.parentUUID)) {
            jobDao.updateFinishedAt(subJobResult.parentUUID)
            submitJob(subJobResult.parentUUID)
            // This is no longer needed. So we remove it from the map
            persistentJobResults.remove(subJobResult.parentUUID)
        }

        return subJob.uuid
    }

    private fun isJobCompleted(jobUUID: String): Boolean {
        val results = persistentJobResults.get(jobUUID)!!
        var completedJobCount = 0

        for (subResult in results) {
            if (subResult != null) {
                completedJobCount++
            }
        }

        return completedJobCount == persistentJobResults.get(jobUUID)!!.size
    }

    /**
     * For now writes the result to a file.
     * That file could be sent as the result in the future.
     */
    private fun submitJob(jobUUID: String) {
        val results = persistentJobResults.get(jobUUID)!!.flatMap { it!!.toList() }
        val filePath = "$resultFolderRoot/$jobUUID.json"
        val file = File(filePath)
        FileUtils.writeStringToFile(file, prettyPrint(results), "UTF-8")
        logger.info("New file created - $filePath")
    }
}