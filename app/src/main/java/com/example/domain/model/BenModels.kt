package com.example.domain.model

enum class WorldTheme(
    val title: String,
    val subtitle: String,
    val targetDistanceMeters: Int,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val skyTopHex: Long,
    val skyBottomHex: Long,
    val groundColorHex: Long,
    val description: String
) {
    WORLD_1_TROPICAL_COAST(
        title = "Ocean Cave",
        subtitle = "Deep Blue Underwater Ambiance",
        targetDistanceMeters = 800,
        primaryColorHex = 0xFF0284C7,
        accentColorHex = 0xFF38BDF8,
        skyTopHex = 0xFF0F172A,
        skyBottomHex = 0xFF0369A1,
        groundColorHex = 0xFF0284C7,
        description = "Deep blue tones with glowing reef ledges and high contrast platforms."
    ),
    WORLD_2_VOLCANIC_ISLAND(
        title = "Desert Ruins",
        subtitle = "Warm Orange & Earthy Brown Tones",
        targetDistanceMeters = 1200,
        primaryColorHex = 0xFFEA580C,
        accentColorHex = 0xFFFDBA74,
        skyTopHex = 0xFF7C2D12,
        skyBottomHex = 0xFFEA580C,
        groundColorHex = 0xFF9A3412,
        description = "Warm orange and earthy brown dunes with ancient crumbling pillars."
    ),
    WORLD_3_CRYSTAL_CAVERNS(
        title = "Sky Realm",
        subtitle = "Light Pastel Blue & Floating Clouds",
        targetDistanceMeters = 1600,
        primaryColorHex = 0xFF38BDF8,
        accentColorHex = 0xFFC084FC,
        skyTopHex = 0xFF38BDF8,
        skyBottomHex = 0xFFE0E7FF,
        groundColorHex = 0xFF6366F1,
        description = "Light pastel blue heavens with floating cloud bridges and violet hues."
    ),
    WORLD_4_MYSTIC_CANOPY(
        title = "Forest Adventure",
        subtitle = "Vibrant Green Jungle Tones",
        targetDistanceMeters = 2000,
        primaryColorHex = 0xFF059669,
        accentColorHex = 0xFF86EFAC,
        skyTopHex = 0xFF064E3B,
        skyBottomHex = 0xFF059669,
        groundColorHex = 0xFF15803D,
        description = "Vibrant emerald green forest with mossy steps and thick foliage."
    ),
    WORLD_5_PIRATE_CASTLE(
        title = "Volcanic Fortress",
        subtitle = "Magma Red Boss Citadel",
        targetDistanceMeters = 2500,
        primaryColorHex = 0xFFD97706,
        accentColorHex = 0xFFFBBF24,
        skyTopHex = 0xFF450A0A,
        skyBottomHex = 0xFFB91C1C,
        groundColorHex = 0xFF7F1D1D,
        description = "Crimson fiery throne of Captain Claw! Rescue Princess Zoe before 5 minutes!"
    )
}

data class SoundCue(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val text: String,
    var x: Float,
    var y: Float,
    val colorHex: Long = 0xFFFDE047,
    var lifetime: Float = 0.9f
)

enum class EnemyType {
    ISLAND_CRAB,
    FIRE_BAT,
    PIRATE_MINION
}

data class PlayerCharacter(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 42f,
    val height: Float = 58f,
    var isGrounded: Boolean = false,
    var facingDirection: Int = 1, // 1 = Right, -1 = Left
    var isRunning: Boolean = false,
    var health: Int = 3,
    var maxHealth: Int = 3,
    var coins: Int = 0,
    var score: Int = 0,
    var distanceMeters: Int = 0,
    var isInvincible: Boolean = false,
    var invincibleTimer: Float = 0f
)

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isBreakable: Boolean = false
)

data class CoinItem(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 14f,
    var isCollected: Boolean = false,
    val value: Int = 10
)

data class EnemyCharacter(
    var x: Float,
    var y: Float,
    var vx: Float = 80f,
    val width: Float = 38f,
    val height: Float = 38f,
    val leftBound: Float,
    val rightBound: Float,
    val type: EnemyType = EnemyType.ISLAND_CRAB,
    var isAlive: Boolean = true
)

data class PrincessClara(
    val x: Float,
    val y: Float,
    val width: Float = 44f,
    val height: Float = 62f,
    var isRescued: Boolean = false
)

data class EffectParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val colorHex: Long,
    var lifetime: Float = 0.5f
)
