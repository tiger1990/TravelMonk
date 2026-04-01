package com.travelmonk.feature.stays.domain.model

data class Stay(
    val id: String,
    val title: String,
    val location: String,
    val price: String,
    val rating: String,
    val imageUrl: String
)
