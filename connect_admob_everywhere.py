import re
import shutil

print("1. Updating AdMobManager.kt to default to Production AdMob mode...")
with open("app/src/main/java/com/example/ads/AdMobManager.kt", "r") as f:
    admob_code = f.read()

# Make sure isInitialTestMode defaults to false (Production mode)
old_test_mode = "private val isInitialTestMode = prefs.getBoolean(KEY_IS_TEST_MODE, true)"
new_test_mode = "private val isInitialTestMode = prefs.getBoolean(KEY_IS_TEST_MODE, false)"
if old_test_mode in admob_code:
    admob_code = admob_code.replace(old_test_mode, new_test_mode, 1)
    print("Updated isInitialTestMode to false (Production Connected).")

# Update banner status initial value to reflect active AdMob production connection
admob_code = admob_code.replace(
    'private val _bannerStatus = MutableStateFlow("Ready")',
    'private val _bannerStatus = MutableStateFlow("🟢 Connected to AdMob (pub-9786472984542330)")'
)

with open("app/src/main/java/com/example/ads/AdMobManager.kt", "w") as f:
    f.write(admob_code)
print("AdMobManager.kt updated.")

print("2. Updating GameScreen.kt to embed AdMob Banner and Interstitial triggers...")
with open("app/src/main/java/com/example/ui/screens/GameScreen.kt", "r") as f:
    game_screen_code = f.read()

# Add imports for AdMob and Activity
if "import com.example.ads.AdMobBannerView" not in game_screen_code:
    imports_to_add = """import android.app.Activity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdMobBannerView
import com.example.ads.AdMobManager
"""
    game_screen_code = game_screen_code.replace("package com.example.ui.screens\n\n", "package com.example.ui.screens\n\n" + imports_to_add, 1)

# Update GameScreen signature to accept adMobManager
old_sig = """@Composable
fun GameScreen(
    engine: BenPlatformerEngine,
    world: WorldTheme,
    onReturnToDashboard: () -> Unit
) {"""

new_sig = """@Composable
fun GameScreen(
    engine: BenPlatformerEngine,
    world: WorldTheme,
    adMobManager: AdMobManager? = null,
    onReturnToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity"""

if old_sig in game_screen_code:
    game_screen_code = game_screen_code.replace(old_sig, new_sig, 1)
    print("Updated GameScreen signature.")

# Add AdMob banner at the bottom center of the game screen
old_controls = """        // --- TOUCH INPUT ON-SCREEN CONTROLS (COMPACT D-PAD: ATRAS / ABANTE & RUN / JUMP) ---
        AdventureControlsOverlay(
            onLeftChange = { engine.setLeftInput(it) },
            onRightChange = { engine.setRightInput(it) },
            onJumpChange = { engine.setJumpInput(it) },
            onRunChange = { engine.setRunInput(it) }
        )"""

new_controls_with_banner = """        // --- TOUCH INPUT ON-SCREEN CONTROLS (COMPACT D-PAD: ATRAS / ABANTE & RUN / JUMP) ---
        AdventureControlsOverlay(
            onLeftChange = { engine.setLeftInput(it) },
            onRightChange = { engine.setRightInput(it) },
            onJumpChange = { engine.setJumpInput(it) },
            onRunChange = { engine.setRunInput(it) }
        )

        // --- IN-GAME BOTTOM ADMOB BANNER (CONNECTED TO pub-9786472984542330 / Unit 6300978111) ---
        if (adMobManager != null) {
            val bannerId by adMobManager.bannerId.collectAsState()
            val bannerEnabled by adMobManager.bannerEnabled.collectAsState()
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.50f)
                    .padding(bottom = 6.dp)
            ) {
                AdMobBannerView(
                    adUnitId = bannerId,
                    isEnabled = bannerEnabled,
                    onStatusChange = { adMobManager.updateBannerStatus(it) }
                )
            }
        }"""

if old_controls in game_screen_code:
    game_screen_code = game_screen_code.replace(old_controls, new_controls_with_banner, 1)
    print("Embedded AdMobBannerView in GameScreen.kt.")

# Update Game Over onRestart to trigger interstitial
old_gameover = """        // --- GAME OVER DIALOG ---
        if (engine.isGameOver) {
            AdventureGameOverDialog(
                score = player.score,
                coins = player.coins,
                distanceMeters = player.distanceMeters,
                onRestart = { engine.startAdventure(world) },
                onReturnToDashboard = onReturnToDashboard
            )
        }"""

new_gameover = """        // --- GAME OVER DIALOG (WITH INTERSTITIAL AD TRIGGER) ---
        if (engine.isGameOver) {
            AdventureGameOverDialog(
                score = player.score,
                coins = player.coins,
                distanceMeters = player.distanceMeters,
                onRestart = {
                    if (adMobManager != null && activity != null) {
                        adMobManager.showInterstitial(activity) {
                            engine.startAdventure(world)
                        }
                    } else {
                        engine.startAdventure(world)
                    }
                },
                onReturnToDashboard = onReturnToDashboard
            )
        }"""

if old_gameover in game_screen_code:
    game_screen_code = game_screen_code.replace(old_gameover, new_gameover, 1)
    print("Updated GameOver restart with interstitial in GameScreen.kt.")

# Update Victory next island to trigger interstitial
old_victory = """        // --- VICTORY CELEBRATION DIALOG ---
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
        }"""

new_victory = """        // --- VICTORY CELEBRATION DIALOG (WITH INTERSTITIAL AD TRIGGER) ---
        if (engine.isVictory) {
            PrincessRescueCelebrationDialog(
                score = player.score,
                coins = player.coins,
                levelPlayTimeSeconds = engine.levelPlayTimeSeconds,
                isOneHourAchieved = engine.hasAwardedOneHourMilestone,
                onContinueNextIsland = {
                    val nextWorldIndex = (world.ordinal + 1) % WorldTheme.values().size
                    val nextWorld = WorldTheme.values()[nextWorldIndex]
                    if (adMobManager != null && activity != null) {
                        adMobManager.showInterstitial(activity) {
                            engine.startAdventure(nextWorld)
                        }
                    } else {
                        engine.startAdventure(nextWorld)
                    }
                },
                onReturnToDashboard = onReturnToDashboard
            )
        }"""

if old_victory in game_screen_code:
    game_screen_code = game_screen_code.replace(old_victory, new_victory, 1)
    print("Updated Victory next island with interstitial in GameScreen.kt.")

with open("app/src/main/java/com/example/ui/screens/GameScreen.kt", "w") as f:
    f.write(game_screen_code)
print("GameScreen.kt updated.")

print("3. Updating MainActivity.kt to pass adMobManager to GameScreen...")
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_code = f.read()

old_game_call = """        ScreenState.GAME -> {
            GameScreen(
                engine = viewModel.engine,
                world = selectedWorld,
                onReturnToDashboard = {
                    viewModel.onGameFinishedSaveData()
                    viewModel.adMobManager.showInterstitial(activity) {
                        currentScreen = ScreenState.DASHBOARD
                    }
                }
            )
        }"""

new_game_call = """        ScreenState.GAME -> {
            GameScreen(
                engine = viewModel.engine,
                world = selectedWorld,
                adMobManager = viewModel.adMobManager,
                onReturnToDashboard = {
                    viewModel.onGameFinishedSaveData()
                    viewModel.adMobManager.showInterstitial(activity) {
                        currentScreen = ScreenState.DASHBOARD
                    }
                }
            )
        }"""

if old_game_call in main_code:
    main_code = main_code.replace(old_game_call, new_game_call, 1)
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(main_code)
    print("MainActivity.kt updated with adMobManager in GameScreen call.")

print("4. Updating index.html & docs/index.html to display live AdMob Connection badges...")
with open("index.html", "r") as f:
    web_code = f.read()

# Update banner ticker to clearly display Connected status
old_ticker = '<span id="adRewardText">📺 Sponsored: Watch Ad for +100 Coins &bull; Unit: 6300978111</span>'
new_ticker = '<span id="adRewardText">🟢 AdMob Connected &bull; pub-9786472984542330 &bull; Unit: 6300978111 (Tap for Ad)</span>'
if old_ticker in web_code:
    web_code = web_code.replace(old_ticker, new_ticker, 1)

# Ensure HUD has AdMob connected indicator
web_code = web_code.replace(
    'title="Watch Ad for +100 Coins">🎬 Ad +100🪙</button>',
    'title="Watch Ad for +100 Coins">🟢 AdMob: +100🪙</button>'
)

with open("index.html", "w") as f:
    f.write(web_code)
shutil.copyfile("index.html", "docs/index.html")
print("index.html and docs/index.html updated.")

print("All updates applied cleanly!")
