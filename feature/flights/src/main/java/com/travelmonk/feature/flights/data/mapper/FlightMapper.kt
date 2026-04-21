package com.travelmonk.feature.flights.data.mapper

import com.travelmonk.feature.flights.data.api.dto.FlightDto
import com.travelmonk.feature.flights.domain.model.Flight

/**
 * Maps [FlightDto] (network layer) to [Flight] (domain layer).
 * Enhance field mappings here when real backend is integrated.
 */
fun FlightDto.toDomain(): Flight = Flight(
    id = id,
    airline = airline,
    departureTime = departureTime,
    arrivalTime = arrivalTime,
    duration = duration,
    price = price,
    fromCode = fromCode,
    toCode = toCode
)
