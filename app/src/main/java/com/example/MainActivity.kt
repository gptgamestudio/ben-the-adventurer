package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.WorldTheme
import com.example.ui.screens.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

enum class ScreenState {
    SIGN_IN,
    DASHBOARD,
    GAME,
    GAME_MATE
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clean up empty 0-byte Chromium variations seed files that trigger 'Seed missing signature' warning
        try {
            val webviewDir = java.io.File(applicationInfo.dataDir, "app_webview")
            if (webviewDir.exists() && webviewDir.isDirectory) {
                listOf("variations_seed", "variations_seed_new", "variations_seed_stamp").forEach { name ->
                    val f = java.io.File(webviewDir, name)
                    if (f.exists() && f.length() == 0L) {
                        f.delete()
                    }
                }
            }
        } catch (_: Exception) {}

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel, activity = this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: GameViewModel,
    activity: Activity
) {
    val currentUser by viewModel.authManager.currentUser.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val gameMate by viewModel.gameMate.collectAsStateWithLifecycle()
    val isCloudSynced by viewModel.isCloudSynced.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(ScreenState.DASHBOARD) }
    var selectedWorld by remember { mutableStateOf(WorldTheme.WORLD_1_TROPICAL_COAST) }

    LaunchedEffect(Unit) {
        viewModel.adMobManager.ensureInitialized(activity)
        viewModel.adMobManager.preloadInterstitial(activity)
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && currentScreen == ScreenState.SIGN_IN) {
            currentScreen = ScreenState.DASHBOARD
        }
    }

    when (currentScreen) {
        ScreenState.SIGN_IN -> {
            SignInScreen(
                onSignInClick = { viewModel.authManager.signInWithGoogle() },
                onEmailSignIn = { email, pass -> viewModel.authManager.signInWithEmail(email, pass) },
                onEmailSignUp = { email, pass -> viewModel.authManager.signUpWithEmail(email, pass) },
                onPlayOffline = { currentScreen = ScreenState.DASHBOARD }
            )
        }

        ScreenState.DASHBOARD -> {
            DashboardScreen(
                profile = userProfile,
                gameMate = gameMate,
                isCloudSynced = isCloudSynced,
                isSyncing = isSyncing,
                syncMessage = syncMessage,
                adMobManager = viewModel.adMobManager,
                onEmbark = { world ->
                    selectedWorld = world
                    viewModel.startLevel(world)
                    currentScreen = ScreenState.GAME
                },
                onOpenGameMate = { currentScreen = ScreenState.GAME_MATE },
                onSignOut = {
                    scope.launch {
                        viewModel.authManager.signOut()
                        currentScreen = ScreenState.SIGN_IN
                    }
                }
            )
        }

        ScreenState.GAME -> {
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
        }

        ScreenState.GAME_MATE -> {
            GameMateScreen(
                gameMate = gameMate,
                isSyncing = isSyncing,
                syncMessage = syncMessage,
                adMobManager = viewModel.adMobManager,
                onUpgradeMate = { viewModel.upgradeGameMate() },
                onForceSync = { viewModel.forceSyncCloud() },
                onBack = { currentScreen = ScreenState.DASHBOARD },
                onShowInterstitialTest = {
                    viewModel.adMobManager.showInterstitial(activity) {}
                }
            )
        }
    }
}
