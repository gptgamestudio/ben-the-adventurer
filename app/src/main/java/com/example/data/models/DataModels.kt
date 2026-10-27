package com.example.data.models

data class AdventurerProfile(
    val userId: String = "",
    val playerName: String = "Ben",
    val email: String = "",
    val totalScore: Int = 0,
    val coins: Int = 0,
    val highestDistanceMeters: Int = 0,
    val princessClaraRescues: Int = 0,
    val totalPlayTimeSeconds: Float = 0f,
    val isOneHourMilestoneAchieved: Boolean = false,
    val currentWorld: String = "WORLD_1_TROPICAL_COAST",
    val updatedAt: Long = System.currentTimeMillis()
)

data class GameMateSync(
    val userId: String = "",
    val playerName: String = "Ben",
    val companionName: String = "Dex the Monkey",
    val companionLevel: Int = 1,
    val companionBonus: String = "+10% Coin Magnet & Speed Booster",
    val updatedAt: Long = System.currentTimeMillis()
)
