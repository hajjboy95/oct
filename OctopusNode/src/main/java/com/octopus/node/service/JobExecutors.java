package com.octopus.node.service;

import com.octopus.node.core.JobExecutor;
import com.octopus.node.core.JobType;
import com.octopus.transport.SubJobResult;
import com.octopus.transport.SubJob;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class JobExecutors {
    private final Map<JobType, JobExecutor> executors = new HashMap<>();

    // Used for executors not yet implemented to avoid a null pointer exception
    private static class NullJobExecutor implements JobExecutor {
        private final Logger LOG = Logger.getLogger(NullJobExecutor.class);

        @Override
        public SubJobResult execute(SubJob subJob) {
            LOG.warn("The null job executor was called.");
            return new SubJobResult(0,null, JobType.NONE, "", 0);
        }

        @Override
        public JobType getJobType() {
            return JobType.NONE;
        }
    }

    /**
     * All new {@link JobExecutor}s must be added here.
     */
    public JobExecutors() {
        executors.put(JobType.WEB_SCRAPING, new WebScrapingJobExecutor());
        executors.put(JobType.VIDEO_TRANSCODE, new VideoJobExecutor());
    }

    public JobExecutor get(JobType type) {
        return executors.getOrDefault(type, new NullJobExecutor());
    }
}
