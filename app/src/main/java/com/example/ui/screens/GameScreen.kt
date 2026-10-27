package com.example.ui.screens

import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.EnemyType
import com.example.domain.model.WorldTheme
import com.example.engine.BenPlatformerEngine
import com.example.ui.components.AdventureControlsOverlay
import com.example.ui.components.AdventureGameOverDialog
import com.example.ui.components.PrincessRescueCelebrationDialog

@Composable
fun GameScreen(
    engine: BenPlatformerEngine,
    world: WorldTheme,
    onReturnToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onReturnToDashboard()
    }

    val player = engine.player
    val cameraX = engine.cameraX
    val platforms = engine.platforms
    val coins = engine.coins
    val enemies = engine.enemies
    val princess = engine.princess
    val particles = engine.particles
    val soundCues = engine.soundCues

    // Kenney.nl CC0 Sprites
    val playerStand = ImageBitmap.imageResource(id = R.drawable.player_stand)
    val playerWalk1 = ImageBitmap.imageResource(id = R.drawable.player_walk1)
    val playerWalk2 = ImageBitmap.imageResource(id = R.drawable.player_walk2)
    val playerJump = ImageBitmap.imageResource(id = R.drawable.player_jump)
    val playerHurt = ImageBitmap.imageResource(id = R.drawable.player_hurt)
    val princessSprite = ImageBitmap.imageResource(id = R.drawable.princess_cheer)
    val enemySlime = ImageBitmap.imageResource(id = R.drawable.enemy_slime)
    val enemyBee = ImageBitmap.imageResource(id = R.drawable.enemy_bee)
    val enemyFrog = ImageBitmap.imageResource(id = R.drawable.enemy_frog)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(world.skyTopHex))
            .testTag("game_screen_root")
    ) {
        // --- 2D CANVAS GAMEPLAY RENDERER WITH DYNAMIC ENVIRONMENT ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. DYNAMIC GAME AREA SKY & ENVIRONMENT GRADIENT
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(world.skyTopHex),
                        Color(world.skyBottomHex)
                    )
                ),
                size = Size(canvasW, canvasH)
            )

            // 2. PARALLAX ENVIRONMENT SILHOUETTES BASED ON ACTIVE GAME AREA
            val bgOffset = (cameraX * 0.22f) % 640f
            for (i in -1..4) {
                val mountainX = (i * 640f) - bgOffset
                // Distant atmospheric layer
                drawCircle(
                    color = Color(world.groundColorHex).copy(alpha = 0.28f),
                    radius = 260f,
                    center = Offset(mountainX + 220f, canvasH - 20f)
                )
                // Midground dune/canopy hills
                drawCircle(
                    color = Color(world.primaryColorHex).copy(alpha = 0.40f),
                    radius = 180f,
                    center = Offset(mountainX + 460f, canvasH + 10f)
                )
            }

            // 3. PLATFORMS & ISLAND GROUND (HIGH CONTRAST BORDERS & SHADOWS)
            for (plat in platforms) {
                val screenX = plat.x - cameraX
                if (screenX + plat.width >= 0 && screenX <= canvasW) {
                    // Dark drop shadow for high contrast readability
                    drawRect(
                        color = Color(0xAA000000),
                        topLeft = Offset(screenX + 4f, plat.y + 4f),
                        size = Size(plat.width, plat.height)
                    )
                    // Platform base rock
                    drawRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(screenX, plat.y),
                        size = Size(plat.width, plat.height)
                    )
                    // Top environment accent strip (High contrast environment color)
                    drawRect(
                        color = Color(world.accentColorHex),
                        topLeft = Offset(screenX, plat.y),
                        size = Size(plat.width, 8f)
                    )
                    // Bright top highlight line for instant eye-tracking
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(screenX, plat.y),
                        size = Size(plat.width, 2f)
                    )
                }
            }

            // 4. GOLD COINS (HIGH CONTRAST GLOW)
            for (coin in coins) {
                if (!coin.isCollected) {
                    val screenX = coin.x - cameraX
                    if (screenX >= -20f && screenX <= canvasW + 20f) {
                        // Dark outer ring for high contrast
                        drawCircle(
                            color = Color(0xFF78350F),
                            radius = coin.radius + 2f,
                            center = Offset(screenX, coin.y)
                        )
                        // Radiant Gold Coin
                        drawCircle(
                            color = Color(0xFFFDE047),
                            radius = coin.radius,
                            center = Offset(screenX, coin.y)
                        )
                        drawCircle(
                            color = Color(0xFFCA8A04),
                            radius = coin.radius * 0.55f,
                            center = Offset(screenX, coin.y)
                        )
                    }
                }
            }

            // 5. ENEMIES (KENNEY CC0 SPRITES)
            for (enemy in enemies) {
                if (enemy.isAlive) {
                    val screenX = enemy.x - cameraX
                    if (screenX + enemy.width >= 0 && screenX <= canvasW) {
                        val enemySprite = when (enemy.type) {
                            EnemyType.ISLAND_CRAB -> enemySlime
                            EnemyType.FIRE_BAT -> enemyBee
                            EnemyType.PIRATE_MINION -> enemyFrog
                        }
                        drawImage(
                            image = enemySprite,
                            dstOffset = IntOffset(screenX.toInt(), (enemy.y - 4).toInt()),
                            dstSize = IntSize((enemy.width + 6).toInt(), (enemy.height + 6).toInt())
                        )
                    }
                }
            }

            // 6. PRINCESS ZOE (KENNEY CC0 SPRITE)
            val pScreenX = princess.x - cameraX
            if (pScreenX + princess.width >= 0 && pScreenX <= canvasW) {
                drawImage(
                    image = princessSprite,
                    dstOffset = IntOffset(pScreenX.toInt(), (princess.y - 12).toInt()),
                    dstSize = IntSize((princess.width + 8).toInt(), (princess.height + 12).toInt())
                )
            }

            // 7. PARTICLES
            for (part in particles) {
                val screenX = part.x - cameraX
                drawCircle(
                    color = Color(part.colorHex).copy(alpha = part.alpha),
                    radius = 4f,
                    center = Offset(screenX, part.y)
                )
            }

            // 8. BEN THE ADVENTURER (KENNEY CC0 SPRITE)
            val playerScreenX = player.x - cameraX
            val isFlashing = player.isInvincible && ((player.invincibleTimer * 10).toInt() % 2 == 0)
            if (!isFlashing) {
                val playerSprite = when {
                    player.invincibleTimer > 0f -> playerHurt
                    !player.isGrounded -> playerJump
                    player.vx != 0f -> {
                        val step = ((player.x / 20).toInt() % 2)
                        if (step == 0) playerWalk1 else playerWalk2
                    }
                    else -> playerStand
                }

                val pX = playerScreenX.toInt()
                val pY = (player.y - 8).toInt()
                val pW = (player.width + 10).toInt()
                val pH = (player.height + 8).toInt()

                val flipX = player.facingDirection < 0
                if (flipX) {
                    scale(
                        scaleX = -1f,
                        scaleY = 1f,
                        pivot = Offset(playerScreenX + player.width / 2f, player.y + player.height / 2f)
                    ) {
                        drawImage(
                            image = playerSprite,
                            dstOffset = IntOffset(pX, pY),
                            dstSize = IntSize(pW, pH)
                        )
                    }
                } else {
                    drawImage(
                        image = playerSprite,
                        dstOffset = IntOffset(pX, pY),
                        dstSize = IntSize(pW, pH)
                    )
                }
            }

            // 9. VISUAL SOUND EFFECT FEEDBACK (FLOATING CUES: JUMP, COIN CHIME, STOMP)
            for (cue in soundCues) {
                val cueScreenX = cue.x - cameraX
                if (cueScreenX >= -40f && cueScreenX <= canvasW + 40f) {
                    val alpha = (cue.lifetime / 0.9f).coerceIn(0f, 1f)
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f
                        isFakeBoldText = true
                        setShadowLayer(6f, 2f, 2f, android.graphics.Color.BLACK)
                        this.alpha = (alpha * 255).toInt()
                    }
                    drawContext.canvas.nativeCanvas.drawText(cue.text, cueScreenX - 20f, cue.y, textPaint)
                }
            }
        }

        // --- HUD OVERLAY TOP BAR (FULL 16:9 LANDSCAPE, EXACT STATS & 5-MIN TIMER) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD90F172A), RoundedCornerShape(12.dp))
                    .border(1.5.dp, Color(world.accentColorHex), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hearts & Lives
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..player.maxHealth) {
                        Text(
                            text = if (i <= player.health) "❤️" else "🖤",
                            fontSize = 17.sp
                        )
                    }
                }

                // Stats: Score, Coins, Distance, Active Game Area Name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCORE: ${player.score}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "COINS: ${player.coins} 🪙",
                        color = Color(0xFFFDE047),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "DIST: ${player.distanceMeters}m / ${world.targetDistanceMeters}m",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Level Countdown Timer: Starts at 00:00, Game Over at 05:00
                val elapsedSec = engine.levelTimerSeconds.toInt()
                val m = elapsedSec / 60
                val s = elapsedSec % 60
                val isUrgent = elapsedSec >= 240
                Text(
                    text = "⏱️ ${String.format("%02d:%02d / 05:00", m, s)}",
                    color = if (isUrgent) Color(0xFFEF4444) else Color(0xFF4ADE80),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-HUD Row: Active Game Area Badge & Adventure BGM Audio Indicator Waveform
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Game Area Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(world.accentColorHex), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📍 AREA: ${world.title.uppercase()}",
                        color = Color(world.accentColorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Active Adventure BGM Audio Indicator / Waveform Overlay
                BgmWaveformIndicator(areaTitle = world.title)
            }
        }

        // --- TOUCH INPUT ON-SCREEN CONTROLS (COMPACT D-PAD: ATRAS / ABANTE & RUN / JUMP) ---
        AdventureControlsOverlay(
            onLeftChange = { engine.setLeftInput(it) },
            onRightChange = { engine.setRightInput(it) },
            onJumpChange = { engine.setJumpInput(it) },
            onRunChange = { engine.setRunInput(it) }
        )

        // --- GAME OVER DIALOG ---
        if (engine.isGameOver) {
            AdventureGameOverDialog(
                score = player.score,
                coins = player.coins,
                distanceMeters = player.distanceMeters,
                onRestart = { engine.startAdventure(world) },
                onReturnToDashboard = onReturnToDashboard
            )
        }

        // --- VICTORY CELEBRATION DIALOG ---
        if (engine.isVictory) {
            PrincessRescueCelebrationDialog(
                score = player.score,
                coins = player.coins,
                levelPlayTimeSeconds = engine.levelPlayTimeSeconds,
                isOneHourAchieved = engine.hasAwardedOneHourMilestone,
                onContinueNextIsland = {
                    val nextWorldIndex = (world.ordinal + 1) % WorldTheme.values().size
                    val nextWorld = WorldTheme.values()[nextWorldIndex]
                    engine.startAdventure(nextWorld)
                },
                onReturnToDashboard = onReturnToDashboard
            )
        }
    }
}

/**
 * Active Adventure BGM Audio Indicator & Waveform Equalizer
 */
@Composable
private fun BgmWaveformIndicator(
    areaTitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgm_waveform")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(340, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(270, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(410, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "b4"
    )

    Row(
        modifier = modifier
            .background(Color(0xD90F172A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("🎵", fontSize = 10.sp)
        Box(modifier = Modifier.width(2.5.dp).height(bar1.dp).background(Color(0xFF38BDF8), RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(2.5.dp).height(bar2.dp).background(Color(0xFF4ADE80), RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(2.5.dp).height(bar3.dp).background(Color(0xFFFDE047), RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(2.5.dp).height(bar4.dp).background(Color(0xFFF43F5E), RoundedCornerShape(1.dp)))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "BGM: $areaTitle Theme",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
