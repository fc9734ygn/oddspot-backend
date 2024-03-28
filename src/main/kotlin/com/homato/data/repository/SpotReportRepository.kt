package com.homato.data.repository

import com.github.michaelbull.result.runCatching
import com.homato.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton

@Singleton
class SpotReportRepository(
    private val database: Database
) {

    suspend fun addSpotReport(
        spotId: Int,
        reporterId: String,
        reason: String
    ) = withContext(Dispatchers.IO) {
        runCatching {
            database.spotReportQueries.insert(
                spot_id = spotId,
                user_id = reporterId,
                report_time = Clock.System.now().toEpochMilliseconds(),
                reason = reason
            )
        }
    }

}