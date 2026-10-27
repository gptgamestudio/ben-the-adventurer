package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PrincessRescueCelebrationDialog(
    score: Int,
    coins: Int,
    levelPlayTimeSeconds: Float,
    isOneHourAchieved: Boolean,
    onContinueNextIsland: () -> Unit,
    onReturnToDashboard: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFFDE047), RoundedCornerShape(20.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "👑 PRINCESS CLARA RESCUED!",
                    color = Color(0xFFFDE047),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Tagumpay ang ekspedisyon! Nailigtas mo si Princess Clara!",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "• Victory Score: $score pts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "• Total Coins: $coins 🪙", color = Color(0xFFFDE047), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val minutes = (levelPlayTimeSeconds / 60).toInt()
                    val seconds = (levelPlayTimeSeconds % 60).toInt()
                    Text(text = "• Island Clear Time: ${minutes}m ${seconds}s", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (isOneHourAchieved) {
                        Text(
                            text = "⭐ 1-HOUR SURVIVOR BADGE UNLOCKED! (+5000 pts bonus)",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReturnToDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_victory_dashboard_button")
                    ) {
                        Text("DASHBOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onContinueNextIsland,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).testTag("dialog_continue_button")
                    ) {
                        Text("NEXT EXPEDITION", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
