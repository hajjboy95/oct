package com.octopus.transport;

import com.octopus.node.core.JobType;

public class SubJob {
    private final int id;
    private final String parentUUID;
    private final Object data;
    private final int sequenceNumber;
    private final JobType subJobType;

    public SubJob(int id, String parentUUID, Object data, int sequenceNumber, JobType subJobType) {
        this.id = id;
        this.parentUUID = parentUUID;
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.subJobType = subJobType;
    }

    public int getId() {
        return id;
    }

    public String getParentUUID() {
        return parentUUID;
    }

    public Object getData() {
        return data;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public JobType getSubJobType() {
        return subJobType;
    }
}
