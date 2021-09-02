package com.octopus.node.core;

import com.octopus.transport.SubJob;
import com.octopus.transport.SubJobResult;

import java.io.IOException;

public interface JobExecutor {
    SubJobResult execute(SubJob subJob);
    JobType getJobType();
}
