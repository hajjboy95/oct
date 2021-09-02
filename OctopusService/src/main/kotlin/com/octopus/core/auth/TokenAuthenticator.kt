package com.octopus.core.auth

import com.octopus.core.models.Account
import com.octopus.dao.AccountDao
import io.dropwizard.auth.AuthenticationException
import io.dropwizard.auth.Authenticator
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumer
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.HmacKey
import java.util.*


class TokenAuthenticator(private val accountDao: AccountDao, private val secret: ByteArray): Authenticator<String, User> {
    private val USER_ID_KEY = "USER_ID"

    private val jwtConsumer: JwtConsumer = JwtConsumerBuilder()
            .setAllowedClockSkewInSeconds(30)
            .setVerificationKey(HmacKey(secret))
            .setRelaxVerificationKeyValidation()
            .build()

    @Throws(AuthenticationException::class)
    override fun authenticate(token: String): Optional<User> {
        try {
            val jwtClaims = jwtConsumer.processToClaims(token)
            val userId = jwtClaims.getClaimValue(USER_ID_KEY) as Long
            val account: Account? = accountDao.findById(userId.toInt())

            if (account != null) {
                println("Authenticated ${account.username} :)")
                return Optional.of(User(account))
            }

            return Optional.empty()
        } catch (e: InvalidJwtException) {
            throw AuthenticationException(e)
        }
    }

    fun generateToken(user: User): String {
        val claims = JwtClaims()
        claims.setExpirationTimeMinutesInTheFuture(30F)
        claims.setClaim(USER_ID_KEY, user.account.id)

        val jws = JsonWebSignature()
        jws.payload = claims.toJson()
        jws.algorithmHeaderValue = AlgorithmIdentifiers.HMAC_SHA256
        jws.key = HmacKey(secret)

        return jws.compactSerialization
    }
}