package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBannerView
import com.example.ads.AdMobManager
import com.example.data.models.AdventurerProfile
import com.example.data.models.GameMateSync
import com.example.domain.model.WorldTheme

@Composable
fun DashboardScreen(
    profile: AdventurerProfile?,
    gameMate: GameMateSync?,
    isCloudSynced: Boolean,
    isSyncing: Boolean,
    syncMessage: String?,
    adMobManager: AdMobManager,
    onEmbark: (WorldTheme) -> Unit,
    onOpenGameMate: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWorld by remember { mutableStateOf(WorldTheme.WORLD_1_TROPICAL_COAST) }
    val worlds = WorldTheme.values()
    val bannerId by adMobManager.bannerId.collectAsState()
    val bannerEnabled by adMobManager.bannerEnabled.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
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
            // HEADER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BEN THE ADVENTURER: ISLAND RESCUE",
                        color = Color(0xFFFDE047),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isCloudSynced) "☁️ CLOUD SYNCED (Firestore Active)" else "📱 LOCAL PROFILE (Sign in to sync)",
                        color = if (isCloudSynced) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenGameMate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_gamemate_button")
                    ) {
                        Text("🎮 GAME MATE & ADS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_signout_button")
                    ) {
                        Text("LOGOUT", fontSize = 10.sp)
                    }
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

            // STATS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(title = "TOTAL SCORE", value = "${profile?.totalScore ?: 0} pts", color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                StatCard(title = "TOTAL COINS", value = "${profile?.coins ?: 0} 🪙", color = Color(0xFFFDE047), modifier = Modifier.weight(1f))
                StatCard(title = "MAX DISTANCE", value = "${profile?.highestDistanceMeters ?: 0} m", color = Color(0xFF4ADE80), modifier = Modifier.weight(1f))
                StatCard(title = "CLARA RESCUED", value = "${profile?.princessClaraRescues ?: 0} 👑", color = Color(0xFFF43F5E), modifier = Modifier.weight(1f))
            }

            // GAME MATE BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x55065F46), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(14.dp))
                    .clickable { onOpenGameMate() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐵", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COMPANION: ${gameMate?.companionName ?: "Dex the Monkey"} (Lv. ${gameMate?.companionLevel ?: 1})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Active Cloud Perk: ${gameMate?.companionBonus ?: "+10% Coin Magnet & Speed Booster"}",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "MANAGE COMPANION ➔",
                        color = Color(0xFFFEF08A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // HERO SHOWCASE BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0F766E), Color(0xFF15803D))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "ISLAND EXPEDITION: RESCUE CLARA 👑",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "1-Hour Real-Time Survival • Mario-Inertia Physics • Firestore Cloud Sync",
                            color = Color(0xFF7DD3FC),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // WORLD THEME SELECTOR
            Text(
                text = "PUMILI NG ISLA (SELECT EXPEDITION THEME):",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(worlds) { world ->
                    val isSelected = world == selectedWorld
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .background(
                                if (isSelected) Color(world.primaryColorHex) else Color(0xFF1E293B),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                2.dp,
                                if (isSelected) Color(world.accentColorHex) else Color(0xFF334155),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedWorld = world }
                            .padding(12.dp)
                            .testTag("world_select_${world.name}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = world.title,
                                color = if (isSelected) Color.White else Color(world.accentColorHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = world.subtitle,
                                color = if (isSelected) Color(0xFFF1F5F9) else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Target: ${world.targetDistanceMeters}m",
                                color = if (isSelected) Color(0xFFFEF08A) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // EMBARK BUTTON & DETAILS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedWorld.title,
                            color = Color(selectedWorld.accentColorHex),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = selectedWorld.description,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { onEmbark(selectedWorld) },
                        modifier = Modifier
                            .height(52.dp)
                            .width(200.dp)
                            .testTag("start_adventure_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(selectedWorld.primaryColorHex))
                    ) {
                        Text("🚀 EMBARK EXPEDITION", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // FUNCTIONAL ANDROID NATIVE APK DOWNLOAD BUTTON (MATCHING REFERENCE UI)
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            Button(
                onClick = {
                    try {
                        uriHandler.openUri("https://gptgamestudio.github.io/ben-the-adventurer/BenTheAdventurer.apk")
                    } catch (e: Exception) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("download_native_apk_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF064E3B)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981))
            ) {
                Text(
                    text = "📲 Download Android Native APK (25MB)",
                    color = Color(0xFF4ADE80),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // BOTTOM BANNER AD CONTAINER
            AdMobBannerView(
                adUnitId = bannerId,
                isEnabled = bannerEnabled,
                onStatusChange = { adMobManager.updateBannerStatus(it) }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}
