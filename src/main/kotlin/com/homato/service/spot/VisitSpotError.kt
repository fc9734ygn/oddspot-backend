package com.homato.service.spot

sealed class VisitSpotError {
    object ImageUpload : VisitSpotError()
    object SpotNotFound : VisitSpotError()
    object SpotInactive : VisitSpotError()
    object SpotVisited : VisitSpotError()
    object Generic : VisitSpotError()
}