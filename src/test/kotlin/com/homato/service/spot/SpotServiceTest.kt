package com.homato.service.spot

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.homato.data.model.Spot
import com.homato.data.model.SpotWithVisits
import com.homato.data.model.Visit
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.repository.FileRepository
import com.homato.data.repository.SpotReportRepository
import com.homato.data.repository.SpotRepository
import com.homato.data.repository.VisitRepository
import com.homato.util.Environment
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

class SpotServiceTest {
    private lateinit var service: SpotService
    private val fileRepository: FileRepository = mockk(relaxed = true)
    private val spotRepository: SpotRepository = mockk(relaxed = true)
    private val visitRepository: VisitRepository = mockk(relaxed = true)
    private val spotReportRepository: SpotReportRepository = mockk(relaxed = true)
    private val environment: Environment = mockk(relaxed = true)

    private val filePath = "path/to/image.jpg"
    private val contentType = ContentType.Image.JPEG
    private val creatorId = "user123"
    private val imageUrl = "http://example.com/uploaded.jpg"
    private val spot: Spot = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        service = SpotService(fileRepository, spotRepository, spotReportRepository, visitRepository, environment)
    }

    @Test
    fun `submitSpot successfully submits a spot`() = runTest {
        every { environment.getVariable(any()) } returns "bucket123"
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any()) } returns Ok(imageUrl)
        coEvery { spotRepository.saveSpot(any(), any(), any(), any(), any(), any(), any(), any()) } returns Ok(Unit)

        val result = service.submitSpot(filePath, createSpotData(), creatorId, contentType)
        assertTrue(result is Ok)
    }

    @Test
    fun `submitSpot fails when image upload fails`() = runTest {
        every { environment.getVariable(any()) } returns "bucket123"
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any()) } returns Err(Throwable("Upload failed"))

        val result = service.submitSpot(filePath, createSpotData(), creatorId, contentType)
        assertTrue(result is Err)
    }

    @Test
    fun `submitSpot fails when saving the spot fails`() = runTest {
        every { environment.getVariable(any()) } returns "bucket123"
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any()) } returns Ok(imageUrl)
        coEvery {
            spotRepository.saveSpot(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Err(Throwable("Database error"))

        val result = service.submitSpot(filePath, createSpotData(), creatorId, contentType)
        assertTrue(result is Err)
    }

    @Test
    fun `getSpotsFeed successfully retrieves spots feed`() = runTest {
        val spotsWithVisits = listOf(
            SpotWithVisits(
                spot = spot,
                visits = listOf(
                    Visit(
                        id = 1,
                        userId = "user1",
                        spotId = 1,
                        visitTime = Clock.System.now().minus(1.days).toEpochMilliseconds(),
                        imageUrl = null
                    )
                )
            )
        )
        coEvery { spotRepository.getAllActiveAndVerifiedSpotsWithVisits() } returns Ok(spotsWithVisits)

        val result = service.getSpotsFeed()
        assertTrue(result is Ok)
    }

    @Test
    fun `getSpotsFeed fails when repository fetch fails`() = runTest {
        coEvery { spotRepository.getAllActiveAndVerifiedSpotsWithVisits() } returns Err(Throwable("Database fetch error"))

        val result = service.getSpotsFeed()
        assertTrue(result is Err)
    }

    @Test
    fun `visitSpot successfully records a spot visit`() = runTest {
        mockSpotVisitSetup()
        coEvery { visitRepository.visitSpot(any(), any(), any()) } returns Ok(Unit)

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Ok)
    }

    @Test
    fun `visitSpot fails if the spot is inactive`() = runTest {
        mockSpotVisitSetup()
        every { spot.isActive } returns false

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Err && result.error == VisitSpotError.SpotInactive)
    }

    @Test
    fun `visitSpot fails when image upload fails`() = runTest {
        mockSpotVisitSetup()
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any()) } returns Err(Throwable("Upload failed"))

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Err && result.error == VisitSpotError.ImageUpload)
    }

    @Test
    fun `visitSpot fails when fetching user visits fail`() = runTest {
        mockSpotVisitSetup()
        coEvery { visitRepository.getAllUserVisits(any()) } returns Err(Throwable("Spot fetch error"))

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Err && result.error == VisitSpotError.Generic)
    }

    @Test
    fun `visitSpot fails when user has already visited the spot`() = runTest {
        mockSpotVisitSetup()
        every { spot.id } returns 123
        coEvery { visitRepository.getAllUserVisits(any()) } returns Ok(
            listOf(
                mockk(relaxed = true) {
                    every { spotId } returns 123
                }
            )
        )

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Err && result.error == VisitSpotError.SpotVisited)
    }

    @Test
    fun `visitSpot fails when repository visitSpot call fails`() = runTest {
        mockSpotVisitSetup()
        coEvery { visitRepository.visitSpot(any(), any(), any()) } returns Err(Throwable("Database error"))

        val result = service.visitSpot(creatorId, spot.id, filePath, contentType)
        assertTrue(result is Err && result.error == VisitSpotError.Generic)
    }

    @Test
    fun `getSubmittedSpots returns successfully`() = runTest {
        coEvery { spotRepository.getSubmittedSpots(any()) } returns Ok(mockk(relaxed = true))

        val result = service.getSubmittedSpots("userId")
        assertTrue(result is Ok)
    }

    @Test
    fun `getSubmittedSpots fails when repository fails`() = runTest {
        coEvery { spotRepository.getSubmittedSpots(any()) } returns Err(Throwable("Database error"))

        val result = service.getSubmittedSpots("userId")
        assertTrue(result is Err)
    }

    @Test
    fun `reportSpot  returns successfully`() = runTest {
        coEvery { spotReportRepository.addSpotReport(any(), any(), any()) } returns Ok(mockk(relaxed = true))

        val result = service.reportSpot (123, "userId", "reason")
        assertTrue(result is Ok)
    }

    @Test
    fun `reportSpot  fails when repository fails`() = runTest {
        coEvery { spotReportRepository.addSpotReport(any(), any(), any()) } returns Err(Throwable("Database error"))

        val result = service.reportSpot (123, "userId", "reason")
        assertTrue(result is Err)
    }

    private fun createSpotData() = SubmitSpotRequest(
        title = "Nice Spot", description = "A very nice spot",
        latitude = 40.7128, longitude = -74.0060, difficulty = 3, isArea = false
    )

    private fun mockSpotVisitSetup() {
        coEvery { fileRepository.uploadImageToBucket(any(), any(), any()) } returns Ok(imageUrl)
        coEvery { spotRepository.getSpot(any()) } returns Ok(spot)
        every { spot.isActive } returns true
        coEvery { visitRepository.getAllUserVisits(any()) } returns Ok(emptyList())
        coEvery { visitRepository.visitSpot(any(), any(), any()) } returns Ok(Unit)
    }
}