package com.octopus.core.auth

import com.octopus.core.models.Account
import com.octopus.dao.AccountDao
import io.dropwizard.auth.AuthenticationException
import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.basic.BasicCredentials
import java.util.Optional

class BasicAuthenticator(private val accountDao: AccountDao): Authenticator<BasicCredentials, User> {
    @Throws(AuthenticationException::class)
    override fun authenticate(credentials: BasicCredentials): Optional<User> {
        val account: Account? = accountDao.findByUsername(credentials.username)

        if (account == null || account.password != credentials.password) {
            return Optional.empty()
        }

        return Optional.of(User(account))
    }
}