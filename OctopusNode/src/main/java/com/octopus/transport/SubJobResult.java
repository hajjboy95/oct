package com.octopus.transport;

import com.octopus.node.core.JobType;

public class SubJobResult {
    private final int id;
    private final Object result;
    private final JobType subJobType;
    private final String parentUUID;
    private final int sequenceNumber;

    public SubJobResult(int id, Object result, JobType subJobType, String parentUUID, int sequenceNumber) {
        this.id = id;
        this.result = result;
        this.subJobType = subJobType;
        this.parentUUID = parentUUID;
        this.sequenceNumber = sequenceNumber;
    }

    public SubJobResult(Object result, SubJob subJob) {
        this.id = subJob.getId();
        this.result = result;
        this.subJobType = subJob.getSubJobType();
        this.parentUUID = subJob.getParentUUID();
        this.sequenceNumber = subJob.getSequenceNumber();
    }

    public int getId() {
        return id;
    }
    
    public Object getResult() {
        return result;
    }

    public JobType getSubJobType() {
        return subJobType;
    }

    public String getParentUUID() {
        return parentUUID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
