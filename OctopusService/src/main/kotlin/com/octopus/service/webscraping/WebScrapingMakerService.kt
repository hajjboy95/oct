package com.octopus.service.webscraping

import com.octopus.service.JobMaker
import com.octopus.core.models.Job
import com.octopus.core.enums.JobType
import com.octopus.core.models.SubJob
import com.octopus.core.models.payloads.WebScrapingPayload
import com.octopus.dao.JobDao
import com.octopus.dao.SubJobDao
import org.slf4j.LoggerFactory

class WebScrapingMakerService(private val jobDao: JobDao, private val subJobDao: SubJobDao) : JobMaker {
    private val logger = LoggerFactory.getLogger(WebScrapingMakerService::class.java)

    override fun makeJob(accountId: Int, data: Any): Job {
        val job = Job(emptyList(), jobType, accountId)
        job.id = jobDao.addJob(job)
        val subJobs = createSubJobs(job, data)
        job.subJobs = subJobs
        logger.info("Created job with id (${job.id}) with ${job.subJobs.count()} SubJobs")
        return job
    }

    private fun createSubJobs(job: Job, data: Any): List<SubJob> {
        val urlsHashMap = data as HashMap<String, List<String>>
        val urlsSet = urlsHashMap["urls"]
        val scrapingTags = urlsHashMap.getOrElse("scrapingTags", { emptyList() })
        val subJobs = mutableListOf<SubJob>()
        logger.info("urlSet = ${urlsSet}}")
        var sequenceNum = 0
        if (urlsSet != null) {
            for (urls in urlsSet.chunked(4)) {
                val payload = WebScrapingPayload(urls, scrapingTags)
                val subJob = SubJob(job.uuid, payload, sequenceNum, jobType)
                subJobs.add(subJob)
                sequenceNum += 1
            }
        }
        // Batch add SubJobs and set generated ids to equivalent SubJobs
        subJobDao.addSubJobs(job.id, *subJobs.toTypedArray()).forEachIndexed { index: Int, id: Int -> subJobs[index].id = id }
        return subJobs
    }


    override val jobType = JobType.WEB_SCRAPING
}
