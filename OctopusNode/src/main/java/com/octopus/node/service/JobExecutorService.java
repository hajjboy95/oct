package com.octopus.node.service;

import com.octopus.node.connection.MessageHandlerHelper;
import com.octopus.node.connection.WebSocketClientEndpoint;
import com.octopus.transport.SubJobResult;
import com.octopus.transport.SubJob;
import org.apache.log4j.Logger;


public class JobExecutorService {
    private final static Logger LOG = Logger.getLogger(JobExecutorService.class);
    private final JobExecutors jobExecutors;

    public JobExecutorService(JobExecutors jobExecutors) {
        this.jobExecutors = jobExecutors;
    }

    public MessageHandlerHelper.CustomMessageHandler<SubJob> getSubJobHandler(WebSocketClientEndpoint client) {
        return (subJob) -> {
            LOG.info("Executing " + subJob.getSubJobType() + " SubJob: " + subJob.getParentUUID() + " / " + subJob.getSequenceNumber());
            client.sendMessage(new SubJobUpdate(subJob.getId(), SubJobUpdateType.STARTED, subJob.getSubJobType()));
            final SubJobResult result = execute(subJob);
            LOG.info("Sending result for SubJob with id (" + subJob.getId() + ") and type (" + subJob.getSubJobType() + ")");
            client.sendMessage(result);
        };
    }

    public SubJobResult execute(SubJob subJob) {
        return jobExecutors.get(subJob.getSubJobType()).execute(subJob);
    }
}
