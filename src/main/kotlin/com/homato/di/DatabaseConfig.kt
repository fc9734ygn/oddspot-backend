package com.homato.di

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.homato.Database
import com.homato.POSTGRESQL_CONNECTION_STRING
import com.homato.POSTGRESQL_PW
import com.homato.POSTGRESQL_USER
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

private const val JDBC_URL_H2 = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
private const val DRIVER_CLASS_NAME_H2 = "org.h2.Driver"
private const val DRIVER_CLASS_NAME_POSTGRESQL = "org.postgresql.Driver"

fun connectToPostgresql(embedded: Boolean): Database {
    val dataSource: HikariDataSource = if (embedded) {
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = JDBC_URL_H2
            driverClassName = DRIVER_CLASS_NAME_H2
        })
    } else {
        val url = System.getenv(POSTGRESQL_CONNECTION_STRING)
        val user = System.getenv(POSTGRESQL_USER)
        val pass = System.getenv(POSTGRESQL_PW)

        HikariDataSource(HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            driverClassName = DRIVER_CLASS_NAME_POSTGRESQL
        })
    }
    val driver = dataSource.asJdbcDriver()
    Database.Schema.create(driver)
    return Database(driver)
}