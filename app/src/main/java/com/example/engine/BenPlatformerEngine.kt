package com.example.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.domain.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BenPlatformerEngine {

    companion object {
        const val ONE_HOUR_TOTAL_SECONDS = 3600f
        const val LEVEL_MAX_TIME_SECONDS = 300f // 5 Minutes maximum level time
        const val GRAVITY = 1250f
        const val JUMP_VELOCITY = -620f
        const val RUN_JUMP_VELOCITY = -700f
        const val WALK_ACCEL = 1600f
        const val RUN_ACCEL = 2200f
        const val MAX_WALK_SPEED = 230f
        const val MAX_RUN_SPEED = 360f
        const val GROUND_FRICTION = 3800f // Crisp, stable footing without slipping
        const val AIR_FRICTION = 600f
    }

    var player by mutableStateOf(
        PlayerCharacter(x = 80f, y = 200f)
    )

    var currentWorld by mutableStateOf(WorldTheme.WORLD_1_TROPICAL_COAST)
    var cameraX by mutableStateOf(0f)
    var timeRemainingSeconds by mutableStateOf(ONE_HOUR_TOTAL_SECONDS)
    var levelTimerSeconds by mutableStateOf(0f) // Starts at 0, game over at 5 mins (300s)
    var levelPlayTimeSeconds by mutableStateOf(0f)
    var isGameOver by mutableStateOf(false)
    var isVictory by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var hasAwardedOneHourMilestone by mutableStateOf(false)

    val platforms = mutableStateListOf<Platform>()
    val coins = mutableStateListOf<CoinItem>()
    val enemies = mutableStateListOf<EnemyCharacter>()
    val particles = mutableStateListOf<EffectParticle>()
    val soundCues = mutableStateListOf<SoundCue>()
    var princess by mutableStateOf(PrincessClara(x = 1000f, y = 300f))

    // Input States (Controlled by On-Screen buttons)
    var inputLeft by mutableStateOf(false)
    var inputRight by mutableStateOf(false)
    var inputJump by mutableStateOf(false)
    var inputRun by mutableStateOf(false)

    fun setLeftInput(pressed: Boolean) {
        inputLeft = pressed
    }

    fun setRightInput(pressed: Boolean) {
        inputRight = pressed
    }

    fun setJumpInput(pressed: Boolean) {
        if (pressed && !inputJump && player.isGrounded) {
            triggerJump()
        }
        inputJump = pressed
    }

    fun setRunInput(pressed: Boolean) {
        inputRun = pressed
    }

    fun triggerJump() {
        if (player.isGrounded && !isGameOver && !isVictory) {
            val boost = if (inputRun) RUN_JUMP_VELOCITY else JUMP_VELOCITY
            player.vy = boost
            player.isGrounded = false
            spawnJumpDust(player.x + player.width / 2, player.y + player.height)
            spawnSoundCue("🔊 JUMP! 💨", player.x, player.y - 12f, 0xFF38BDF8)
            com.example.audio.RetroSoundPlayer.playJump()
        }
    }

    fun spawnSoundCue(text: String, x: Float, y: Float, colorHex: Long) {
        soundCues.add(SoundCue(text = text, x = x, y = y, colorHex = colorHex))
        if (soundCues.size > 8) {
            soundCues.removeAt(0)
        }
    }

    fun startAdventure(world: WorldTheme) {
        currentWorld = world
        isGameOver = false
        isVictory = false
        isPaused = false
        cameraX = 0f
        timeRemainingSeconds = ONE_HOUR_TOTAL_SECONDS
        levelTimerSeconds = 0f
        levelPlayTimeSeconds = 0f
        hasAwardedOneHourMilestone = false
        soundCues.clear()

        player = PlayerCharacter(
            x = 80f,
            y = 200f,
            vx = 0f,
            vy = 0f,
            health = 3,
            coins = 0,
            score = 0,
            distanceMeters = 0
        )

        inputLeft = false
        inputRight = false
        inputJump = false
        inputRun = false

        generateLevel(world)
    }

    private fun generateLevel(world: WorldTheme) {
        platforms.clear()
        coins.clear()
        enemies.clear()
        particles.clear()

        val worldLength = world.targetDistanceMeters * 35f

        // Solid continuous ground base with occasional island gaps
        var groundX = 0f
        while (groundX < worldLength) {
            val sectionLength = Random.nextFloat() * 400f + 350f
            platforms.add(
                Platform(
                    x = groundX,
                    y = 390f,
                    width = sectionLength,
                    height = 90f
                )
            )
            groundX += sectionLength + (if (groundX > 500f && Random.nextFloat() < 0.28f) 85f else 0f)
        }

        // Floating stepped platforms, coin clusters, and enemies
        var stepX = 260f
        while (stepX < worldLength - 300f) {
            val platY = Random.nextFloat() * 140f + 190f
            val platW = Random.nextFloat() * 120f + 110f

            platforms.add(
                Platform(
                    x = stepX,
                    y = platY,
                    width = platW,
                    height = 24f
                )
            )

            // Coins above platform
            val coinCount = Random.nextInt(2, 5)
            for (i in 0 until coinCount) {
                if (Random.nextFloat() < 0.85f) {
                    coins.add(
                        CoinItem(
                            id = "c_${stepX}_$i",
                            x = stepX + (i * 28f) + 16f,
                            y = platY - 26f
                        )
                    )
                }
            }

            // Patrol enemies on ground or large platforms
            if (Random.nextFloat() < 0.45f) {
                val eType = when (world) {
                    WorldTheme.WORLD_2_VOLCANIC_ISLAND -> EnemyType.FIRE_BAT
                    WorldTheme.WORLD_5_PIRATE_CASTLE -> EnemyType.PIRATE_MINION
                    else -> EnemyType.ISLAND_CRAB
                }
                enemies.add(
                    EnemyCharacter(
                        x = stepX + 30f,
                        y = platY - 34f,
                        leftBound = stepX,
                        rightBound = stepX + platW,
                        type = eType
                    )
                )
            }

            stepX += Random.nextFloat() * 160f + 170f
        }

        // Princess Clara Goalpost at the end
        val goalX = worldLength - 160f
        princess = PrincessClara(x = goalX, y = 330f)
        platforms.add(Platform(x = goalX - 100f, y = 390f, width = 300f, height = 90f))
    }

    fun update(delta: Float) {
        if (isGameOver || isVictory || isPaused) return

        // 5-Minute Level Countdown Timer Rule: Starts at 0, finishes at 300s (5 mins).
        // If reaches 5 minutes, player auto-dies and restarts!
        levelTimerSeconds += delta
        levelPlayTimeSeconds += delta
        if (levelTimerSeconds >= LEVEL_MAX_TIME_SECONDS && !isGameOver) {
            player.health = 0
            isGameOver = true
            spawnSoundCue("⏰ 5-MIN TIME'S UP! BACK TO START", player.x, player.y - 20f, 0xFFEF4444)
        }

        // Update active sound cues
        val cueIterator = soundCues.iterator()
        while (cueIterator.hasNext()) {
            val cue = cueIterator.next()
            cue.y -= 38f * delta
            cue.lifetime -= delta
            if (cue.lifetime <= 0f) {
                cueIterator.remove()
            }
        }

        // 1-Hour Run milestone bonus
        if (levelPlayTimeSeconds >= ONE_HOUR_TOTAL_SECONDS && !hasAwardedOneHourMilestone) {
            hasAwardedOneHourMilestone = true
            player.score += 5000
            player.coins += 100
            spawnSoundCue("🏆 1-HOUR MILESTONE! +5000", player.x, player.y - 30f, 0xFFFDE047)
        }

        // Invincibility cooldown timer
        if (player.isInvincible) {
            player.invincibleTimer -= delta
            if (player.invincibleTimer <= 0f) {
                player.isInvincible = false
            }
        }

        // --- HORIZONTAL MOVEMENT & SUPER MARIO INERTIA PHYSICS ---
        val accel = if (inputRun) RUN_ACCEL else WALK_ACCEL
        val maxSpeed = if (inputRun) MAX_RUN_SPEED else MAX_WALK_SPEED

        if (inputLeft && !inputRight) {
            player.facingDirection = -1
            player.vx -= accel * delta
            if (player.vx < -maxSpeed) {
                player.vx = -maxSpeed
            }
            player.isRunning = inputRun
        } else if (inputRight && !inputLeft) {
            player.facingDirection = 1
            player.vx += accel * delta
            if (player.vx > maxSpeed) {
                player.vx = maxSpeed
            }
            player.isRunning = inputRun
        } else {
            // Decelerate firmly towards 0 using high friction
            val friction = if (player.isGrounded) GROUND_FRICTION else AIR_FRICTION
            if (player.vx > 0) {
                player.vx = max(0f, player.vx - friction * delta)
            } else if (player.vx < 0) {
                player.vx = min(0f, player.vx + friction * delta)
            }
            if (kotlin.math.abs(player.vx) < 10f) {
                player.vx = 0f
            }
            player.isRunning = false
        }

        // Move horizontally
        player.x += player.vx * delta
        player.x = max(0f, player.x)

        // Calculate distance meters traveled
        val currentDist = (player.x / 35f).toInt()
        if (currentDist > player.distanceMeters) {
            player.distanceMeters = currentDist
        }

        // --- VERTICAL MOVEMENT & GRAVITY ---
        player.vy += GRAVITY * delta

        // Variable jump height cut: releasing jump button cuts ascent
        if (!inputJump && player.vy < -250f) {
            player.vy += 700f * delta
        }

        player.y += player.vy * delta
        player.isGrounded = false

        // Check Platform Collisions
        for (plat in platforms) {
            val playerRight = player.x + player.width
            val playerBottom = player.y + player.height
            val platRight = plat.x + plat.width
            val platBottom = plat.y + plat.height

            // AABB Overlap check
            val overlapX = playerRight > plat.x && player.x < platRight
            val overlapY = playerBottom > plat.y && player.y < platBottom

            if (overlapX && overlapY) {
                // Landing on top of platform
                val prevBottom = playerBottom - (player.vy * delta)
                if (prevBottom <= plat.y + 12f && player.vy >= 0f) {
                    player.y = plat.y - player.height
                    player.vy = 0f
                    player.isGrounded = true
                }
            }
        }

        // Bottom pit check
        if (player.y > 600f) {
            player.health = 0
            isGameOver = true
        }

        // Check Coin Pickups
        for (coin in coins) {
            if (!coin.isCollected) {
                val dx = (player.x + player.width / 2) - coin.x
                val dy = (player.y + player.height / 2) - coin.y
                if (dx * dx + dy * dy < 38f * 38f) {
                    coin.isCollected = true
                    player.coins += 1
                    player.score += coin.value
                    spawnCoinSparkle(coin.x, coin.y)
                    spawnSoundCue("🔔 CHIME! +${coin.value}", coin.x, coin.y - 14f, 0xFFFDE047)
                    com.example.audio.RetroSoundPlayer.playCoin()
                }
            }
        }

        // Update Enemies & Stomp Collision
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            if (!enemy.isAlive) continue

            // Patrol movement
            enemy.x += enemy.vx * delta
            if (enemy.x <= enemy.leftBound) {
                enemy.x = enemy.leftBound
                enemy.vx = kotlin.math.abs(enemy.vx)
            } else if (enemy.x + enemy.width >= enemy.rightBound) {
                enemy.x = enemy.rightBound - enemy.width
                enemy.vx = -kotlin.math.abs(enemy.vx)
            }

            // Player vs Enemy Collision
            val playerRight = player.x + player.width
            val playerBottom = player.y + player.height
            val enemyRight = enemy.x + enemy.width
            val enemyBottom = enemy.y + enemy.height

            if (playerRight > enemy.x && player.x < enemyRight && playerBottom > enemy.y && player.y < enemyBottom) {
                // Mario-style Stomp from above
                val prevBottom = playerBottom - (player.vy * delta)
                if (prevBottom <= enemy.y + 14f && player.vy > 0f) {
                    enemy.isAlive = false
                    player.vy = JUMP_VELOCITY * 0.8f
                    player.score += 100
                    spawnStompPuff(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2)
                    spawnSoundCue("💥 STOMP! +100", enemy.x, enemy.y - 12f, 0xFF4ADE80)
                    com.example.audio.RetroSoundPlayer.playStomp()
                } else if (!player.isInvincible) {
                    // Take damage
                    player.health -= 1
                    player.isInvincible = true
                    player.invincibleTimer = 1.6f
                    player.vy = -300f
                    player.vx = -player.facingDirection * 150f
                    spawnSoundCue("⚠️ OUCH! -1❤️", player.x, player.y - 14f, 0xFFEF4444)
                    com.example.audio.RetroSoundPlayer.playHurt()

                    if (player.health <= 0) {
                        isGameOver = true
                    }
                }
            }
        }

        // Princess Clara Rescue Trigger
        if (!princess.isRescued) {
            val playerRight = player.x + player.width
            val playerBottom = player.y + player.height
            val pRight = princess.x + princess.width
            val pBottom = princess.y + princess.height

            if (playerRight > princess.x && player.x < pRight && playerBottom > princess.y && player.y < pBottom) {
                princess = princess.copy(isRescued = true)
                isVictory = true
                player.score += 3000
                player.coins += 50
                spawnVictoryFireworks(princess.x + 20f, princess.y)
                com.example.audio.RetroSoundPlayer.playVictory()
            }
        }

        // Smooth Camera Follow
        val targetCam = player.x - 220f
        cameraX = max(0f, cameraX + (targetCam - cameraX) * 0.12f)

        // Update visual particles
        val partIterator = particles.iterator()
        while (partIterator.hasNext()) {
            val p = partIterator.next()
            p.x += p.vx * delta
            p.y += p.vy * delta
            p.lifetime -= delta
            p.alpha = max(0f, p.lifetime / 0.5f)
            if (p.lifetime <= 0f) {
                partIterator.remove()
            }
        }
    }

    private fun spawnJumpDust(x: Float, y: Float) {
        for (i in 0..4) {
            particles.add(
                EffectParticle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 120f - 60f,
                    vy = -Random.nextFloat() * 60f,
                    colorHex = 0xFFCBD5E1,
                    lifetime = 0.35f
                )
            )
        }
    }

    private fun spawnCoinSparkle(x: Float, y: Float) {
        for (i in 0..6) {
            particles.add(
                EffectParticle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 160f - 80f,
                    vy = Random.nextFloat() * 160f - 80f,
                    colorHex = 0xFFFDE047,
                    lifetime = 0.45f
                )
            )
        }
    }

    private fun spawnStompPuff(x: Float, y: Float) {
        for (i in 0..7) {
            particles.add(
                EffectParticle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 180f - 90f,
                    vy = -Random.nextFloat() * 140f,
                    colorHex = 0xFFF87171,
                    lifetime = 0.45f
                )
            )
        }
    }

    private fun spawnVictoryFireworks(x: Float, y: Float) {
        val colors = listOf(0xFFFDE047, 0xFF38BDF8, 0xFF4ADE80, 0xFFF43F5E, 0xFFA855F7)
        for (i in 0..30) {
            particles.add(
                EffectParticle(
                    x = x,
                    y = y,
                    vx = Random.nextFloat() * 320f - 160f,
                    vy = -Random.nextFloat() * 280f - 40f,
                    colorHex = colors[Random.nextInt(colors.size)],
                    lifetime = 0.8f
                )
            )
        }
    }
}
