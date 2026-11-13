package progamerbattle.com.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun AdminSettingsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val state by adminViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "APP CONTROLS & REMOTE CONFIG",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = CyberGold
                )
                Text(
                    text = "Live master controls: Maintenance mode, announcements, update link & support",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // ==========================================
        // 1. MAINTENANCE MODE SWITCH CARD (3D DESIGN)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = if (state.maintenanceModeEnabled) DangerRed else NeonGreen)
                    .border(
                        1.2.dp,
                        if (state.maintenanceModeEnabled) DangerRed.copy(alpha = 0.8f) else CarbonBorder,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (state.maintenanceModeEnabled) DangerRed.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Construction,
                                    contentDescription = null,
                                    tint = if (state.maintenanceModeEnabled) DangerRed else NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Maintenance Mode",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (state.maintenanceModeEnabled) "🔴 ACTIVE (Players locked out)" else "🟢 DISABLED (App is live)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = if (state.maintenanceModeEnabled) DangerRed else NeonGreen
                                )
                            }
                        }

                        Switch(
                            checked = state.maintenanceModeEnabled,
                            onCheckedChange = { adminViewModel.onToggleMaintenanceMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DangerRed,
                                checkedTrackColor = DangerRed.copy(alpha = 0.35f),
                                uncheckedThumbColor = NeonGreen,
                                uncheckedTrackColor = NeonGreen.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag("admin_maintenance_switch")
                        )
                    }

                    OutlinedTextField(
                        value = state.maintenanceMessageInput,
                        onValueChange = { adminViewModel.onMaintenanceMessageChanged(it) },
                        label = { Text("Maintenance Notice for Players") },
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Neon3DButton(
                        text = "SAVE MAINTENANCE NOTICE",
                        onClick = { adminViewModel.onSaveMaintenanceSettings() },
                        style = if (state.maintenanceModeEnabled) Neon3DStyle.RED else Neon3DStyle.CYAN,
                        modifier = Modifier.fillMaxWidth().testTag("admin_save_maintenance_button")
                    )
                }
            }
        }

        // ==========================================
        // 2. BROADCAST NOTIFICATION & ANNOUNCEMENT
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = NeonCyan)
                    .border(1.2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Push Announcement / Notice",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Text(
                                text = "Broadcasts alert banner to all players in real time",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.announcementMessageInput,
                        onValueChange = { adminViewModel.onAnnouncementMessageChanged(it) },
                        label = { Text("Announcement Message (e.g. Mega Tournament at 9 PM!)") },
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_announcement_input")
                    )

                    Neon3DButton(
                        text = "BROADCAST ANNOUNCEMENT",
                        onClick = { adminViewModel.onBroadcastAnnouncement() },
                        style = Neon3DStyle.CYAN,
                        modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_button")
                    )
                }
            }
        }

        // ==========================================
        // 3. APP UPDATE CONTROLLER & LINK SEND
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = CyberGold)
                    .border(1.2.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Version & Update Link",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Text(
                                text = "Send APK download URL & enforce minimum version",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.minVersionInput,
                        onValueChange = { adminViewModel.onMinVersionChanged(it) },
                        label = { Text("Minimum Version Required (e.g. 1.0 or 2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_min_version_input")
                    )

                    OutlinedTextField(
                        value = state.updateUrlInput,
                        onValueChange = { adminViewModel.onUpdateUrlChanged(it) },
                        label = { Text("Update Download URL / APK Link") },
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_update_url_input")
                    )

                    Neon3DButton(
                        text = "PUBLISH UPDATE LINK",
                        onClick = { adminViewModel.onSaveUpdateSettings() },
                        style = Neon3DStyle.GOLD,
                        modifier = Modifier.fillMaxWidth().testTag("admin_publish_update_button")
                    )
                }
            }
        }

        // ==========================================
        // 4. SOCIAL & SUPPORT LINKS (WHATSAPP & TELEGRAM)
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = NeonGreen)
                    .border(1.2.dp, CarbonBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Community & Support Links",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Text(
                                text = "WhatsApp support chat & official Telegram channel",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.whatsappLinkInput,
                        onValueChange = { adminViewModel.onWhatsappLinkChanged(it) },
                        label = { Text("WhatsApp Link (e.g. https://wa.me/message/...)") },
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_input")
                    )

                    OutlinedTextField(
                        value = state.telegramLinkInput,
                        onValueChange = { adminViewModel.onTelegramLinkChanged(it) },
                        label = { Text("Telegram Link (e.g. https://t.me/...)") },
                        colors = adminSettingFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_telegram_input")
                    )

                    Neon3DButton(
                        text = "SAVE SUPPORT LINKS",
                        onClick = { adminViewModel.onSaveSocialLinks() },
                        style = Neon3DStyle.CYAN,
                        modifier = Modifier.fillMaxWidth().testTag("admin_save_links_button")
                    )
                }
            }
        }

        // ==========================================
        // 5. FIREBASE REMOTE CONFIG SYNC TRIGGER
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CarbonCardElevated)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Firebase Remote Config",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.isFetchingRemoteConfig) "Syncing in progress..." else "Syncs with cloud parameters instantly",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }

                    Neon3DButton(
                        text = "SYNC NOW",
                        onClick = { adminViewModel.onRefreshRemoteConfig() },
                        style = Neon3DStyle.GOLD,
                        modifier = Modifier.testTag("admin_sync_firebase_button")
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun adminSettingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CarbonBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CarbonSurface,
    unfocusedContainerColor = CarbonSurface
)
