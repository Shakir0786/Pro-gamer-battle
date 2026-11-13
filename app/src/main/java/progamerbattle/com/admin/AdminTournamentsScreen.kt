package progamerbattle.com.admin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.TournamentMatch
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

import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import android.widget.Toast

@Composable
fun AdminTournamentsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val matches by adminViewModel.matches.collectAsState()
    val state by adminViewModel.uiState.collectAsState()

    var matchToDelete by remember { mutableStateOf<TournamentMatch?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_tournaments_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Top Action Bar: Create Custom Rooms (Separate functions for Per Kill and Survival)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MANAGE CUSTOM ROOMS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${matches.size} Total Custom Arenas in System",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // 2 SEPARATE DEDICATED ROOM CREATION BUTTONS (3D HIGH-TECH STYLE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Create Per Kill Room
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = NeonCyan)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonCyan, Color(0xFF008060))
                                )
                            )
                            .clickable { adminViewModel.openCreatePerKillMatchDialog() }
                            .padding(vertical = 11.dp, horizontal = 8.dp)
                            .testTag("admin_create_per_kill_room_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CarbonBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "⚔️ PER KILL ROOM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = CarbonBackground,
                                maxLines = 1
                            )
                        }
                    }

                    // Button 2: Create Survival Room (Top 10 Prize Pool)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = CyberGold)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(CyberGold, Color(0xFFB37400))
                                )
                            )
                            .clickable { adminViewModel.openCreateSurvivalMatchDialog() }
                            .padding(vertical = 11.dp, horizontal = 8.dp)
                            .testTag("admin_create_survival_room_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CarbonBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🏆 SURVIVAL ROOM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = CarbonBackground,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // List of Custom Rooms
        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No Custom Rooms Created Yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap Per Kill Room or Survival Room above to create your first live match.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(matches) { match ->
                AdminMatchCard(
                    match = match,
                    onEditClick = { adminViewModel.openEditMatchDialog(match) },
                    onBroadcastClick = { adminViewModel.openBroadcastCredentialsDialog(match) },
                    onStartClick = { adminViewModel.startMatch(match.id) },
                    onDeclareResultsClick = { adminViewModel.prepareResultDeclaration(match) },
                    onCancelClick = { adminViewModel.promptCancelMatch(match) },
                    onViewPlayersClick = {
                        adminViewModel.selectMatch(match.id)
                        adminViewModel.selectTab(AdminTab.PLAYER_LISTS)
                    },
                    onDeleteClick = { matchToDelete = match }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // Modal: Broadcast Room ID & Password
    if (state.isBroadcastCredentialsDialogOpen) {
        AlertDialog(
            onDismissRequest = { adminViewModel.closeBroadcastCredentialsDialog() },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Broadcast Room Credentials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the official Custom Room ID and Password to unlock it for all registered participants.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = state.broadcastRoomIdInput,
                        onValueChange = { adminViewModel.onBroadcastRoomIdChanged(it) },
                        label = { Text("Custom Room ID (e.g. 84920194)") },
                        singleLine = true,
                        colors = adminFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_room_id")
                    )

                    OutlinedTextField(
                        value = state.broadcastPasswordInput,
                        onValueChange = { adminViewModel.onBroadcastPasswordChanged(it) },
                        label = { Text("Room Password (e.g. 1234)") },
                        singleLine = true,
                        colors = adminFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_room_password")
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "BROADCAST LIVE",
                    onClick = { adminViewModel.broadcastRoomCredentials() },
                    style = Neon3DStyle.CYAN,
                    modifier = Modifier.testTag("admin_submit_broadcast_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { adminViewModel.closeBroadcastCredentialsDialog() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Modal: Cancel Match & Automatic Refund Confirmation
    state.matchToCancel?.let { match ->
        val totalRefund = (match.joinedPlayersCount * match.entryFee).toInt()
        AlertDialog(
            onDismissRequest = { adminViewModel.dismissCancelMatch() },
            containerColor = CarbonCardElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel Match & Auto-Refund?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = DangerRed
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Are you sure you want to cancel '${match.title}'?",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Entry Fee: ₹${match.entryFee.toInt()} per player",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "• Enrolled Players: ${match.joinedPlayersCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "• Total Auto-Refund Amount: ₹$totalRefund",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                            Text(
                                text = "• Action: Status -> CANCELLED, Wallet credited automatically with full transaction record.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberGoldLight
                            )
                        }
                    }

                    Text(
                        text = "Sabhi registered players ki entry fee bina kisi katauti ke turant unke wallet balance mein credit ho jayegi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { adminViewModel.confirmCancelMatch() },
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = DangerRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, DangerRed)
                ) {
                    Text("Confirm Cancel & Refund", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { adminViewModel.dismissCancelMatch() }) {
                    Text("Keep Match", color = TextSecondary)
                }
            }
        )
    }

    // Modal: Delete Confirmation
    matchToDelete?.let { match ->
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            containerColor = CarbonCardElevated,
            title = { Text("Delete Tournament Room?", color = DangerRed) },
            text = { Text("Are you sure you want to delete '${match.title}'? This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        adminViewModel.deleteMatch(match.id)
                        matchToDelete = null
                    }
                ) {
                    Text("Delete Room", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { matchToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminMatchCard(
    match: TournamentMatch,
    onEditClick: () -> Unit,
    onBroadcastClick: () -> Unit,
    onStartClick: () -> Unit,
    onDeclareResultsClick: () -> Unit,
    onCancelClick: () -> Unit,
    onViewPlayersClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Live countdown timer state (recalculated every second so admin never forgets match time)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val millisUntilStart = match.startTimeMillis - currentTimeMillis
    val isTimeArrived = millisUntilStart <= 0L && match.status == MatchStatus.UPCOMING
    val isUnderSevenMinutes = millisUntilStart in 1L..(7 * 60 * 1000L) && match.status == MatchStatus.UPCOMING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonCard)
            .border(
                1.dp,
                if (match.status == MatchStatus.CANCELLED) DangerRed.copy(alpha = 0.5f) else CarbonBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("admin_match_card_${match.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Header: Title & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Map: ${match.map} • ${match.matchType.label} • ${match.totalSlots} Slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (match.status) {
                                MatchStatus.UPCOMING -> if (isTimeArrived) DangerRed.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)
                                MatchStatus.ONGOING -> NeonGreen.copy(alpha = 0.15f)
                                MatchStatus.COMPLETED -> CyberGold.copy(alpha = 0.15f)
                                MatchStatus.CANCELLED -> DangerRed.copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            1.dp,
                            when (match.status) {
                                MatchStatus.UPCOMING -> if (isTimeArrived) DangerRed else NeonCyan
                                MatchStatus.ONGOING -> NeonGreen
                                MatchStatus.COMPLETED -> CyberGold
                                MatchStatus.CANCELLED -> DangerRed
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = match.status.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = when (match.status) {
                            MatchStatus.UPCOMING -> if (isTimeArrived) DangerRed else NeonCyan
                            MatchStatus.ONGOING -> NeonGreen
                            MatchStatus.COMPLETED -> CyberGold
                            MatchStatus.CANCELLED -> DangerRed
                        }
                    )
                }
            }

            // 1B. LIVE SCHEDULE & MATCH START COUNTDOWN BANNER (ADMIN NEVER FORGETS MATCH TIME)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            match.status == MatchStatus.CANCELLED -> DangerRed.copy(alpha = 0.08f)
                            match.status == MatchStatus.COMPLETED -> CarbonSurface
                            match.status == MatchStatus.ONGOING -> NeonGreen.copy(alpha = 0.15f)
                            isTimeArrived -> DangerRed.copy(alpha = 0.25f)
                            isUnderSevenMinutes -> CyberGold.copy(alpha = 0.22f)
                            else -> CarbonSurface
                        }
                    )
                    .border(
                        1.2.dp,
                        when {
                            match.status == MatchStatus.CANCELLED -> DangerRed.copy(alpha = 0.3f)
                            match.status == MatchStatus.COMPLETED -> CarbonBorder
                            match.status == MatchStatus.ONGOING -> NeonGreen
                            isTimeArrived -> DangerRed
                            isUnderSevenMinutes -> CyberGold
                            else -> NeonCyan.copy(alpha = 0.5f)
                        },
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Formatted Match Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Match Schedule",
                            tint = when {
                                match.status == MatchStatus.ONGOING -> NeonGreen
                                isTimeArrived -> DangerRed
                                isUnderSevenMinutes -> CyberGold
                                else -> NeonCyan
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "MATCH SCHEDULE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.6.sp
                                ),
                                color = TextMuted
                            )
                            Text(
                                text = match.formattedDateTime(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                ),
                                color = when {
                                    match.status == MatchStatus.ONGOING -> NeonGreen
                                    isTimeArrived -> DangerRed
                                    isUnderSevenMinutes -> CyberGoldLight
                                    else -> TextPrimary
                                }
                            )
                        }
                    }

                    // Right: Live Status / Countdown Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    match.status == MatchStatus.COMPLETED -> CarbonCardElevated
                                    match.status == MatchStatus.CANCELLED -> DangerRed.copy(alpha = 0.2f)
                                    match.status == MatchStatus.ONGOING -> NeonGreen.copy(alpha = 0.25f)
                                    isTimeArrived -> DangerRed
                                    isUnderSevenMinutes -> CyberGold
                                    else -> NeonCyan.copy(alpha = 0.18f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                match.status == MatchStatus.COMPLETED -> "FINISHED"
                                match.status == MatchStatus.CANCELLED -> "CANCELLED"
                                match.status == MatchStatus.ONGOING -> "● LIVE NOW"
                                isTimeArrived -> "🔥 TIME OVER! START NOW"
                                isUnderSevenMinutes -> "⚡ START IN ${match.formatMinusCountdown(currentTimeMillis)}"
                                else -> "⏳ In ${match.formatMinusCountdown(currentTimeMillis)}"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = when {
                                match.status == MatchStatus.COMPLETED -> TextMuted
                                match.status == MatchStatus.CANCELLED -> DangerRed
                                match.status == MatchStatus.ONGOING -> NeonGreen
                                isTimeArrived -> Color.White
                                isUnderSevenMinutes -> CarbonBackground
                                else -> NeonCyanLight
                            }
                        )
                    }
                }
            }

            // Cancelled Banner if match is cancelled
            if (match.status == MatchStatus.CANCELLED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DangerRed.copy(alpha = 0.12f))
                        .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MATCH CANCELLED • 100% Entry Fees Refunded to Players",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = DangerRed
                        )
                    }
                }
            }

            // 2. Credentials & Enrolled Stats Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CarbonSurface)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Credentials Row with 1-Tap Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CyberGold, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room: ${match.roomId}  |  Pass: ${match.roomPassword}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyberGoldLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Copy Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CarbonCardElevated)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("Room: ${match.roomId} Pass: ${match.roomPassword}"))
                                    Toast.makeText(context, "Room credentials copied!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "COPY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = CyberGold
                            )
                        }
                    }

                    // Enrolled Count & Prize Bounty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enrolled: ${match.joinedPlayersCount}/${match.totalSlots} Players  •  Entry: ₹${match.entryFee.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )

                        if (match.isSurvivalMode) {
                            Text(
                                text = "🏆 Top 10 Pool: ₹${match.totalPrizePool.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = CyberGoldLight
                            )
                        } else {
                            Text(
                                text = "Kill Reward: ₹${match.perKillReward.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                        }
                    }
                }
            }

            // 3. Operational Action Buttons (2-Row Layout: No Squished or Vertically Cut Text)
            // ROW 1: Match Live Operations (Broadcast ID & Start/Declare)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Broadcast Room ID
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onBroadcastClick() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BroadcastOnPersonal,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Broadcast ID",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // If upcoming, allow starting match; if ongoing or completed, allow result declaration
                if (match.status == MatchStatus.UPCOMING) {
                    val isStartUrgent = isTimeArrived || isUnderSevenMinutes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isTimeArrived -> DangerRed
                                    isUnderSevenMinutes -> NeonGreen
                                    else -> NeonGreen.copy(alpha = 0.15f)
                                }
                            )
                            .border(
                                1.dp,
                                if (isTimeArrived) DangerRed else NeonGreen,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onStartClick() }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isStartUrgent) CarbonBackground else NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTimeArrived) "Start Match Now!" else "Start Match",
                                color = if (isStartUrgent) CarbonBackground else NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (match.status != MatchStatus.CANCELLED) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberGold.copy(alpha = 0.15f))
                            .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable { onDeclareResultsClick() }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Declare Kills",
                                color = CyberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ROW 2: Secondary Room Management (Edit, View Roster, Cancel Room, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Room Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberGold.copy(alpha = 0.15f))
                        .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onEditClick() }
                        .padding(horizontal = 10.dp)
                        .testTag("admin_edit_match_${match.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Room",
                            tint = CyberGoldLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            color = CyberGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // View Roster Button
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonCardElevated)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                        .clickable { onViewPlayersClick() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = NeonCyanLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "View Roster",
                            color = NeonCyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Cancel Match Button (with automatic refund)
                if (match.status == MatchStatus.UPCOMING || match.status == MatchStatus.ONGOING) {
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.10f))
                            .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onCancelClick() }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cancel Room",
                                color = DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Delete Button (Compact styled square)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonCardElevated)
                        .border(1.dp, DangerRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Room",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 4. INLINE JOINED USERS LIST & FREE FIRE UID COPIER
            val joinedList = match.defaultEnteredParticipants
            var isRosterExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isRosterExpanded) NeonCyan.copy(alpha = 0.12f) else CarbonSurface)
                    .border(1.dp, if (isRosterExpanded) NeonCyan.copy(alpha = 0.6f) else CarbonBorder, RoundedCornerShape(10.dp))
                    .clickable { isRosterExpanded = !isRosterExpanded }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .testTag("admin_toggle_roster_${match.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Joined Users & Free Fire UIDs (${joinedList.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (isRosterExpanded) "Hide List ▲" else "View & Copy UIDs ▼",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = NeonCyanLight
                    )
                }
            }

            if (isRosterExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Action: Copy All FF UIDs Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PARTICIPANT ROSTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            color = TextMuted
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable {
                                    val allUids = joinedList.map { "${it.name}: ${it.uidNumber}" }.joinToString("\n")
                                    clipboardManager.setText(AnnotatedString(allUids))
                                    Toast.makeText(context, "Copied all ${joinedList.size} player Free Fire UIDs!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("admin_copy_all_uids_${match.id}")
                        ) {
                            Text(
                                text = "COPY ALL UIDs",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = NeonGreen
                            )
                        }
                    }

                    joinedList.forEachIndexed { idx, player ->
                        val slot = idx + 1
                        val ffUid = player.uidNumber.ifBlank { "84920${slot}19" }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonCardElevated)
                                .border(0.6.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(CyberGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$slot",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                        color = CyberGoldLight
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "FF UID: ",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextMuted
                                        )
                                        Text(
                                            text = ffUid,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = NeonCyanLight
                                        )
                                    }
                                }
                            }

                            // 1-Tap Copy UID Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(ffUid))
                                        Toast.makeText(context, "Copied UID: $ffUid (${player.name})", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("admin_copy_uid_${player.name}")
                            ) {
                                Text(
                                    text = "COPY UID",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                    color = NeonCyanLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CarbonBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CarbonSurface,
    unfocusedContainerColor = CarbonSurface
)
