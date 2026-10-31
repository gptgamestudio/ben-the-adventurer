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
    UNIFIED_GAME,
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
    var currentScreen by remember { mutableStateOf(ScreenState.UNIFIED_GAME) }

    LaunchedEffect(Unit) {
        viewModel.adMobManager.ensureInitialized(activity)
        viewModel.adMobManager.preloadInterstitial(activity)
    }

    when (currentScreen) {
        ScreenState.UNIFIED_GAME -> {
            UnifiedGameScreen(
                adMobManager = viewModel.adMobManager,
                activity = activity,
                onOpenAdminSettings = {
                    currentScreen = ScreenState.GAME_MATE
                }
            )
        }

        ScreenState.GAME_MATE -> {
            GameMateScreen(
                gameMate = gameMate,
                isSyncing = isSyncing,
                syncMessage = syncMessage,
                adMobManager = viewModel.adMobManager,
                currentUserEmail = currentUser?.email,
                onUpgradeMate = { viewModel.upgradeGameMate() },
                onForceSync = { viewModel.forceSyncCloud() },
                onBack = { currentScreen = ScreenState.UNIFIED_GAME },
                onShowInterstitialTest = {
                    viewModel.adMobManager.showInterstitial(activity) {}
                }
            )
        }
    }
}
