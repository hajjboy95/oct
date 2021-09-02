package com.octopus.resources

import com.octopus.core.auth.TokenAuthenticator
import com.octopus.core.auth.User
import io.dropwizard.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Response

@Path("/auth")
class AuthResource(private val tokenAuthenticator: TokenAuthenticator) {
    private val logger: Logger = LoggerFactory.getLogger(AuthResource::class.java)

    @GET
    fun generateValidToken(@Auth user: User): Response {
        val token: String = tokenAuthenticator.generateToken(user)
        return Response.ok(token).build()
    }

    @GET
    @Path("/check")
    fun checkAuth(@Auth user: Optional<User>): Response {
        if (user.isPresent) {
            return Response.ok("${user.get().name} authenticated :)").build()
        }
        return Response.ok("Not authenticated :(").build()
    }
}