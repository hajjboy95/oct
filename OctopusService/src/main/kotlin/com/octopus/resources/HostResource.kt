package com.octopus.resources

import com.octopus.core.auth.User
import com.octopus.dao.HostDao
import io.dropwizard.auth.Auth
import org.slf4j.LoggerFactory
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/hosts")
class HostResource(private val hostDao: HostDao) {
    private val logger = LoggerFactory.getLogger(HostResource::class.java)

    @GET
    fun getHosts(@Auth user: User): Response {
        logger.info("Find All Hosts")
        val hosts = hostDao.findAllForAccount(user.getId())
        return Response.ok(hosts).build()
    }

    @GET
    @Path("/{macId}")
    fun findByMacId(@PathParam("macId") macId: String): Response {
        logger.info("Find host by macId $macId")
        val host = hostDao.findByMacId(macId)
        return Response.ok(host).build()
    }

    @DELETE
    @Path("/{macId}")
    fun removeByMacId(@PathParam("macId") macId: String, @Auth user: User): Response {
        logger.info("Removing host: $macId")
        val host = hostDao.findByMacId(macId)
        if (host == null || user.getId() != host.accountId) {
            throw NotFoundException("Host not found.")
        }

        hostDao.remove(macId)
        return Response.ok().build()
    }
}
