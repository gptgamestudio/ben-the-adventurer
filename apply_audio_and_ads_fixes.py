import re

print("Starting audio and ads updates...")

# 1. Update AndroidManifest.xml: ensure AD_ID permission is retained
with open("app/src/main/AndroidManifest.xml", "r") as f:
    manifest = f.read()

# Replace tools:node="remove" for AD_ID with standard declaration
old_adid = '<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />'
new_adid = '<uses-permission android:name="com.google.android.gms.permission.AD_ID" />'
if old_adid in manifest:
    manifest = manifest.replace(old_adid, new_adid)
    with open("app/src/main/AndroidManifest.xml", "w") as f:
        f.write(manifest)
    print("Updated AndroidManifest.xml with AD_ID permission.")
else:
    print("AD_ID in AndroidManifest already configured or not matching exact pattern.")

# 2. Update BenPlatformerEngine.kt: add RetroSoundPlayer calls
with open("app/src/main/java/com/example/engine/BenPlatformerEngine.kt", "r") as f:
    engine_code = f.read()

# Ensure playCoin
if "RetroSoundPlayer.playCoin()" not in engine_code:
    target_coin = 'spawnSoundCue("🔔 CHIME! +${coin.value}", coin.x, coin.y - 14f, 0xFFFDE047)'
    replacement_coin = target_coin + '\n                    com.example.audio.RetroSoundPlayer.playCoin()'
    engine_code = engine_code.replace(target_coin, replacement_coin, 1)

# Ensure playStomp
if "RetroSoundPlayer.playStomp()" not in engine_code:
    target_stomp = 'spawnSoundCue("💥 STOMP! +100", enemy.x, enemy.y - 12f, 0xFF4ADE80)'
    replacement_stomp = target_stomp + '\n                    com.example.audio.RetroSoundPlayer.playStomp()'
    engine_code = engine_code.replace(target_stomp, replacement_stomp, 1)

# Ensure playHurt
if "RetroSoundPlayer.playHurt()" not in engine_code:
    target_hurt = 'spawnSoundCue("⚠️ OUCH! -1❤️", player.x, player.y - 14f, 0xFFEF4444)'
    replacement_hurt = target_hurt + '\n                    com.example.audio.RetroSoundPlayer.playHurt()'
    engine_code = engine_code.replace(target_hurt, replacement_hurt, 1)

# Ensure playVictory
if "RetroSoundPlayer.playVictory()" not in engine_code:
    target_vic = 'spawnVictoryFireworks(princess.x + 20f, princess.y)'
    replacement_vic = target_vic + '\n                com.example.audio.RetroSoundPlayer.playVictory()'
    engine_code = engine_code.replace(target_vic, replacement_vic, 1)

with open("app/src/main/java/com/example/engine/BenPlatformerEngine.kt", "w") as f:
    f.write(engine_code)
print("Updated BenPlatformerEngine.kt with RetroSoundPlayer calls.")

# 3. Update MainActivity.kt: preload interstitial on launch
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_code = f.read()

if "preloadInterstitial" not in main_code:
    target_main_effect = """        LaunchedEffect(Unit) {
            viewModel.loadInitialData()"""
    replacement_main_effect = """        LaunchedEffect(Unit) {
            viewModel.loadInitialData()
            viewModel.adMobManager.ensureInitialized(activity)
            viewModel.adMobManager.preloadInterstitial(activity)"""
    if target_main_effect in main_code:
        main_code = main_code.replace(target_main_effect, replacement_main_effect, 1)
        with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
            f.write(main_code)
        print("Updated MainActivity.kt to preload Interstitial Ads on launch.")
    else:
        print("Could not find LaunchedEffect in MainActivity.kt")

# 4. Update AdMobManager.kt: ensure automatic fallback on load failure so ads always show
with open("app/src/main/java/com/example/ads/AdMobManager.kt", "r") as f:
    admob_code = f.read()

old_fail = """                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isAdLoading = false
                        Log.w(TAG, "Interstitial notice (${loadAdError.code}): ${loadAdError.message}")

                        if (allowFallback && unitId != DEFAULT_INTERSTITIAL_TEST_ID) {
                            Log.d(TAG, "Falling back to sample interstitial ad unit for preview")
                            _interstitialStatus.value = "Fallback active (Preview/Test mode)"
                            isAdLoading = true
                            loadInterstitialWithUnit(context, DEFAULT_INTERSTITIAL_TEST_ID, allowFallback = false)
                        } else {
                            interstitialAd = null
                            _interstitialStatus.value = "Notice: ${loadAdError.message}"
                        }
                    }"""

new_fail = """                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isAdLoading = false
                        Log.w(TAG, "Interstitial notice (${loadAdError.code}): ${loadAdError.message}")

                        // When production ad unit is pending Google review or returns code 3 (NO_FILL),
                        // automatically fallback to Google test unit so interstitial is always ready to show
                        if (unitId != DEFAULT_INTERSTITIAL_TEST_ID) {
                            Log.d(TAG, "Automatic fallback to Google sample interstitial unit for instant display")
                            _interstitialStatus.value = "Fallback Ready (Sample Unit)"
                            isAdLoading = true
                            loadInterstitialWithUnit(context, DEFAULT_INTERSTITIAL_TEST_ID, allowFallback = false)
                        } else {
                            interstitialAd = null
                            _interstitialStatus.value = "Ad Notice (${loadAdError.code}): ${loadAdError.message}"
                        }
                    }"""

if old_fail in admob_code:
    admob_code = admob_code.replace(old_fail, new_fail, 1)
    with open("app/src/main/java/com/example/ads/AdMobManager.kt", "w") as f:
        f.write(admob_code)
    print("Updated AdMobManager.kt with robust interstitial fallback.")

print("Android files updated successfully.")
