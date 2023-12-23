package com.homato.service.spot

import com.homato.data.repository.FileRepository
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

@Singleton
class SpotService(
    private val fileRepository: FileRepository
) : KoinComponent {

    suspend fun submitSpot() {
       val a=  fileRepository.authorizeBackBlazeAccount()
        val b = 1
    }
}