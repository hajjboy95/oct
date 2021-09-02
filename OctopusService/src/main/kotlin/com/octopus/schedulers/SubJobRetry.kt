package com.octopus.schedulers

import com.octopus.core.models.SubJob
import com.octopus.dao.SubJobDao
import com.octopus.jobExecutorsKey
import com.octopus.service.JobExecutors
import com.octopus.service.node.NodeService
import com.octopus.subjobDaoKey
import org.knowm.sundial.Job
import org.knowm.sundial.SundialJobScheduler
import org.knowm.sundial.annotations.SimpleTrigger
import org.knowm.sundial.exceptions.JobInterruptException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@SimpleTrigger(repeatInterval = 15, timeUnit = TimeUnit.SECONDS) // TODO: update to minutes once we want to release to prod/staging
class SubJobRetry : Job() {
    private val logger: Logger = LoggerFactory.getLogger(SubJobRetry::class.java)

    @Throws(JobInterruptException::class)
    override fun doRun() {
        val subJobDao = SundialJobScheduler.getServletContext().getAttribute(subjobDaoKey) as SubJobDao
        val jobExecutors = SundialJobScheduler.getServletContext().getAttribute(jobExecutorsKey) as JobExecutors
        val retryLimit = 3 // later we can have a config entry map with key being jobtype - value being retry limit - no need now
        if (NodeService.hasNodes()) {
            val subJobs = subJobDao.findInProgressSubJobsOlderThanAndRetryable(1, retryLimit); // TODO: update to 15 mins when we want to release to prod
            if (subJobs.isEmpty()) {
                logger.info("No SubJobs will be retried")
            } else {
                logger.info("${subJobs.count()} subJobs are going to be retried")
                handleSubjobs(subJobs, jobExecutors, subJobDao);
            }
        } else {
            logger.warn("No node available - Run !!!")
        }
    }

    private fun handleSubjobs(subJobs: List<SubJob>, jobExecutors: JobExecutors, subJobDao: SubJobDao) {
        for (subJob in subJobs) {
            subJobDao.incrementRetries(subJob.id)
            jobExecutors.get(subJob.subJobType).execute(subJob)
        }
    }
}
