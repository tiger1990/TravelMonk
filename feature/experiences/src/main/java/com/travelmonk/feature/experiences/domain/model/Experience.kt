package com.travelmonk.feature.experiences.domain.model

data class Experience(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val rating: Double,
    val imageUrl: String,
    val category: ExperienceCategory
)

enum class ExperienceCategory { PACKAGES, GUIDES, YOGA }
