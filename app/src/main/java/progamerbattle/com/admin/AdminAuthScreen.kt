package progamerbattle.com.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.R
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun AdminAuthScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val state by adminViewModel.uiState.collectAsState()
    var isPinVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 3D Admin Console Emblem Logo
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = CyberGold, spotColor = Color.Black)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.linearGradient(listOf(CyberGold, NeonCyan)), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_admin_3d),
                contentDescription = "Admin 3D Gaming Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TOURNAMENT ADMIN PORTAL",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Enter 6-digit Master PIN to unlock organizer controls",
            style = MaterialTheme.typography.bodySmall,
            color = CyberGoldLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 6-Digit Individual Glowing Boxes (Clickable to focus text field)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { focusRequester.requestFocus() }
                .padding(vertical = 4.dp)
                .testTag("admin_pin_boxes_row")
        ) {
            for (i in 0 until 6) {
                val hasDigit = i < state.adminPinInput.length
                val char = if (hasDigit) {
                    if (isPinVisible) state.adminPinInput[i].toString() else "•"
                } else ""
                val isCurrentFocus = i == state.adminPinInput.length

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hasDigit) Color(0xFF1F1A08) else CarbonSurface)
                        .border(
                            width = if (isCurrentFocus) 2.dp else 1.2.dp,
                            color = if (hasDigit || isCurrentFocus) CyberGold else CarbonBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .shadow(if (hasDigit) 4.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = CyberGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = if (isPinVisible) 20.sp else 28.sp
                        ),
                        color = CyberGoldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Text Input Field (Clickable for Soft Keyboard)
        OutlinedTextField(
            value = state.adminPinInput,
            onValueChange = { adminViewModel.onPinInputChanged(it) },
            label = { Text("Click here to type 6-Digit PIN", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = CyberGold)
            },
            trailingIcon = {
                IconButton(onClick = { isPinVisible = !isPinVisible }) {
                    Icon(
                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle PIN Visibility",
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { adminViewModel.verifyPin() }
            ),
            singleLine = true,
            isError = state.authError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberGold,
                unfocusedBorderColor = CarbonBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CarbonSurface,
                unfocusedContainerColor = CarbonSurface,
                errorBorderColor = DangerRed
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .focusRequester(focusRequester)
                .testTag("admin_pin_text_input")
        )

        if (state.authError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.authError ?: "",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = DangerRed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Neon3DButton(
            text = "UNLOCK ADMIN CONSOLE",
            onClick = { adminViewModel.verifyPin() },
            style = Neon3DStyle.GOLD,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("admin_login_submit_button")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "— OR USE ON-SCREEN KEYPAD —",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            ),
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // On-Screen Keypad for physical/touch convenience
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLR", "0", "DEL")
            )

            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (key in row) {
                        AdminKeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "CLR" -> adminViewModel.onPinInputChanged("")
                                    "DEL" -> {
                                        if (state.adminPinInput.isNotEmpty()) {
                                            adminViewModel.onPinInputChanged(state.adminPinInput.dropLast(1))
                                        }
                                    }
                                    else -> adminViewModel.onPinInputChanged(state.adminPinInput + key)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdminKeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val isAction = label == "CLR" || label == "DEL"

    Box(
        modifier = Modifier
            .size(68.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isAction) CarbonSurface else CarbonCard)
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("admin_key_$label"),
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Backspace",
                tint = DangerRed,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isAction) 13.sp else 22.sp
                ),
                color = if (label == "CLR") CyberGold else TextPrimary
            )
        }
    }
}
