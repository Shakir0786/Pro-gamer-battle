package progamerbattle.com.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.collectLatest
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.model.User
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DChip
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.components.RoomCredentialCard
import progamerbattle.com.ui.components.TournamentMatchCard
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
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.JoinMatchResult
import progamerbattle.com.viewmodel.MatchViewModel

@Composable
fun HomeScreen(
    matchViewModel: MatchViewModel,
    currentUser: User?,
    onNavigateToWallet: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    announcementMessage: String = ""
) {
    val context = LocalContext.current
    val matches by matchViewModel.filteredMatches.collectAsState()
    val selectedStatusFilter by matchViewModel.selectedStatusFilter.collectAsState()
    val survivalFilterOnly by matchViewModel.survivalFilterOnly.collectAsState()
    val forceUnlock by matchViewModel.forceUnlockCredentials.collectAsState()

    var matchToJoin by remember { mutableStateOf<TournamentMatch?>(null) }
    var activeCredentialMatch by remember { mutableStateOf<TournamentMatch?>(null) }
    var insufficientFundsMatch by remember { mutableStateOf<Pair<TournamentMatch, Double>?>(null) }

    LaunchedEffect(Unit) {
        matchViewModel.joinEvents.collectLatest { result ->
            when (result) {
                is JoinMatchResult.Success -> {
                    Toast.makeText(context, "Slot Confirmed for ${result.match.title}!", Toast.LENGTH_LONG).show()
                    matchToJoin = null
                }
                is JoinMatchResult.InsufficientBalance -> {
                    matchToJoin = null
                    insufficientFundsMatch = Pair(result.match, result.requiredAmount)
                }
                is JoinMatchResult.AlreadyJoined -> {
                    Toast.makeText(context, "You are already enrolled in this tournament!", Toast.LENGTH_SHORT).show()
                    matchToJoin = null
                }
                is JoinMatchResult.MatchFull -> {
                    Toast.makeText(context, "Sorry, all slots are filled!", Toast.LENGTH_SHORT).show()
                    matchToJoin = null
                }
                is JoinMatchResult.RegistrationClosed -> {
                    Toast.makeText(context, "Registration has closed for this tournament!", Toast.LENGTH_SHORT).show()
                    matchToJoin = null
                }
                is JoinMatchResult.NotLoggedIn -> {
                    Toast.makeText(context, "Please login first to enter matches", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val cyberSurfaces = progamerbattle.com.ui.theme.LocalCyberSurfaces.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("home_screen_tournament_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Global Remote Config Announcement Banner
        if (announcementMessage.isNotBlank()) {
            item {
                progamerbattle.com.ui.components.AnnouncementBanner(
                    message = announcementMessage,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }

        // Hero Esports 3D Tournament Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = NeonCyan.copy(alpha = 0.35f),
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF13203C),
                                CarbonCardElevated,
                                CarbonCard
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(NeonCyan, ElectricPurple, NeonMagenta)
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3D Neon Badge
                        Box(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = NeonCyan)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BATTLE GAMING ESPORTS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = CarbonBackground,
                                letterSpacing = 0.8.sp
                            )
                        }

                        // 3D Quick Balance Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CarbonBackground)
                                .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { onNavigateToWallet(null) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = CyberGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${(currentUser?.walletBalance ?: 0.0).toInt()}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = CyberGoldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Official Custom Rooms & Tournaments",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.4.sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "1 vs 1, 2 vs 2, 4 vs 4 & Battle Arenas • 1 to 48 Slots Custom Rooms",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 3D Tactile Filter Chips: All Rooms, Survival (Top 10), Per Kill Arena, Upcoming, Completed Results
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Neon3DChip(
                    text = "All Rooms",
                    selected = selectedStatusFilter == null && survivalFilterOnly == null,
                    activeColor = NeonCyan,
                    onClick = {
                        matchViewModel.setStatusFilter(null)
                        matchViewModel.setSurvivalFilter(null)
                    }
                )
                Neon3DChip(
                    text = "Survival (Top 10)",
                    selected = survivalFilterOnly == true,
                    activeColor = CyberGold,
                    onClick = {
                        matchViewModel.setStatusFilter(null)
                        matchViewModel.setSurvivalFilter(if (survivalFilterOnly == true) null else true)
                    }
                )
                Neon3DChip(
                    text = "Per Kill Arena",
                    selected = survivalFilterOnly == false,
                    activeColor = NeonMagenta,
                    onClick = {
                        matchViewModel.setStatusFilter(null)
                        matchViewModel.setSurvivalFilter(if (survivalFilterOnly == false) null else false)
                    }
                )
                Neon3DChip(
                    text = "Upcoming (Pre-Match)",
                    selected = selectedStatusFilter == progamerbattle.com.model.MatchStatus.UPCOMING && survivalFilterOnly == null,
                    activeColor = NeonGreen,
                    onClick = {
                        matchViewModel.setSurvivalFilter(null)
                        matchViewModel.setStatusFilter(progamerbattle.com.model.MatchStatus.UPCOMING)
                    }
                )
                Neon3DChip(
                    text = "Completed Results",
                    selected = selectedStatusFilter == progamerbattle.com.model.MatchStatus.COMPLETED && survivalFilterOnly == null,
                    activeColor = CyberGold,
                    onClick = {
                        matchViewModel.setSurvivalFilter(null)
                        matchViewModel.setStatusFilter(progamerbattle.com.model.MatchStatus.COMPLETED)
                    }
                )
            }
        }

        // Match Items
        items(matches, key = { it.id }) { match ->
            val isEnrolled = currentUser?.joinedMatchIds?.contains(match.id) == true ||
                    match.joinedPlayerUids.contains(currentUser?.uid ?: "")

            TournamentMatchCard(
                match = match,
                isUserEnrolled = isEnrolled,
                forceUnlock = forceUnlock,
                onJoinClick = { matchToJoin = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 28.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Custom Rooms Available Yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "New tournaments created by the host will appear here live.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // --- 3D DIALOG: CONFIRM JOIN MATCH ---
    matchToJoin?.let { match ->
        val fee = match.entryFee
        val balance = currentUser?.walletBalance ?: 0.0
        val remainingBalance = balance - fee

        AlertDialog(
            onDismissRequest = { matchToJoin = null },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Confirm Match Enrollment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        DetailRow(label = "Entry Fee:", value = "₹${fee.toInt()}", valueColor = NeonCyan)
                        DetailRow(label = "Current Balance:", value = "₹${balance.toInt()}", valueColor = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CarbonBorder))
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            label = "Balance After Join:",
                            value = "₹${remainingBalance.toInt()}",
                            valueColor = if (remainingBalance >= 0) NeonGreen else DangerRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Room ID and Password will unlock in this app exactly 7 minutes before match starts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "CONFIRM & PAY ₹${fee.toInt()}",
                    onClick = { matchViewModel.joinMatch(match) },
                    style = Neon3DStyle.CYAN,
                    testTag = "confirm_join_button"
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { matchToJoin = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // --- 3D DIALOG: INSUFFICIENT FUNDS REDIRECT ---
    insufficientFundsMatch?.let { (match, needed) ->
        AlertDialog(
            onDismissRequest = { insufficientFundsMatch = null },
            containerColor = CarbonCardElevated,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonMagenta,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Insufficient Wallet Balance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "You need ₹${needed.toInt()} more to join ${match.title}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add cash via UPI to join this tournament and secure your slot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "DEPOSIT ₹${needed.toInt()} NOW",
                    onClick = {
                        val amountNeeded = needed
                        insufficientFundsMatch = null
                        onNavigateToWallet(amountNeeded)
                    },
                    style = Neon3DStyle.GOLD,
                    testTag = "add_funds_now_button"
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { insufficientFundsMatch = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("LATER", color = TextSecondary)
                }
            }
        )
    }

    // --- DIALOG / MODAL: ROOM CREDENTIALS REVEAL ---
    activeCredentialMatch?.let { match ->
        Dialog(onDismissRequest = { activeCredentialMatch = null }) {
            RoomCredentialCard(
                match = match,
                forceUnlock = forceUnlock,
                onClose = { activeCredentialMatch = null }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}
