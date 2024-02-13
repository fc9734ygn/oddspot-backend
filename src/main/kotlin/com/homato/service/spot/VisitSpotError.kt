package com.homato.service.spot

sealed class VisitSpotError {
    object ImageUpload : VisitSpotError()
    object SpotNotFound : VisitSpotError()
    object SpotInactive : VisitSpotError()
    object SpotRecentlyVisited : VisitSpotError()
    object Generic : VisitSpotError()
}