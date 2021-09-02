package com.octopus.service

import com.octopus.core.enums.JobType
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.video.VideoCollector
import com.octopus.service.video.VideoExecutorService
import com.octopus.service.video.VideoMakerService
import com.octopus.service.webscraping.WebScrapingCollector
import com.octopus.service.webscraping.WebScrapingExecutorService
import com.octopus.service.webscraping.WebScrapingMakerService
import java.util.*

class JobExecutors(jobDao: JobDao, subJobDao: SubJobDao) {
    private val executors: MutableMap<JobType, JobExecutor> = EnumMap(JobType::class.java)

    // To avoid creating a new NullJobExecutor each call
    companion object {
        private val nullJobExecutor = NullJobExecutor()
    }

    fun get(type: JobType): JobExecutor {
        return executors.getOrDefault(type, nullJobExecutor)
    }

    /**
     * All new [JobExecutor]s must be added here.
     */
    init {
        // Add WebScraping Executor
        executors[JobType.WEB_SCRAPING] = WebScrapingExecutorService(
                WebScrapingMakerService(jobDao, subJobDao),
                WebScrapingCollector(jobDao, subJobDao),
                subJobDao, jobDao
        )

        // Add Video Executor
        executors[JobType.VIDEO_TRANSCODE] = VideoExecutorService(
                VideoMakerService(jobDao, subJobDao),
                VideoCollector(jobDao, subJobDao),
                subJobDao, jobDao
        )
    }
}
