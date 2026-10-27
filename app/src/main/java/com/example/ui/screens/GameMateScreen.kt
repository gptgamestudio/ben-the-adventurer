package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBannerView
import com.example.ads.AdMobManager
import com.example.data.models.GameMateSync
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GameMateScreen(
    gameMate: GameMateSync?,
    isSyncing: Boolean,
    syncMessage: String?,
    adMobManager: AdMobManager,
    onUpgradeMate: () -> Unit,
    onForceSync: () -> Unit,
    onBack: () -> Unit,
    onShowInterstitialTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bannerId by adMobManager.bannerId.collectAsState()
    val interstitialId by adMobManager.interstitialId.collectAsState()
    val bannerEnabled by adMobManager.bannerEnabled.collectAsState()
    val interstitialEnabled by adMobManager.interstitialEnabled.collectAsState()
    val isTestMode by adMobManager.isTestMode.collectAsState()
    val bannerStatus by adMobManager.bannerStatus.collectAsState()
    val interstitialStatus by adMobManager.interstitialStatus.collectAsState()
    val appAdsTxtState by adMobManager.appAdsTxtState.collectAsState()

    var inputBannerId by remember(bannerId) { mutableStateOf(bannerId) }
    var inputInterstitialId by remember(interstitialId) { mutableStateOf(interstitialId) }
    var inputTestMode by remember(isTestMode) { mutableStateOf(isTestMode) }
    var inputBannerEnabled by remember(bannerEnabled) { mutableStateOf(bannerEnabled) }
    var inputInterstitialEnabled by remember(interstitialEnabled) { mutableStateOf(interstitialEnabled) }
    var adSaveMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    androidx.activity.compose.BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF064E3B), Color(0xFF0F172A))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF0284C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐵", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GAME MATE & ADMOB CHANGER",
                            color = Color(0xFFFDE047),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Production Ads • app-ads.txt Suite • Dex Companion",
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("gamemate_back_button")
                ) {
                    Text("◀ DASHBOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (syncMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x3310B981), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = syncMessage, color = Color(0xFFA7F3D0), fontSize = 11.sp)
                }
            }

            if (adSaveMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x3338BDF8), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = adSaveMessage ?: "", color = Color(0xFF7DD3FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 1. ADMOB CHANGER MATE SECTION (BANNER & INTERSTITIAL PRODUCTION)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x990F172A), RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ADMOB CHANGER MATE",
                                    color = Color(0xFFFDE047),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Banner & Interstitial Ads Only (Strictly No Rewarded Ads)",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (inputTestMode) Color(0xFF0284C7) else Color(0xFF16A34A),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (inputTestMode) "🧪 GOOGLE TEST ADS" else "🚀 LIVE PRODUCTION ADS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !inputTestMode && inputBannerId.contains(AdMobManager.OFFICIAL_PUBLISHER_ID),
                            onClick = {
                                inputTestMode = false
                                inputBannerId = AdMobManager.DEFAULT_BANNER_PROD_ID
                                inputInterstitialId = AdMobManager.DEFAULT_INTERSTITIAL_PROD_ID
                                adSaveMessage = "Gamit na ang opisyal mong Publisher ID (${AdMobManager.OFFICIAL_PUBLISHER_ID})!"
                            },
                            label = { Text("👑 My Pub ID", fontSize = 10.sp) },
                            modifier = Modifier.testTag("toggle_pub_id_chip")
                        )

                        FilterChip(
                            selected = inputTestMode,
                            onClick = {
                                inputTestMode = true
                                inputBannerId = AdMobManager.DEFAULT_BANNER_TEST_ID
                                inputInterstitialId = AdMobManager.DEFAULT_INTERSTITIAL_TEST_ID
                                adSaveMessage = "Gamit ang Official Google Test IDs."
                            },
                            label = { Text("🧪 Test IDs", fontSize = 10.sp) },
                            modifier = Modifier.testTag("toggle_test_mode_chip")
                        )

                        FilterChip(
                            selected = inputBannerEnabled,
                            onClick = { inputBannerEnabled = !inputBannerEnabled },
                            label = { Text("Banner: ${if (inputBannerEnabled) "ON" else "OFF"}", fontSize = 10.sp) },
                            modifier = Modifier.testTag("toggle_banner_chip")
                        )

                        FilterChip(
                            selected = inputInterstitialEnabled,
                            onClick = { inputInterstitialEnabled = !inputInterstitialEnabled },
                            label = { Text("Inter: ${if (inputInterstitialEnabled) "ON" else "OFF"}", fontSize = 10.sp) },
                            modifier = Modifier.testTag("toggle_interstitial_chip")
                        )
                    }

                    OutlinedTextField(
                        value = inputBannerId,
                        onValueChange = { inputBannerId = it },
                        label = { Text("Banner Ad Unit ID", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admob_banner_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        trailingIcon = {
                            TextButton(onClick = {
                                inputBannerId = AdMobManager.DEFAULT_BANNER_PROD_ID
                            }) {
                                Text("Real ID", fontSize = 9.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    )
                    Text(
                        text = "Banner Status: $bannerStatus",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )

                    OutlinedTextField(
                        value = inputInterstitialId,
                        onValueChange = { inputInterstitialId = it },
                        label = { Text("Interstitial Ad Unit ID", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admob_interstitial_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        trailingIcon = {
                            TextButton(onClick = {
                                inputInterstitialId = AdMobManager.DEFAULT_INTERSTITIAL_PROD_ID
                            }) {
                                Text("Real ID", fontSize = 9.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    )
                    Text(
                        text = "Interstitial Status: $interstitialStatus",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                adMobManager.saveAdMobConfig(
                                    newBannerId = inputBannerId,
                                    newInterstitialId = inputInterstitialId,
                                    testMode = inputTestMode,
                                    bannerOn = inputBannerEnabled,
                                    interstitialOn = inputInterstitialEnabled
                                )
                                adSaveMessage = "✅ AdMob IDs updated! Changes applied immediately."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("save_admob_config_button")
                        ) {
                            Text("💾 SAVE & APPLY IDS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                inputBannerId = AdMobManager.DEFAULT_BANNER_PROD_ID
                                inputInterstitialId = AdMobManager.DEFAULT_INTERSTITIAL_PROD_ID
                                inputTestMode = false
                                inputBannerEnabled = true
                                inputInterstitialEnabled = true
                                adMobManager.applyProductionIds()
                                adSaveMessage = "🚀 Reset to Production Pub IDs (${AdMobManager.OFFICIAL_PUBLISHER_ID})!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("reset_admob_defaults_button")
                        ) {
                            Text("⚡ REAL DEFAULTS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                adMobManager.saveAdMobConfig(
                                    newBannerId = inputBannerId,
                                    newInterstitialId = inputInterstitialId,
                                    testMode = inputTestMode,
                                    bannerOn = inputBannerEnabled,
                                    interstitialOn = inputInterstitialEnabled
                                )
                                adMobManager.preloadInterstitial(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("preload_interstitial_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⏳ Preload Interstitial", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }

                        Button(
                            onClick = onShowInterstitialTest,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .testTag("show_interstitial_test_button")
                        ) {
                            Text("🎬 Show Interstitial Test", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Text(
                        text = "LIVE BANNER AD PREVIEW:",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AdMobBannerView(
                        adUnitId = bannerId,
                        isEnabled = bannerEnabled,
                        onStatusChange = { adMobManager.updateBannerStatus(it) }
                    )
                }
            }

            // 2. OFFICIAL APP-ADS.TXT MONETIZATION SUITE (100% WORKING VERIFICATION)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x990F172A), RoundedCornerShape(18.dp))
                    .border(
                        1.5.dp,
                        if (appAdsTxtState.isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "APP-ADS.TXT MONETIZATION SUITE",
                                    color = Color(0xFF34D399),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Google AdMob & IAB Tech Lab Monetization",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (appAdsTxtState.isValid) Color(0xFF065F46) else Color(0xFF991B1B),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (appAdsTxtState.isValid) Color(0xFF34D399) else Color(0xFFF87171),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (appAdsTxtState.isValid) "✅ 100% WORKING (TRUE)" else "❌ VERIFICATION FAILED (FALSE)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Text(
                        text = "OPISYAL NA ENTRY NA ILALAGAY SA APP-ADS.TXT:",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Entry Code Snippet Box with Copy Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF020617), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appAdsTxtState.rawEntry,
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(appAdsTxtState.rawEntry))
                                    adSaveMessage = "📋 Na-kopya ang opisyal na app-ads.txt entry sa iyong clipboard!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_app_ads_txt_button")
                            ) {
                                Text("📋 KOPYAHIN", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Checklist Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "STATUS CHECKLIST (TRUE / FALSE):",
                            color = Color(0xFFFDE047),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )

                        VerificationRow(label = "1. Domain (google.com)", value = "google.com", isTrue = appAdsTxtState.domainValid)
                        VerificationRow(label = "2. Publisher ID", value = appAdsTxtState.publisherId, isTrue = appAdsTxtState.publisherIdValid)
                        VerificationRow(label = "3. Relationship (DIRECT)", value = "DIRECT", isTrue = appAdsTxtState.relationshipValid)
                        VerificationRow(label = "4. Authority Key (Google Tag)", value = appAdsTxtState.certificationAuthority, isTrue = appAdsTxtState.authorityValid)
                        VerificationRow(label = "5. APK Bundled Asset", value = "app/src/main/assets/app-ads.txt", isTrue = appAdsTxtState.assetFileExists)
                        VerificationRow(label = "6. Overall Monetization Ready", value = if (appAdsTxtState.isValid) "100% WORKING (TRUE)" else "FALSE", isTrue = appAdsTxtState.isValid)
                    }

                    // Instructions Note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x330284C7), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "💡 PAANO ITO GUMAGANA SA ADMOB MONETIZATION:",
                                color = Color(0xFF7DD3FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1. Libreng Domain: Gamitin ang iyong Firebase Hosting domain na 'https://super-tank-online.web.app/app-ads.txt' o sariling domain.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "2. Sa Google Play Console (Store Settings > Website), ilagay ang parehong website URL.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "3. Iki-crawl ito ng Google AdMob crawler sa loob ng 24 oras upang maging 'Authorized' at ma-enable ang 100% live revenue ng iyong ads.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                adMobManager.recheckAppAdsTxt()
                                adSaveMessage = "🔍 Na-verify muli ang app-ads.txt: ${appAdsTxtState.statusSummary}"
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔍 RE-VERIFY STATUS", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }

                        Button(
                            onClick = {
                                inputTestMode = false
                                inputBannerId = AdMobManager.DEFAULT_BANNER_PROD_ID
                                inputInterstitialId = AdMobManager.DEFAULT_INTERSTITIAL_PROD_ID
                                adSaveMessage = "✅ Na-apply ang iyong Publisher ID (${AdMobManager.OFFICIAL_PUBLISHER_ID}) format sa mga inputs!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).height(40.dp)
                        ) {
                            Text("⚡ GAMITIN ANG AKING PUB ID", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // 3. COMPANION PROFILE CARD (Dex the Monkey)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x88065F46), RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = gameMate?.companionName ?: "Dex the Monkey",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Companion Level: Lv. ${gameMate?.companionLevel ?: 1}",
                            color = Color(0xFFFDE047),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current Cloud Perk: ${gameMate?.companionBonus ?: "+10% Coin Magnet & Speed Booster"}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onUpgradeMate,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("upgrade_mate_button")
                    ) {
                        Text("⭐ UPGRADE MATE", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // 4. CLOUD STORAGE & FIRESTORE STATUS CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x881E293B), RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🔥 FIRESTORE REAL-TIME PERSISTENCE",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )

                    val dateStr = if (gameMate != null) {
                        SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(gameMate.updatedAt))
                    } else "Not synced yet"

                    Text(
                        text = "• Document: /game_mates/${gameMate?.userId ?: "auth_uid"}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Security Rules: Auth-Scoped (request.auth.uid == userId)",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Last Cloud Sync: $dateStr",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onForceSync,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("force_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("☁️ SYNC GAME MATE TO FIRESTORE NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationRow(label: String, value: String, isTrue: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(text = value, color = Color(0xFF94A3B8), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier
                .background(
                    if (isTrue) Color(0xFF065F46) else Color(0xFF991B1B),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isTrue) "TRUE (PASS)" else "FALSE (FAIL)",
                color = if (isTrue) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
