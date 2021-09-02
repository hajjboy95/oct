package com.octopus.node.service;

import com.octopus.node.core.JobType;

public class SubJobUpdate {
    final int id;
    final SubJobUpdateType subjobUpdateType;
    final JobType subjobType;
    public SubJobUpdate(final int id, final SubJobUpdateType subjobUpdateType, final JobType subjobType) {
        this.id = id;
        this.subjobUpdateType = subjobUpdateType;
        this.subjobType = subjobType;
    }

    public int getId() {
        return id;
    }

    public SubJobUpdateType getSubjobUpdateType() {
        return subjobUpdateType;
    }

    public JobType getSubjobType() {
        return subjobType;
    }
}
