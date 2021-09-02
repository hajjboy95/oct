package com.octopus.resources

import com.octopus.service.video.VideoMakerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Path("/files")
class FileResource {
    private val logger: Logger = LoggerFactory.getLogger(FileResource::class.java)

    @GET
    @Path("/video/{jobUUID}/{fileName}")
    fun getVideoFile(@PathParam("jobUUID") jobUUID: String, @PathParam("fileName") fileName: String) : Response {
        val filePath = VideoMakerService.SUBJOB_FOLDER_ROOT + jobUUID + "/" + fileName
        val videoFile = File(filePath)

        logger.info("Requested file: $filePath")

        if (!videoFile.exists()) {
            throw NotFoundException("Requested video file not found.")
        }

        logger.info("Found file. Sending...")

        return Response.ok(videoFile, MediaType.APPLICATION_OCTET_STREAM).build()
    }
}