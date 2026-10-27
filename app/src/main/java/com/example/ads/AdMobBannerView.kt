package com.example.ads

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBannerView(
    adUnitId: String,
    isEnabled: Boolean,
    onStatusChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isEnabled) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0x33000000), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob Banner is currently disabled in Mate",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
        return
    }

    var currentUnitId by remember(adUnitId) { mutableStateOf(adUnitId) }
    var showBackupPlaceholder by remember { mutableStateOf(false) }
    var isReadyToLoad by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        isReadyToLoad = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(8.dp))
            .testTag("admob_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        if (!isReadyToLoad || showBackupPlaceholder) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🏆 SPONSORED: Ben The Adventurer 1,000 Levels Season Pass",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AD • READY",
                    color = Color(0xFF10B981),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            key(currentUnitId) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    factory = { context: Context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            setAdUnitId(currentUnitId)
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    showBackupPlaceholder = false
                                    onStatusChange("Banner Loaded Successfully ✅")
                                    Log.d("AdMobBannerView", "Banner ad loaded ($currentUnitId)")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    onStatusChange("Banner Notice: ${error.message}")
                                    Log.w("AdMobBannerView", "Banner notice (${error.code}): ${error.message}")

                                    // If production ad unit failed (e.g. error 9 Publisher Data Not Found in emulator/testing),
                                    // fall back cleanly to official Google test sample ad unit
                                    if (currentUnitId != AdMobManager.DEFAULT_BANNER_TEST_ID) {
                                        Log.d("AdMobBannerView", "Switching to Google sample banner test unit")
                                        currentUnitId = AdMobManager.DEFAULT_BANNER_TEST_ID
                                    } else {
                                        showBackupPlaceholder = true
                                    }
                                }

                                override fun onAdOpened() {
                                    super.onAdOpened()
                                    onStatusChange("Banner Ad Opened")
                                }
                            }
                            try {
                                loadAd(AdRequest.Builder().build())
                            } catch (e: Exception) {
                                Log.w("AdMobBannerView", "Banner ad load notice: ${e.message}")
                                showBackupPlaceholder = true
                            }
                        }
                    }
                )
            }
        }
    }
}
