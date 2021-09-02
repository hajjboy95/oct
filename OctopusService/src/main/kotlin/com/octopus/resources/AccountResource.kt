package com.octopus.resources

import com.octopus.core.models.Account
import com.octopus.dao.AccountDao
import io.dropwizard.auth.Auth
import org.slf4j.LoggerFactory
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/accounts")
class AccountResource(private val accountDao: AccountDao) {
    private val logger = LoggerFactory.getLogger(AccountResource::class.java)

    @GET
    // TODO: This will be used for testing only. Should be removed later.
    fun getAccounts(): Response {
        val accounts = accountDao.findAll()
        return Response.ok(accounts).build()
    }

    @PUT
    fun addAccount(account: Account): Response {
        logger.info("Adding account with username (${account.username}) to database...")
        val newId = accountDao.addAccount(account)
        val addedAccount = accountDao.findById(newId)
        return Response.ok(addedAccount).build()
    }

    @GET
    @Path("/account")
    fun getAccountById(@Auth accountId: Int): Response {
        val account = accountDao.findById(accountId)
        return Response.ok(account).build()
    }

    @GET
    @Path("/username/{username}")
    fun getAccountByUsername(@PathParam("username") username: String): Response {
        val account = accountDao.findByUsername(username)
        return Response.ok(account).build()
    }
}
