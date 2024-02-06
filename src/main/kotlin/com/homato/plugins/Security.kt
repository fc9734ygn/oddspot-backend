package com.homato.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.homato.JWT_REALM
import com.homato.service.authentication.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.java.KoinJavaComponent.getKoin

fun Application.configureSecurity() {

    val config = getKoin().get<TokenConfig>()

    authentication {
        jwt {
            realm = System.getenv(JWT_REALM)
            verifier(
                JWT.require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(config.audience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
