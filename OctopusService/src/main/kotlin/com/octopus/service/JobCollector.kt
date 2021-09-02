package com.octopus.service

import com.octopus.core.models.Job
import com.octopus.transport.SubJobResult
import javax.websocket.Session

interface JobCollector {
    fun addNewJob(job: Job)
    fun join(subJobResult: SubJobResult, session: Session) : String?
    fun getResultFolderRoot() : String
}