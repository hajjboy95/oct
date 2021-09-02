package com.octopus.service.video

import com.octopus.core.enums.JobType
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.BaseExecutorService
import com.octopus.service.JobCollector
import com.octopus.service.JobMaker
import com.octopus.utils.VideoConcatenator.getAllChildPaths
import org.slf4j.LoggerFactory
import java.io.File

class VideoExecutorService(
        jobMaker: JobMaker,
        private val jobCollector: JobCollector,
        subJobDao: SubJobDao,
        jobDao: JobDao
) : BaseExecutorService(
        LoggerFactory.getLogger(VideoExecutorService::class.java),
        jobMaker,
        jobCollector,
        subJobDao,
        jobDao
) {
    override val jobType = JobType.VIDEO_TRANSCODE

    override fun getResultFile(jobUUID: String): File {
        val filePath = getAllChildPaths(jobCollector.getResultFolderRoot()).find { path -> path.contains(jobUUID) }
        return File(filePath)
    }
}
