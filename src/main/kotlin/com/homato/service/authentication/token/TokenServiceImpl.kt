package com.homato.service.authentication.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton
import java.util.*

@Singleton(binds = [TokenService::class])
class TokenServiceImpl : TokenService {
    override fun generate(config: TokenConfig, vararg claims: TokenClaim): String {

        var token =  JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(Clock.System.now().toEpochMilliseconds() + config.expiresIn))

        claims.forEach { claim ->
            token = token.withClaim(claim.name, claim.value)
        }

        return token.sign(Algorithm.HMAC256(config.secret))
    }
}