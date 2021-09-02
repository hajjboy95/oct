package com.octopus.resources

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/health")
class HealthResource {
    private val logger: Logger = LoggerFactory.getLogger(HealthResource::class.java)

    @GET
    fun checkHealth() : Response {
        logger.info("Health check called")
        return Response.ok("Healthy").build()
    }
}
