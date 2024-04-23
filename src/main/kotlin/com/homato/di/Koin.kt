package com.homato.di


import com.homato.Database
import com.homato.service.authentication.token.TokenConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

@Module
@ComponentScan("com.homato")
class AppModule

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            configModule(
                database = connectToPostgresql(false),
                tokenConfig = createTokenConfig()
            ),
            AppModule().module
        )
    }
}

fun configModule(database: Database, tokenConfig: TokenConfig) = module {
    single { database }
    single { tokenConfig }
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}