package com.example.ui

data class Achievement(
    val id: String,
    val titleEn: String,
    val titleRu: String,
    val descriptionEn: String,
    val descriptionRu: String,
    val targetValue: Int,
    val iconType: String, // "crown", "lines", "speed", "score", "blast", "combo"
    val pointsReward: Int,
    val isUnlocked: Boolean = false,
    val currentValue: Int = 0
)
