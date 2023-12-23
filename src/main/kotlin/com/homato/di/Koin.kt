package com.homato.di


import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.homato.Database
import com.homato.JWT_SECRET_ENV
import com.homato.POSTGRESQL_PW
import com.homato.service.authentication.token.TokenConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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

private const val TOKEN_EXPIRATION_DURATION = 365L * 1000L * 60L * 60L * 24L // 1 year

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

fun Application.connectToPostgresql(embedded: Boolean): Database {
    val dataSource: HikariDataSource = if (embedded) {
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            driverClassName = "org.h2.Driver"
        })
    } else {
        val url = environment.config.property("database.url").getString()
        val user = environment.config.property("database.user").getString()
        val pass = System.getenv(POSTGRESQL_PW)

        HikariDataSource(HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            driverClassName = "org.postgresql.Driver"
        })
    }
    val driver = dataSource.asJdbcDriver()
    Database.Schema.create(driver)
    return Database(driver)
}

fun Application.createTokenConfig(): TokenConfig = TokenConfig(
    issuer = environment.config.property("jwt.issuer").getString(),
    audience = environment.config.property("jwt.audience").getString(),
    expiresIn = TOKEN_EXPIRATION_DURATION,
    secret = System.getenv(JWT_SECRET_ENV)
)
