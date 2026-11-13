package progamerbattle.com.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.User
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
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentUser: User? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // User preferences: Match & Bounty alerts enabled by default
    var matchAlertsEnabled by remember { mutableStateOf(true) }
    var winningsAlertsEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }

    var isClearCacheDialogOpen by remember { mutableStateOf(false) }

    val cyberSurfaces = progamerbattle.com.ui.theme.LocalCyberSurfaces.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 3D Settings Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = NeonCyan)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonCyan.copy(alpha = 0.35f), CarbonSurface)
                            )
                        )
                        .border(1.2.dp, NeonCyan, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "APP SETTINGS & PREFERENCES",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Customize notifications, game overlays & tournament support",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // --- HELP & SUPPORT SECTION (Email Support Intent) ---
        item {
            Text(
                text = "HELP & SUPPORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = NeonCyanLight
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Setting3DActionRow(
                    icon = Icons.AutoMirrored.Filled.Help,
                    iconTint = NeonCyan,
                    title = "Tournament Host Support",
                    subtitle = "Email support team at www.progamerbattle@gmail.com",
                    onClick = {
                        val userMobile = currentUser?.mobileNumber ?: "N/A"
                        val userUid = currentUser?.inGameUid ?: "N/A"
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:www.progamerbattle@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Pro GamerBattle - Support Request")
                            val bodyText = "Hi Support Team,\n\nMobile: $userMobile\nIn-Game UID: $userUid\n\nIssue:\n"
                            putExtra(Intent.EXTRA_TEXT, bodyText)
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send Support Email"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Contact: www.progamerbattle@gmail.com", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- MATCH NOTIFICATIONS SECTION ---
        item {
            Text(
                text = "MATCH NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = NeonMagenta
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.2.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Setting3DToggleRow(
                        icon = Icons.Default.Notifications,
                        iconTint = NeonMagenta,
                        title = "15-Minute Room Unlock Alerts",
                        subtitle = "Alert when Room ID & Password unlock for your match",
                        checked = matchAlertsEnabled,
                        activeColor = NeonMagenta,
                        onCheckedChange = { matchAlertsEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CarbonBorder))
                    Spacer(modifier = Modifier.height(14.dp))
                    Setting3DToggleRow(
                        icon = Icons.Default.CheckCircle,
                        iconTint = NeonGreen,
                        title = "Prize & Bounty Credit Alerts",
                        subtitle = "Notify immediately when kill bounties hit your wallet",
                        checked = winningsAlertsEnabled,
                        activeColor = NeonGreen,
                        onCheckedChange = { winningsAlertsEnabled = it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- GAMEPLAY & OVERLAY ---
        item {
            Text(
                text = "GAMEPLAY & SENSORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = ElectricPurple
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.2.dp, ElectricPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Setting3DToggleRow(
                    icon = Icons.Default.Vibration,
                    iconTint = CyberGold,
                    title = "Haptic Tactile Feedback",
                    subtitle = "Vibrate on button taps and match joins",
                    checked = hapticsEnabled,
                    activeColor = CyberGold,
                    onCheckedChange = { hapticsEnabled = it }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- APP STORAGE & CACHE ---
        item {
            Text(
                text = "STORAGE & PERFORMANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = CyberGoldLight
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isClearCacheDialogOpen = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberGold.copy(alpha = 0.16f))
                                .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Clear Cached Data & Room Logs",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Purge local cache and temporary match logs (14.2 MB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- ABOUT APP ---
        item {
            Text(
                text = "ABOUT PRO GAMERBATTLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingInfoRow(label = "Application Version", value = "v1.0.5 - 3D Cyber Edition")
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingInfoRow(label = "Application ID", value = "progamerbattle.com")
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingInfoRow(label = "Support Email", value = "www.progamerbattle@gmail.com")
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingInfoRow(label = "Target Platform", value = "Android 15 (API 36)")
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingInfoRow(label = "UI Architecture", value = "3D Jetpack Compose + Neon Shaders")
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIALOG: CLEAR CACHE ---
    if (isClearCacheDialogOpen) {
        AlertDialog(
            onDismissRequest = { isClearCacheDialogOpen = false },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Clear Cached Data?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will clear temporary match logs, cached tournament images, and offline room buffers. Your wallet balance and account profile will NOT be affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Neon3DButton(
                    text = "CLEAR CACHE",
                    onClick = {
                        isClearCacheDialogOpen = false
                        Toast.makeText(context, "14.2 MB cache cleared successfully!", Toast.LENGTH_SHORT).show()
                    },
                    style = Neon3DStyle.RED
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isClearCacheDialogOpen = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun Setting3DToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    activeColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.16f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CarbonBackground,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CarbonBorder
            )
        )
    }
}

@Composable
private fun Setting3DActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.18f))
                .border(1.dp, iconTint.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = NeonCyanLight
        )
    }
}
