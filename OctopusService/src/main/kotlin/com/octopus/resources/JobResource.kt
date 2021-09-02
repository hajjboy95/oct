package com.octopus.resources

import com.octopus.VIDEO_INPUT_FOLDER_ROOT
import com.octopus.core.auth.User
import com.octopus.core.enums.JobType
import com.octopus.core.models.ExecutableJob
import com.octopus.core.models.UserJob
import com.octopus.core.models.responses.JobInfoResponse
import com.octopus.dao.JobDao
import com.octopus.service.AllJobExecutorService
import io.dropwizard.auth.Auth
import org.apache.commons.io.FileUtils
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataParam
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/jobs")
class JobResource(private val jobDao: JobDao, private val allJobExecutorService: AllJobExecutorService) {
    private val logger: Logger = LoggerFactory.getLogger(JobResource::class.java)

    @GET
    fun getAllForAccount(@Auth user: User): Response {
        val userJobs = jobDao.getAllForAccount(user.getId())
        val userJobInfoList = userJobs.map { JobInfoResponse.from(it) }
        return Response.ok(userJobInfoList).build()
    }

    @POST
    fun addNewJob(executableJob: ExecutableJob, @Auth user: User): Response {
        logger.info("Received executableJob of type ${executableJob.jobType}")
        executableJob.accountId = user.getId()
        val job = allJobExecutorService.execute(executableJob)
        return Response.ok(JobInfoResponse.from(job)).build()
    }

    @GET
    @Path("/{jobUUID}")
    fun getJob(@PathParam("jobUUID") jobUUID: String, @Auth user: User): Response {
        // Although UUIDs are unique, the account id is passed to make sure that account owns the job
        val job: UserJob = jobDao.getByUUID(jobUUID, user.getId()) ?: throw NotFoundException("Job not found")
        return Response.ok(JobInfoResponse.from(job)).build()
    }

    @POST
    @Path("/video-upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun uploadVideo(@FormDataParam("file") videoData: InputStream,
                    @FormDataParam("file") fileDetail: FormDataContentDisposition,
                    @FormDataParam("toFormat") toFormat: String, @Auth user: User): Response {
        logger.info("Received Video with name - ${fileDetail.fileName} to encode it to $toFormat")
        val folderPath = VIDEO_INPUT_FOLDER_ROOT
        val videoPath = folderPath + fileDetail.fileName
        FileUtils.copyInputStreamToFile(videoData, File(videoPath))
        logger.info("Saved Video to $videoPath")
        val data = hashMapOf(
                "file" to fileDetail.fileName,
                "targetEncoding" to toFormat) // TODO: need to sanatise these fields later
        val executableJob = ExecutableJob(JobType.VIDEO_TRANSCODE, data)
        executableJob.accountId = user.getId()
        // should make this call async
        val job = allJobExecutorService.execute(executableJob)
        return Response.ok(JobInfoResponse.from(job)).build()
    }
}
