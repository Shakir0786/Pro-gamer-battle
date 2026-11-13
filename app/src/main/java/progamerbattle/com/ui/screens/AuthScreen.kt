package progamerbattle.com.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.R
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by authViewModel.uiState.collectAsState()
    var pinVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Trigger navigation on successful auth
    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) {
            onAuthSuccess()
        }
    }

    // Show Toast on error
    LaunchedEffect(state.loginGeneralError) {
        state.loginGeneralError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.regGeneralError) {
        state.regGeneralError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 3D Gaming Emblem Logo
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(18.dp, RoundedCornerShape(24.dp), ambientColor = NeonCyan, spotColor = Color.Black)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.linearGradient(listOf(NeonCyan, CyberGold)), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_user_3d),
                contentDescription = "Pro GamerBattle 3D Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PRO GAMERBATTLE",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Battle Gaming Tournaments • Compete & Win",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonCyanLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3D Tactile Mode Switcher (Login vs Register)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(16.dp))
                .background(CarbonSurface)
                .border(1.2.dp, CarbonBorder, RoundedCornerShape(16.dp))
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (state.isLoginMode) {
                            Modifier.background(Brush.verticalGradient(listOf(NeonCyan, Color(0xFF008FA3))))
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable { authViewModel.setAuthMode(true) }
                    .padding(vertical = 12.dp)
                    .testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LOGIN",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = if (state.isLoginMode) CarbonBackground else TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (!state.isLoginMode) {
                            Modifier.background(Brush.verticalGradient(listOf(NeonMagenta, Color(0xFFB00058))))
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable { authViewModel.setAuthMode(false) }
                    .padding(vertical = 12.dp)
                    .testTag("tab_register"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "REGISTER",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = if (!state.isLoginMode) Color.White else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Welcome Bonus Banner for New Gamers
        if (!state.isLoginMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberGold.copy(alpha = 0.16f))
                    .border(1.2.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Get ₹10 Free Entry Bonus instantly on registration!",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberGoldLight
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3D Form Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = if (state.isLoginMode) NeonCyan.copy(alpha = 0.3f) else NeonMagenta.copy(alpha = 0.3f),
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(CarbonCardElevated, CarbonCard)
                    )
                )
                .border(
                    1.2.dp,
                    if (state.isLoginMode) NeonCyan.copy(alpha = 0.4f) else NeonMagenta.copy(alpha = 0.4f),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.isLoginMode) {
                    // --- LOGIN FORM ---
                    Text(
                        text = "MOBILE LOGIN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Mobile Number
                    OutlinedTextField(
                        value = state.loginMobile,
                        onValueChange = { authViewModel.onLoginMobileChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_mobile_input"),
                        label = { Text("10-Digit Mobile Number") },
                        prefix = { Text("+91  ", color = TextSecondary, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = state.loginMobileError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.loginMobileError != null) {
                        Text(
                            text = state.loginMobileError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secret PIN
                    OutlinedTextField(
                        value = state.loginPin,
                        onValueChange = { authViewModel.onLoginPinChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_pin_input"),
                        label = { Text("6-Digit Secret PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle PIN",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.loginPinError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.loginPinError != null) {
                        Text(
                            text = state.loginPinError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    if (state.loginGeneralError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.loginGeneralError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = DangerRed,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3D Login Button
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    } else {
                        Neon3DButton(
                            text = "LOGIN TO BATTLE",
                            onClick = { authViewModel.login() },
                            style = Neon3DStyle.CYAN,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_submit_button")
                        )
                    }


                } else {
                    // --- REGISTER FORM ---
                    Text(
                        text = "CREATE GAMER PROFILE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = NeonMagenta
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name
                    OutlinedTextField(
                        value = state.regFullName,
                        onValueChange = { authViewModel.onRegFullNameChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_fullname_input"),
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NeonMagenta)
                        },
                        isError = state.regFullNameError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.regFullNameError != null) {
                        Text(
                            text = state.regFullNameError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 10-Digit Mobile
                    OutlinedTextField(
                        value = state.regMobile,
                        onValueChange = { authViewModel.onRegMobileChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_mobile_input"),
                        label = { Text("10-Digit Mobile Number") },
                        prefix = { Text("+91  ", color = TextSecondary, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NeonMagenta)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = state.regMobileError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.regMobileError != null) {
                        Text(
                            text = state.regMobileError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player IGN
                    OutlinedTextField(
                        value = state.regIgn,
                        onValueChange = { authViewModel.onRegIgnChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_ign_input"),
                        label = { Text("Player In-Game Name (IGN)") },
                        leadingIcon = {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonMagenta)
                        },
                        isError = state.regIgnError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.regIgnError != null) {
                        Text(
                            text = state.regIgnError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Game UID
                    OutlinedTextField(
                        value = state.regInGameUid,
                        onValueChange = { authViewModel.onRegInGameUidChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_uid_input"),
                        label = { Text("Player In-Game UID (e.g. 849201948)") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = NeonMagenta)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.regInGameUidError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.regInGameUidError != null) {
                        Text(
                            text = state.regInGameUidError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6-Digit PIN
                    OutlinedTextField(
                        value = state.regPin,
                        onValueChange = { authViewModel.onRegPinChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pin_input"),
                        label = { Text("Set 6-Digit Secret PIN") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonMagenta)
                        },
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle PIN",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.regPinError != null,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (state.regPinError != null) {
                        Text(
                            text = state.regPinError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Referral Code (Optional)
                    OutlinedTextField(
                        value = state.regReferralCode,
                        onValueChange = { authViewModel.onRegReferralCodeChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_referral_input"),
                        label = { Text("Referral Code (Optional - Win ₹10)") },
                        leadingIcon = {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = CyberGold)
                        },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "Enter a friend's code to link reward (Fair play verified)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )

                    if (state.regGeneralError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.regGeneralError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3D Register Button
                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonMagenta,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    } else {
                        Neon3DButton(
                            text = "REGISTER & CLAIM ₹10 BONUS",
                            onClick = { authViewModel.register() },
                            style = Neon3DStyle.MAGENTA,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_submit_button")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "By continuing, you agree to Fair Play Esports Policy and Anti-Hack Terms.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CarbonBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CarbonBackground,
    unfocusedContainerColor = CarbonBackground,
    errorBorderColor = DangerRed
)
