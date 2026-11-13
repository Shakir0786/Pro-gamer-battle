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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import progamerbattle.com.ui.components.Neon3DStatCube
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
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    currentUser: User?,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var editIgn by remember { mutableStateOf(currentUser?.inGameName ?: "") }
    var editUid by remember { mutableStateOf(currentUser?.inGameUid ?: "") }

    val played = currentUser?.matchesPlayed ?: 0
    val won = currentUser?.matchesWon ?: 0
    val totalKills = currentUser?.totalKills ?: 0
    val winRate = if (played > 0) ((won.toFloat() / played.toFloat()) * 100).toInt() else 0

    val cyberSurfaces = progamerbattle.com.ui.theme.LocalCyberSurfaces.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 3D Player Identity Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = ElectricPurple.copy(alpha = 0.3f),
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1F1235),
                                CarbonCardElevated,
                                CarbonCard
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(ElectricPurple, NeonCyan)
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(ElectricPurple.copy(alpha = 0.4f), CarbonSurface)
                                        )
                                    )
                                    .border(2.dp, ElectricPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = currentUser?.inGameName ?: "ProGamer",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.4.sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentUser?.fullName ?: "Esports Player",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editIgn = currentUser?.inGameName ?: ""
                                editUid = currentUser?.inGameUid ?: ""
                                isEditDialogOpen = true
                            },
                            modifier = Modifier
                                .testTag("edit_profile_button")
                                .clip(RoundedCornerShape(10.dp))
                                .background(CarbonSurface)
                                .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3D Detail Vault Rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        ProfileInfoRow(
                            icon = Icons.Default.Badge,
                            label = "Player Game UID",
                            value = currentUser?.inGameUid ?: "—",
                            valueColor = NeonCyanLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Registered Mobile",
                            value = if (!currentUser?.mobileNumber.isNullOrBlank()) "+91 ${currentUser?.mobileNumber}" else "—",
                            valueColor = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Wallet Balance",
                            value = "₹${(currentUser?.walletBalance ?: 0.0).toInt()}",
                            valueColor = CyberGoldLight
                        )
                    }
                }
            }
        }

        // 3D Esports Career Stat Cubes
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "ESPORTS CAREER STATS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = ElectricPurple
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Neon3DStatCube(
                    title = "Matches",
                    value = "$played",
                    icon = Icons.Default.MilitaryTech,
                    neonColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                Neon3DStatCube(
                    title = "Victories",
                    value = "$won",
                    icon = Icons.Default.EmojiEvents,
                    neonColor = CyberGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Neon3DStatCube(
                    title = "Total Kills",
                    value = "$totalKills",
                    icon = Icons.Default.SportsKabaddi,
                    neonColor = NeonMagenta,
                    modifier = Modifier.weight(1f)
                )
                Neon3DStatCube(
                    title = "Win Ratio",
                    value = "$winRate%",
                    icon = Icons.Default.EmojiEvents,
                    neonColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Fair Play & App Actions
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SETTINGS & POLICIES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = NeonCyanLight
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CarbonCardElevated, CarbonCard)
                        )
                    )
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    SettingActionRow(
                        icon = Icons.Default.Gavel,
                        iconTint = CyberGold,
                        title = "Fair Play & Anti-Hack Rules",
                        subtitle = "Strict zero tolerance for third-party scripts",
                        onClick = {
                            Toast.makeText(context, "All tournament custom rooms use anti-cheat spectator logs.", Toast.LENGTH_LONG).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CarbonBorder))
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingActionRow(
                        icon = Icons.AutoMirrored.Filled.Help,
                        iconTint = NeonCyan,
                        title = "Tournament Host Support",
                        subtitle = "Email support: www.progamerbattle@gmail.com",
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3D Logout Button
            Neon3DButton(
                text = "LOGOUT ACCOUNT",
                onClick = {
                    authViewModel.signOut()
                    Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                },
                style = Neon3DStyle.RED,
                icon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            )
        }
    }

    // --- DIALOG: EDIT GAME PROFILE ---
    if (isEditDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEditDialogOpen = false },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Update Game Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editIgn,
                        onValueChange = { editIgn = it },
                        label = { Text("Player IGN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonBackground,
                            unfocusedContainerColor = CarbonBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editUid,
                        onValueChange = { editUid = it.filter { c -> c.isDigit() } },
                        label = { Text("Player Game UID") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonBackground,
                            unfocusedContainerColor = CarbonBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "SAVE CHANGES",
                    onClick = {
                        if (editIgn.isNotBlank() && editUid.length >= 6) {
                            authViewModel.updateProfile(editIgn, editUid)
                            isEditDialogOpen = false
                            Toast.makeText(context, "Game Profile Updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter valid IGN and numeric UID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    style = Neon3DStyle.CYAN
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isEditDialogOpen = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
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
