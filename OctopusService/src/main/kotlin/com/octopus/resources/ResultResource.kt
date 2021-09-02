package com.octopus.resources

import com.octopus.core.auth.User
import com.octopus.core.models.UserJob
import com.octopus.dao.JobDao
import com.octopus.service.JobExecutors
import com.octopus.utils.getFileName
import io.dropwizard.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Path("/results/{jobUUID}")
class ResultResource(private val jobDao: JobDao, private val jobExecutors: JobExecutors) {
    private val logger: Logger = LoggerFactory.getLogger(ResultResource::class.java)

    @GET
    fun getResultFile(@PathParam("jobUUID") jobUUID: String, @Auth user: User): Response {
        val job: UserJob = jobDao.getByUUID(jobUUID, user.getId()) ?: throw NotFoundException("Job not found.")

        if (job.finishedAt == null) {
            return Response.status(425).build()
        }

        val file = jobExecutors.get(job.jobType).getResultFile(jobUUID)
        val filePath = file.path.replace("\\", "/")
        val fileName = file.path.substring(filePath.lastIndexOf("/") + 1)

        if (!file.exists()) {
            // If the job finished, there should be a file
            // We check just in case, but this is a bad situation
            logger.warn("Result file for finished job not found: $fileName")
            throw NotFoundException("Job result not found.")
        }

        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "inline; filename=\"$fileName\"")
                .build()
    }
}
