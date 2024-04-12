package com.homato.service.wishlist

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.homato.data.repository.WishlistRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WishlistServiceTest {
    private val wishlistRepository: WishlistRepository = mockk(relaxed = true)
    private lateinit var service: WishlistService

    @BeforeEach
    fun setUp() {
        service = WishlistService(wishlistRepository)
    }

    @Test
    fun `addToWishlist successfully adds an item`() = runTest {
        coEvery { wishlistRepository.addToWishlist("userId", 1) } returns Ok(Unit)

        val result = service.addToWishlist("userId", 1)
        assertTrue(result is Ok)
    }

    @Test
    fun `addToWishlist fails to add an item`() = runTest {
        coEvery { wishlistRepository.addToWishlist("userId", 1) } returns Err(Throwable("Database error"))

        val result = service.addToWishlist("userId", 1)
        assertTrue(result is Err && result.error.message == "Database error")
    }

    @Test
    fun `removeFromWishlist successfully removes an item`() = runTest {
        coEvery { wishlistRepository.removeFromWishlist("userId", 1) } returns Ok(Unit)

        val result = service.removeFromWishlist("userId", 1)
        assertTrue(result is Ok)
    }

    @Test
    fun `removeFromWishlist fails to remove an item`() = runTest {
        coEvery { wishlistRepository.removeFromWishlist("userId", 1) } returns Err(Throwable("Database error"))

        val result = service.removeFromWishlist("userId", 1)
        assertTrue(result is Err && result.error.message == "Database error")
    }

    @Test
    fun `getWishlist successfully retrieves a wishlist`() = runTest {
        val wishlist = listOf(3, 6)
        coEvery { wishlistRepository.getWishlist("userId") } returns Ok(wishlist)

        val result = service.getWishlist("userId")
        assertTrue(result is Ok && result.value.spotIds == wishlist)
    }

    @Test
    fun `getWishlist fails to retrieve a wishlist`() = runTest {
        coEvery { wishlistRepository.getWishlist("userId") } returns Err(Throwable("Database error"))

        val result = service.getWishlist("userId")
        assertTrue(result is Err && result.error.message == "Database error")
    }
}