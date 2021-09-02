package com.octopus.service.video

import com.octopus.VIDEO_INPUT_FOLDER_ROOT
import com.octopus.core.enums.JobType
import com.octopus.core.models.Job
import com.octopus.core.models.SubJob
import com.octopus.core.models.payloads.VideoPayload
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.JobMaker
import com.octopus.utils.VideoSplitter
import com.octopus.utils.getFileExtension
import org.slf4j.LoggerFactory
import java.io.File
import javax.ws.rs.BadRequestException

class VideoMakerService(private val jobDao: JobDao, private val subJobDao: SubJobDao) : JobMaker {
    companion object {
        val SUBJOB_FOLDER_ROOT = "src/main/resources/videos/data/"
        val PART_SIZE_BYTES = 200_000
    }

    private val logger = LoggerFactory.getLogger(VideoMakerService::class.java)

    override val jobType = JobType.VIDEO_TRANSCODE

    /**
     * This is just a barely functioning prototype and needs to be reworked.
     */
    override fun makeJob(accountId: Int, data: Any): Job {
        val job = Job(emptyList(), jobType, accountId)
        job.id = jobDao.addJob(job)
        val subJobs = createSubJobs(job, data)
        job.subJobs = subJobs
        logger.info("Job (${job.uuid}) has ${job.subJobs.count()} SubJobs.")
        return job
    }

    private fun createSubJobs(job: Job, data: Any): List<SubJob> {
        val jobData = data as HashMap<String, String>
        val videoFile = getFile(jobData["file"])
        val sourceEncoding = getFileExtension(videoFile.path)
        val targetEncoding = jobData["targetEncoding"] ?: throw BadRequestException("A 'targetEncoding' must be provided.")

        // Create folder to store video file sections
        val jobFolder = SUBJOB_FOLDER_ROOT + job.uuid + "/"
        
        logger.info("Creating folder: $jobFolder")
        File(jobFolder).mkdirs()

        // Split the video into parts and store it in the folder
        logger.info("Splitting video: ${videoFile.path}")
        val subFilePaths = VideoSplitter.splitVideo(videoFile.path, jobFolder, PART_SIZE_BYTES)

        var seqNum = 1

        // Create a list of SubJobs from the filePaths after splitting a file
        val subJobs = subFilePaths.map {
            // TODO: Move the domain name to a config
            val videoLink = "http://localhost:8000/files/video/${job.uuid}/$seqNum.$sourceEncoding"
            val payload = VideoPayload(videoLink, sourceEncoding, targetEncoding)
            SubJob(job.uuid, payload, seqNum++, jobType)
        }

        // Add SubJobs to the DB
        subJobDao.addSubJobs(job.id, *subJobs.toTypedArray()).forEachIndexed {
            index: Int, id: Int -> subJobs[index].id = id
        }

        return subJobs
    }

    /**
     * Currently all files are stored on the Service and users need to send the fileName to encode.
     * This is a temporary solution. Later we may download the file from a provided ftp server / S3.
     */
    private fun getFile(fileName: String?) : File {
        if (fileName == null) {
            throw BadRequestException("The 'file' attribute must not be null.")
        }

        return File(VIDEO_INPUT_FOLDER_ROOT + fileName)
    }
}