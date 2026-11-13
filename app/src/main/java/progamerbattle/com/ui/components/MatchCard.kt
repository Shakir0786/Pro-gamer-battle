package progamerbattle.com.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import kotlinx.coroutines.delay
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextSecondary

@Composable
fun TournamentMatchCard(
    match: TournamentMatch,
    isUserEnrolled: Boolean,
    forceUnlock: Boolean = false,
    onJoinClick: (TournamentMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark
    val formattedTime = match.formattedDateTime()

    var isBreakdownDialogOpen by remember { mutableStateOf(false) }
    var isGoToRoomDialogOpen by remember { mutableStateOf(false) }

    // Live countdown timer state (recalculated every second)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val isMatchCompleted = match.status == MatchStatus.COMPLETED
    val millisUntilStart = match.startTimeMillis - currentTimeMillis
    val isRegistrationLocked = match.isRegistrationClosed || currentTimeMillis >= match.startTimeMillis

    // 7-minute threshold (7 * 60 * 1000 = 420,000 ms)
    val isInActiveRoomPhase = millisUntilStart <= 7 * 60 * 1000L || match.status == MatchStatus.ONGOING || forceUnlock

    val borderBrush = if (isMatchCompleted) {
        Brush.horizontalGradient(listOf(NeonGreen, CyberGold))
    } else if (isUserEnrolled) {
        Brush.horizontalGradient(listOf(NeonCyan, NeonMagenta))
    } else {
        Brush.verticalGradient(
            if (isDark) {
                listOf(
                    SpecularHighlightBright.copy(alpha = 0.4f),
                    cyberSurfaces.border,
                    cyberSurfaces.border.copy(alpha = 0.2f)
                )
            } else {
                listOf(Color.White, cyberSurfaces.border)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}")
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isMatchCompleted) NeonGreen.copy(alpha = 0.3f) else if (isUserEnrolled) NeonCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    if (isDark) listOf(CarbonCardElevated, CarbonCard, CarbonSurface)
                    else listOf(Color.White, cyberSurfaces.card, cyberSurfaces.cardElevated)
                )
            )
            .border(
                width = if (isUserEnrolled || isMatchCompleted) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Time + Status / Registration Lock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp
                        ),
                        color = cyberSurfaces.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Match Time",
                            tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = cyberSurfaces.textSecondary
                        )
                    }
                }

                // Status or Enrolled / Lock Pill
                if (isMatchCompleted) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = NeonGreen)
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(1.2.dp, NeonGreen, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) NeonGreenLight else Color(0xFF047857)
                            )
                        }
                    }
                } else if (isUserEnrolled) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = NeonCyan)
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonCyan.copy(alpha = 0.18f))
                            .border(1.2.dp, NeonCyan, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Enrolled",
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ENROLLED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isDark) NeonCyanLight else Color(0xFF007A8A)
                            )
                        }
                    }
                } else if (isRegistrationLocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.18f))
                            .border(1.dp, DangerRed, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REGISTRATION CLOSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                color = DangerRed
                            )
                        }
                    }
                } else if (match.status == MatchStatus.ONGOING) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = DangerRed)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.22f))
                            .border(1.2.dp, DangerRed, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "LIVE NOW",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row: Map, Format (Solo), Perspective, or Survival Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (match.isSurvivalMode) {
                    Neon3DPill(text = "SURVIVAL", color = CyberGold)
                    Neon3DPill(text = "TOP 10 PRIZE POOL", color = NeonGreen)
                    Neon3DPill(text = match.perspective, color = ElectricPurple)
                } else {
                    Neon3DPill(text = "MAP: ${match.map}", color = NeonCyan)
                    Neon3DPill(text = "MODE: ${match.matchType.label}", color = NeonMagenta)
                    Neon3DPill(text = match.perspective, color = ElectricPurple)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Room Conditions: Survival Mode (Prize Pool & Top 10 Paid) vs Per Kill Parameters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) CarbonSurface else Color(0xFFF1F5F9))
                    .border(1.dp, cyberSurfaces.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.isSurvivalMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Prize Pool",
                            tint = CyberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Prize Pool: ₹${match.survivalPrizePool.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = CyberGold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Top 10 Paid",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top 10 Paid",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreenLight
                        )
                    }

                    Text(
                        text = "Entry: ₹${match.entryFee.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = cyberSurfaces.textPrimary
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = "Per Kill",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Per Kill: ₹${match.perKillReward.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = cyberSurfaces.textPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Entry Fee",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Entry Fee: ₹${match.entryFee.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = cyberSurfaces.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Button labeled 'Prize Pool & Rankings (Top 10)' for Survival, or 'Ranking' for Per Kill
            Neon3DButton(
                text = if (match.isSurvivalMode) "Prize Pool & Rankings (Top 10)" else "Ranking",
                onClick = { isBreakdownDialogOpen = true },
                style = if (match.isSurvivalMode) Neon3DStyle.GOLD else Neon3DStyle.CYAN,
                icon = if (match.isSurvivalMode) Icons.Default.EmojiEvents else Icons.Default.Leaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ranking_button_${match.id}")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Slots Progress Bar with 3D Track & Registration Status
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Slots",
                            tint = cyberSurfaces.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRegistrationLocked) "Final Entries: ${match.joinedPlayersCount}/${match.totalSlots}"
                            else "Spots Filled: ${match.joinedPlayersCount}/${match.totalSlots}",
                            style = MaterialTheme.typography.labelMedium,
                            color = cyberSurfaces.textSecondary
                        )
                    }
                    Text(
                        text = if (isMatchCompleted) "Match Finished"
                        else if (isRegistrationLocked) "Registration Locked"
                        else "${match.remainingSlots} Left",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isMatchCompleted) NeonGreenLight else if (isRegistrationLocked) DangerRed else if (match.remainingSlots <= 5) NeonMagenta else NeonGreenLight
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CarbonBackground else Color(0xFFCBD5E1))
                ) {
                    LinearProgressIndicator(
                        progress = { match.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (isMatchCompleted) NeonGreen else if (isRegistrationLocked) DangerRed else NeonCyan,
                        trackColor = Color.Transparent
                    )
                }
            }

            if (isRegistrationLocked && !isMatchCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) CarbonSurface else Color(0xFFF1F5F9))
                        .border(1.dp, cyberSurfaces.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Final Entries: ${match.joinedPlayersCount}/${match.totalSlots}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = DangerRed
                        )
                        Text(
                            text = "Registration Locked",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cyberSurfaces.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // High-End 3D Action Buttons: Completed vs Enrolled vs Join
            if (isMatchCompleted) {
                Neon3DButton(
                    text = "MATCH COMPLETED",
                    onClick = { isBreakdownDialogOpen = true },
                    style = Neon3DStyle.GOLD,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("match_completed_button_${match.id}")
                )
            } else if (isUserEnrolled) {
                if (isInActiveRoomPhase) {
                    Neon3DButton(
                        text = "GO TO ROOM",
                        onClick = { isGoToRoomDialogOpen = true },
                        style = Neon3DStyle.GREEN,
                        icon = Icons.Default.SportsEsports,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("go_to_room_button_${match.id}")
                    )
                } else {
                    // Waiting phase (> 7 mins left): Live ticking countdown with minus format
                    val minusCountdown = match.formatMinusCountdown(currentTimeMillis)
                    Neon3DButton(
                        text = "WAIT FOR START ($minusCountdown)",
                        onClick = {
                            Toast.makeText(
                                context,
                                "Match starts in $minusCountdown. Room unlocks 7 minutes before match start.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = true,
                        style = Neon3DStyle.DARK,
                        icon = Icons.Default.HourglassBottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wait_for_start_button_${match.id}")
                    )
                }
            } else {
                // Not enrolled: Join Match button or Registration Closed
                val canJoin = !isRegistrationLocked && match.remainingSlots > 0 && match.status == MatchStatus.UPCOMING
                Neon3DButton(
                    text = if (isRegistrationLocked) "REGISTRATION CLOSED"
                    else if (match.remainingSlots <= 0) "MATCH FULL"
                    else "JOIN NOW • ₹${match.entryFee.toInt()}",
                    onClick = { onJoinClick(match) },
                    enabled = canJoin,
                    style = if (canJoin) Neon3DStyle.CYAN else Neon3DStyle.DARK,
                    icon = if (isRegistrationLocked) Icons.Default.Lock else Icons.Default.MilitaryTech,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_match_button_${match.id}")
                )
            }
        }
    }

    // --- RANKING SECTION DIALOG ---
    val isCompleted = match.status == MatchStatus.COMPLETED
    val participantRankings = match.getParticipantRankings()

    if (isBreakdownDialogOpen) {
        AlertDialog(
            onDismissRequest = { isBreakdownDialogOpen = false },
            containerColor = if (isDark) CarbonCardElevated else Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(4.dp, CircleShape, spotColor = if (isCompleted || match.isSurvivalMode) CyberGold else NeonCyan)
                            .clip(CircleShape)
                            .background((if (isCompleted || match.isSurvivalMode) CyberGold else NeonCyan).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted || match.isSurvivalMode) Icons.Default.EmojiEvents else Icons.Default.Leaderboard,
                            contentDescription = "Ranking",
                            tint = if (isCompleted || match.isSurvivalMode) (if (isDark) CyberGold else Color(0xFFD97706)) else NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (match.isSurvivalMode) "Survival Top 10 Prize Pool" else "Ranking",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = cyberSurfaces.textPrimary
                        )
                        Text(
                            text = if (match.isSurvivalMode) {
                                if (isCompleted) "${match.title} • Completed (Top 10 Paid)" else "${match.title} • Guaranteed ₹${match.survivalPrizePool.toInt()} Pool"
                            } else {
                                if (isCompleted) "${match.title} • Completed (Ranked by Kills)" else "${match.title} • Pre-Match Participants"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) NeonGreen else cyberSurfaces.textSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    // Header Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCompleted || match.isSurvivalMode) CyberGold.copy(alpha = 0.12f) else NeonCyan.copy(alpha = 0.12f))
                            .border(1.dp, if (isCompleted || match.isSurvivalMode) CyberGold.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (match.isSurvivalMode) {
                                    if (isCompleted) "SURVIVAL COMPLETED • TOP 10 CASH PRIZES WON" else "SURVIVAL ROOM • TOP 10 GUARANTEED PAYOUTS"
                                } else {
                                    if (isCompleted) "MATCH COMPLETED • RANKED BY HIGHEST KILLS" else "PRE-MATCH • ALL ENTERED PARTICIPANTS"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                color = if (isCompleted || match.isSurvivalMode) CyberGoldLight else NeonCyan
                            )
                            Text(
                                text = "${participantRankings.size} Players",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = cyberSurfaces.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top 10 Prize Pool Breakdown Card for Survival Mode
                    if (match.isSurvivalMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) CarbonSurface else Color(0xFFF1F5F9))
                                .border(1.dp, CyberGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CyberGold, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Top 10 Prize Pool Split",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = CyberGoldLight
                                        )
                                    }
                                    Text(
                                        text = "Pool: ₹${match.survivalPrizePool.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = NeonGreen
                                    )
                                }
                                HorizontalDivider(color = CarbonBorder, modifier = Modifier.padding(vertical = 3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rank 1 (Booyah): ₹${match.getSurvivalPrizeForRank(1).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black), color = CyberGold)
                                    Text("Rank 2: ₹${match.getSurvivalPrizeForRank(2).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = NeonCyanLight)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rank 3: ₹${match.getSurvivalPrizeForRank(3).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textPrimary)
                                    Text("Rank 4: ₹${match.getSurvivalPrizeForRank(4).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rank 5: ₹${match.getSurvivalPrizeForRank(5).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                    Text("Rank 6: ₹${match.getSurvivalPrizeForRank(6).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rank 7: ₹${match.getSurvivalPrizeForRank(7).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                    Text("Rank 8: ₹${match.getSurvivalPrizeForRank(8).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rank 9: ₹${match.getSurvivalPrizeForRank(9).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                    Text("Rank 10: ₹${match.getSurvivalPrizeForRank(10).toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = cyberSurfaces.textSecondary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(participantRankings) { rankingItem ->
                            RankingParticipantRow(
                                item = rankingItem,
                                isCompleted = isCompleted,
                                containerBg = if (isDark) CarbonSurface else Color(0xFFF8FAFC),
                                borderColor = cyberSurfaces.border,
                                textColor = cyberSurfaces.textPrimary,
                                secondaryColor = cyberSurfaces.textSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { isBreakdownDialogOpen = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (isGoToRoomDialogOpen) {
        GoToRoomDialog(
            match = match,
            onDismiss = { isGoToRoomDialogOpen = false }
        )
    }
}

@Composable
private fun RankingParticipantRow(
    item: progamerbattle.com.model.ParticipantRanking,
    isCompleted: Boolean,
    containerBg: Color,
    borderColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    val isRank1 = item.rankNumber == 1
    val badgeBg = if (isCompleted && isRank1) CyberGold.copy(alpha = 0.22f) else NeonCyan.copy(alpha = 0.15f)
    val badgeBorder = if (isCompleted && isRank1) CyberGold else NeonCyan.copy(alpha = 0.6f)
    val badgeText = if (isCompleted && isRank1) CyberGoldLight else NeonCyan

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .border(1.dp, badgeBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = item.rank,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                    color = badgeText
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = "${item.kills} kills",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor
                )
            }
        }

        Text(
            text = if (!isCompleted) "0 winnings" else "₹${item.earnedAmount.toInt()}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = if (isCompleted && item.earnedAmount > 0) NeonGreen else secondaryColor
        )
    }
}
