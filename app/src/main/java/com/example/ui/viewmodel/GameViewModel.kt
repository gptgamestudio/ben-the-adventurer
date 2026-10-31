package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.auth.AuthManager
import com.example.data.models.AdventurerProfile
import com.example.data.models.GameMateSync
import com.example.data.repository.AdventurerProfileRepository
import com.example.data.repository.GameMateRepository
import com.example.domain.model.WorldTheme
import com.example.engine.BenPlatformerEngine
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    application: Application
) : AndroidViewModel(application) {

    val engine = BenPlatformerEngine()
    val authManager = AuthManager(application)
    val adMobManager = AdMobManager(application)

    private val profileRepository = AdventurerProfileRepository()
    private val gameMateRepository = GameMateRepository()

    private val _userProfile = MutableStateFlow<AdventurerProfile?>(null)
    val userProfile: StateFlow<AdventurerProfile?> = _userProfile.asStateFlow()

    private val _gameMate = MutableStateFlow<GameMateSync?>(null)
    val gameMate: StateFlow<GameMateSync?> = _gameMate.asStateFlow()

    private val _isCloudSynced = MutableStateFlow(false)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        adMobManager.ensureInitialized(application)
        adMobManager.preloadInterstitial(application)

        // Observe Auth State to sync Firestore profile and game mate
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    loadCloudData(user)
                } else {
                    _userProfile.value = null
                    _gameMate.value = null
                    _isCloudSynced.value = false
                }
            }
        }

        // Game Loop Coroutine
        viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                engine.update(delta)
                kotlinx.coroutines.delay(16) // ~60 FPS
            }
        }
    }

    private fun loadCloudData(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                profileRepository.getProfileFlow(user.uid).collect { profile ->
                    if (profile != null) {
                        _userProfile.value = profile
                        _isCloudSynced.value = true
                    } else {
                        val newProfile = AdventurerProfile(
                            userId = user.uid,
                            playerName = user.displayName ?: "Adventurer Ben",
                            email = user.email ?: "",
                            totalScore = 0,
                            coins = 0,
                            highestDistanceMeters = 0,
                            princessClaraRescues = 0,
                            totalPlayTimeSeconds = 0f,
                            isOneHourMilestoneAchieved = false,
                            currentWorld = "WORLD_1_TROPICAL_COAST"
                        )
                        try {
                            profileRepository.saveProfile(newProfile)
                            _userProfile.value = newProfile
                            _isCloudSynced.value = true
                        } catch (e: Exception) {
                            _userProfile.value = newProfile
                        }
                    }
                }
            } catch (e: Exception) {
                _syncMessage.value = "Profile sync offline: ${e.message}"
            }
        }

        viewModelScope.launch {
            try {
                gameMateRepository.getGameMateFlow(user.uid).collect { mate ->
                    if (mate != null) {
                        _gameMate.value = mate
                    } else {
                        val newMate = GameMateSync(
                            userId = user.uid,
                            playerName = user.displayName ?: "Adventurer Ben",
                            companionName = "Dex the Monkey",
                            companionLevel = 1,
                            companionBonus = "+10% Coin Magnet & Speed Booster"
                        )
                        try {
                            gameMateRepository.saveGameMate(newMate)
                            _gameMate.value = newMate
                        } catch (e: Exception) {
                            _gameMate.value = newMate
                        }
                    }
                }
            } catch (e: Exception) {
                _syncMessage.value = "Mate sync offline: ${e.message}"
            }
        }
    }

    fun startLevel(world: WorldTheme) {
        engine.startAdventure(world)
    }

    fun onGameFinishedSaveData() {
        val user = authManager.currentUser.value
        val currentProfile = _userProfile.value
        val playScore = engine.player.score
        val playCoins = engine.player.coins
        val playDistance = engine.player.distanceMeters
        val rescued = if (engine.isVictory) 1 else 0
        val playTime = engine.levelPlayTimeSeconds

        if (user != null) {
            viewModelScope.launch {
                _isSyncing.value = true
                val updatedProfile = AdventurerProfile(
                    userId = user.uid,
                    playerName = user.displayName ?: (currentProfile?.playerName ?: "Adventurer Ben"),
                    email = user.email ?: (currentProfile?.email ?: ""),
                    totalScore = (currentProfile?.totalScore ?: 0) + playScore,
                    coins = (currentProfile?.coins ?: 0) + playCoins,
                    highestDistanceMeters = maxOf(currentProfile?.highestDistanceMeters ?: 0, playDistance),
                    princessClaraRescues = (currentProfile?.princessClaraRescues ?: 0) + rescued,
                    totalPlayTimeSeconds = (currentProfile?.totalPlayTimeSeconds ?: 0f) + playTime,
                    isOneHourMilestoneAchieved = (currentProfile?.isOneHourMilestoneAchieved == true) || engine.hasAwardedOneHourMilestone,
                    currentWorld = engine.currentWorld.name,
                    updatedAt = System.currentTimeMillis()
                )
                try {
                    profileRepository.saveProfile(updatedProfile)
                    _userProfile.value = updatedProfile
                    _isCloudSynced.value = true
                    _syncMessage.value = "Progress saved to Firestore Cloud!"
                } catch (e: Exception) {
                    _syncMessage.value = "Offline cache: ${e.message}"
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    fun upgradeGameMate() {
        val user = authManager.currentUser.value
        val currentMate = _gameMate.value
        if (user != null && currentMate != null) {
            viewModelScope.launch {
                _isSyncing.value = true
                val newLevel = currentMate.companionLevel + 1
                val bonus = when (newLevel) {
                    2 -> "+15% Coin Magnet & Dash Booster"
                    3 -> "+25% Super Jump & Island Radar"
                    4 -> "+35% Coin Duplicator & Shield Aura"
                    else -> "+50% Legendary Explorer Aura & All Perks"
                }
                val updatedMate = currentMate.copy(
                    companionLevel = newLevel,
                    companionBonus = bonus,
                    updatedAt = System.currentTimeMillis()
                )
                try {
                    gameMateRepository.saveGameMate(updatedMate)
                    _gameMate.value = updatedMate
                    _syncMessage.value = "Companion Dex leveled up to Lv. $newLevel!"
                } catch (e: Exception) {
                    _syncMessage.value = "Update failed: ${e.message}"
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    fun forceSyncCloud() {
        val user = authManager.currentUser.value
        if (user != null) {
            loadCloudData(user)
            _syncMessage.value = "Synced with Firestore Realtime Database."
        }
    }
}
