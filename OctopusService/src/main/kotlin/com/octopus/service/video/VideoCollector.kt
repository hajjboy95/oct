package com.octopus.service.video

import com.octopus.core.models.Job
import com.octopus.core.models.payloads.VideoPayload
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.JobCollector
import com.octopus.service.node.NodeService
import com.octopus.service.video.VideoMakerService.Companion.SUBJOB_FOLDER_ROOT
import com.octopus.transport.SubJobResult
import com.octopus.utils.PersistentMutableMap
import com.octopus.utils.VideoConcatenator
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import javax.websocket.Session

class VideoCollector(private val jobDao: JobDao, private val subJobDao: SubJobDao) : JobCollector {
    private val logger = LoggerFactory.getLogger(VideoCollector::class.java)
    private val resultFolderRoot = "src/main/resources/results/video"
    private val subJobResultFolderRoot = "src/main/resources/videos/out"
    private val jobResultsStore = "src/main/resources/store/video"
    private val persistentJobResults = PersistentMutableMap <String /*job UUID*/, JobInfo>(jobResultsStore, true)

    data class JobInfo(val subJobCount : Int, val targetEncoding : String) : Serializable

    override fun addNewJob(job: Job) {
        // Create a folder to collect the encoded video sent by Nodes
        val subJobResultDir = File("$subJobResultFolderRoot/${job.uuid}/")
        subJobResultDir.mkdirs()
        // Add some info on the job
        val targetEncoding = (job.subJobs[0].data as VideoPayload).targetEncoding
        persistentJobResults.put(job.uuid, JobInfo(job.subJobs.count(), targetEncoding))
    }

    override fun getResultFolderRoot() : String {
        return resultFolderRoot
    }

    override fun join(subJobResult: SubJobResult, session: Session) : String? {
        logger.info("Joining SubJob result #${subJobResult.sequenceNumber} for ${subJobResult.parentUUID}")

        if (subJobResult.parentUUID == null || subJobResult.sequenceNumber == null) {
            return null
        }

        if (!persistentJobResults.containsKey(subJobResult.parentUUID)) {
            logger.warn("Cannot find JobInfo in the persistent map for ${subJobResult.parentUUID}. Is this a duplicate SubJobResult?")
            return null
        }

        // Save video bytes to a file
        saveVideoToFile(subJobResult)

        // Update the DB with completed SubJob info
        val subJob = subJobDao.getSubJobByParentUUIDAndSequenceNumber(subJobResult.parentUUID, subJobResult.sequenceNumber)
        val host = NodeService.getHostBySession(session)
        // Update the time the SubJob completed at
        subJobDao.updateExecutionAndSubJobFinishedAt(subJob.id, host!!.id!!)

        if (isJobCompleted(subJobResult.parentUUID)) {
            logger.info("Job completed. Combining video result for job ${subJobResult.parentUUID}")
            submitJob(subJobResult.parentUUID)
            jobDao.updateFinishedAt(subJobResult.parentUUID)
            persistentJobResults.remove(subJobResult.parentUUID)
            // Delete folder with video parts as we now have the new video ready
            File("$subJobResultFolderRoot/${subJobResult.parentUUID}").delete()
            File("$SUBJOB_FOLDER_ROOT/${subJobResult.parentUUID}").delete()
        }

        return subJob.uuid
    }

    /**
     * For now the [result] is a [Base64] encoded byte array as a String.
     * Later, the [result] could be a link to download the file (if the Node uploads it to S3 for example)
     */
    private fun saveVideoToFile(subJobResult: SubJobResult) {
        // Get bytes from message
        val encodedBytes = subJobResult.result as String
        val bytes: ByteArray = Base64.getDecoder().decode(encodedBytes)

        // Create file
        val jobInfo = persistentJobResults.get(subJobResult.parentUUID!!) ?: throw RuntimeException("JobInfo for $subJobResult.parentUUID not found in persistent map!!")
        val jobResultFolderPath = "$subJobResultFolderRoot/${subJobResult.parentUUID}"
        val subJobResultFileName = "${subJobResult.sequenceNumber}.${jobInfo.targetEncoding}"

        val parentDir = File(jobResultFolderPath)
        val videoFile = File("$jobResultFolderPath/$subJobResultFileName")

        // If parent dir doesn't exists and we fail to create it, fail
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw RuntimeException("Failed to create the parent directory for ${subJobResult.parentUUID}/${subJobResult.sequenceNumber}")
        }

        // If video file already exists, this is probably a duplicate
        if (videoFile.exists()) {
            logger.warn("Result already exists for ${subJobResult.parentUUID}/${subJobResult.sequenceNumber}. Skipping")
            return
        }

        // Write bytes to file
        val fileOutputStream = FileOutputStream(videoFile)
        try {
            fileOutputStream.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream.close()
        }
    }

    private fun isJobCompleted(jobUUID: String): Boolean {
        val jobInfo = persistentJobResults.get(jobUUID) ?: throw RuntimeException("JobInfo for $jobUUID not found in persistent map!!")
        val subJobResultDir = File("$subJobResultFolderRoot/$jobUUID/")
        // We subtract 1 as walk() also gets the directory itself
        val completedSubJobCount = subJobResultDir.walk().maxDepth(1).count() - 1

        // This should never happen - we check just in case
        if (completedSubJobCount > jobInfo.subJobCount) {
            throw RuntimeException("More SubJob results submitted for job $jobUUID than total SubJobs (${jobInfo.subJobCount})")
        }

        return completedSubJobCount == jobInfo.subJobCount
    }

    private fun submitJob(jobUUID: String) {
        val jobInfo = persistentJobResults.get(jobUUID) ?: throw RuntimeException("JobInfo for $jobUUID not found in persistent map!!")
        VideoConcatenator.concatVideosInDir("$subJobResultFolderRoot/$jobUUID", "$resultFolderRoot/$jobUUID.${jobInfo.targetEncoding}")
    }
}