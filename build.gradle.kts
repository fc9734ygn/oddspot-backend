val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val koin_version: String by project
val koin_ksp_version: String by project
val h2_version: String by project
val postgresql_version: String by project
val commons_codec_version: String by project

plugins {
    application
    kotlin("jvm") version "1.8.21"
    id("io.ktor.plugin") version "2.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
    id ("com.google.devtools.ksp") version "1.8.21-1.0.11"
    id("app.cash.sqldelight") version "2.0.0"
}

group = "com.homato"
version = "0.0.1"
application {
    mainClass.set("com.homato.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.homato")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0")
        }
    }
}

dependencies {

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-freemarker:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.3")
    implementation("io.ktor:ktor-server-core-jvm:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.3")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.3")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.3")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Db
    implementation("org.postgresql:postgresql:$postgresql_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("commons-codec:commons-codec:$commons_codec_version")
    implementation("app.cash.sqldelight:jdbc-driver:2.0.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Koin for Ktor
    implementation("io.insert-koin:koin-ktor:$koin_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.3")
    implementation("io.insert-koin:koin-annotations:$koin_ksp_version")
    ksp("io.insert-koin:koin-ksp-compiler:$koin_ksp_version")

    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    //Result/Either monad
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.18")

}

// Use code generated by KSP, used by Koin Annotations
sourceSets {
    main { java.srcDirs("${project.buildDir}/generated/ksp/main/kotlin") }
    test { java.srcDirs("${project.buildDir}/generated/ksp/test/kotlin") }
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "com.homato.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
