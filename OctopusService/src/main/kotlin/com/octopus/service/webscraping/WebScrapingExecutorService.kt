package com.octopus.service.webscraping

import com.octopus.core.enums.JobType
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import com.octopus.service.BaseExecutorService
import com.octopus.service.JobCollector
import com.octopus.service.JobMaker
import org.slf4j.LoggerFactory
import java.io.File

class WebScrapingExecutorService(
        jobMaker: JobMaker,
        private val jobCollector: JobCollector,
        subJobDao: SubJobDao,
        jobDao: JobDao
) : BaseExecutorService(
        LoggerFactory.getLogger(WebScrapingExecutorService::class.java),
        jobMaker,
        jobCollector,
        subJobDao,
        jobDao
) {
    override val jobType = JobType.WEB_SCRAPING

    override fun getResultFile(jobUUID: String): File {
        return File("${jobCollector.getResultFolderRoot()}/$jobUUID.json")
    }
}
