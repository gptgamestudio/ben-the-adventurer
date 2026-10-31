package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdventureControlsOverlay(
    onLeftChange: (Boolean) -> Unit,
    onRightChange: (Boolean) -> Unit,
    onJumpChange: (Boolean) -> Unit,
    onRunChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // --- BOTTOM-LEFT: COMPACT CIRCULAR D-PAD CONTROLLER WITH 'ATRAS' ON TOP & 'ABANTE' AT BOTTOM ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(126.dp)
                .clip(CircleShape)
                .background(Color(0xE60F172A))
                .border(2.5.dp, Color(0xFF38BDF8), CircleShape)
                .testTag("compact_circular_dpad_controller"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Text: 'ATRAS'
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ATRAS",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Center Directional Touch Arrows: ◀ ATRAS (Left) and ABANTE (Right) ▶
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Arrow (Atras movement)
                    DpadDirectionButton(
                        symbol = "◀",
                        testTag = "button_dpad_atras",
                        onPressChange = onLeftChange
                    )

                    // Center D-Pad tactile center hub
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF334155), CircleShape)
                            .border(1.dp, Color(0xFF64748B), CircleShape)
                    )

                    // Right Arrow (Abante movement)
                    DpadDirectionButton(
                        symbol = "▶",
                        testTag = "button_dpad_abante",
                        onPressChange = onRightChange
                    )
                }

                // Bottom Footer Text: 'ABANTE'
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABANTE",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // --- BOTTOM-RIGHT ACTION BUTTONS: RUN (B) & JUMP (A) (ORIGINAL SIZES & POSITIONS) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .testTag("action_controls_container"),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DASH / RUN (B)
            HoldableActionButton(
                symbol = "⚡",
                label = "RUN (B)",
                testTag = "button_action_run",
                baseColor = Color(0xFF1E293B),
                activeColor = Color(0xFFD97706),
                borderColor = Color(0xFFFBBF24),
                onPressChange = onRunChange
            )

            // JUMP (A)
            HoldableActionButton(
                symbol = "⬆",
                label = "JUMP (A)",
                testTag = "button_action_jump",
                baseColor = Color(0xFF1E293B),
                activeColor = Color(0xFF15803D),
                borderColor = Color(0xFF4ADE80),
                onPressChange = onJumpChange
            )
        }
    }
}

@Composable
private fun DpadDirectionButton(
    symbol: String,
    testTag: String,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(44.dp)
            .testTag(testTag)
            .background(
                if (isPressed) Color(0xFF0284C7) else Color(0xFF1E293B),
                CircleShape
            )
            .border(
                2.dp,
                if (isPressed) Color.White else Color(0xFF38BDF8),
                CircleShape
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    onPressChange(true)

                    waitForUpOrCancellation()
                    isPressed = false
                    onPressChange(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun HoldableActionButton(
    symbol: String,
    label: String,
    testTag: String,
    baseColor: Color,
    activeColor: Color,
    borderColor: Color,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(68.dp)
            .testTag(testTag)
            .background(
                if (isPressed) activeColor else baseColor,
                CircleShape
            )
            .border(
                2.5.dp,
                if (isPressed) Color.White else borderColor,
                CircleShape
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    onPressChange(true)

                    waitForUpOrCancellation()
                    isPressed = false
                    onPressChange(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                color = if (isPressed) Color.White else Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
