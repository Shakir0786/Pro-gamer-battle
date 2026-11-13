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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.MatchStatus
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
fun AdminResultsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val matches by adminViewModel.matches.collectAsState()
    val state by adminViewModel.uiState.collectAsState()

    val selectedMatch = matches.firstOrNull { it.id == state.resultSelectedMatchId } ?: matches.firstOrNull()
    val participants = selectedMatch?.defaultEnteredParticipants ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_results_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Match Selector Header
        item {
            Text(
                text = "SELECT MATCH TO DECLARE RESULTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = CyberGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CyberGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Custom Rooms Created Yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create a tournament room first to declare winners & distribute prizes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(matches) { match ->
                        val isSelected = match.id == selectedMatch?.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(CyberGold, Color(0xFFB45309)))
                                    else Brush.horizontalGradient(listOf(CarbonCard, CarbonSurface))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyberGoldLight else CarbonBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { adminViewModel.prepareResultDeclaration(match) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (match.isSurvivalMode) "🏆 " else "⚔️ ",
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = match.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) CarbonBackground else TextPrimary
                                    )
                                }
                                Text(
                                    text = "${match.formattedDateTime()} • ${if (match.isSurvivalMode) "Survival Pool" else "Per Kill"} • ${match.status.label.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    color = if (isSelected) CarbonBackground else when (match.status) {
                                        MatchStatus.UPCOMING -> NeonCyan
                                        MatchStatus.ONGOING -> NeonGreen
                                        MatchStatus.COMPLETED -> CyberGold
                                        MatchStatus.CANCELLED -> DangerRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Match Calculation Banner
        selectedMatch?.let { match ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = if (match.isSurvivalMode) CyberGold else NeonCyan)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                if (match.isSurvivalMode) listOf(Color(0xFF261D04), Color(0xFF0F0B02))
                                else listOf(Color(0xFF041C26), Color(0xFF020E14))
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.horizontalGradient(listOf(if (match.isSurvivalMode) CyberGold else NeonCyan, NeonGreen)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = match.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "📅 ${match.formattedDateTime()} (${match.formatMinusCountdown()})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyberGoldLight
                                )
                                Text(
                                    text = if (match.isSurvivalMode) {
                                        "🏆 Survival Mode • Guaranteed Prize Pool: ₹${match.totalPrizePool.toInt()}"
                                    } else {
                                        "⚔️ Per Kill Arena • Rate: ₹${match.perKillReward.toInt()} Per Kill • Entry: ₹${match.entryFee.toInt()}"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (match.isSurvivalMode) CyberGoldLight else NeonCyanLight
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonGreen.copy(alpha = 0.2f))
                                    .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "BOOYAH: ${state.resultBooyahWinnerName.ifBlank { match.winnerName.ifBlank { "Pending" } }}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                                    color = NeonGreen
                                )
                            }
                        }

                        HorizontalDivider(color = CarbonBorder, thickness = 1.dp)

                        Text(
                            text = if (match.isSurvivalMode) {
                                "Set rank (1-10) for each player. Players in Top 10 will receive their exact share of the ₹${match.totalPrizePool.toInt()} Survival Prize Pool credited directly to their winning wallet."
                            } else {
                                "Set Booyah #1 winner and record kill counts. Finalizing will calculate ₹${match.perKillReward.toInt()} bounty per kill and credit player winning wallets."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // Table Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (match.isSurvivalMode) "PLAYER & SURVIVAL RANK" else "PLAYER & BOOYAH",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = TextSecondary
                    )
                    Text(
                        text = if (match.isSurvivalMode) "PRIZE & KILLS" else "KILLS & BOUNTY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = TextSecondary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Participant Result Rows
            if (participants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No players have joined this custom room yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
            items(participants) { participant ->
                val currentKills = state.resultPlayerKills[participant.name] ?: participant.kills
                val currentRank = state.resultPlayerRanks[participant.name] ?: 1
                val isWinner = (state.resultBooyahWinnerName == participant.name) ||
                               (state.resultBooyahWinnerName.isBlank() && match.winnerName.isNotBlank() && participant.name == match.winnerName)
                val ffUid = participant.uidNumber.ifBlank { "Not Set" }

                val earnedReward: Double = if (match.isSurvivalMode) {
                    match.getSurvivalPrizeForRank(currentRank)
                } else {
                    currentKills * match.perKillReward
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWinner || currentRank == 1) Color(0xFF1E1704) else CarbonCard)
                        .border(
                            1.dp,
                            if (isWinner || currentRank == 1) CyberGold else CarbonBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .testTag("admin_result_row_${participant.name}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Player Info & UID
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clickable { adminViewModel.setBooyahWinner(participant.name) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isWinner || currentRank == 1) CyberGold else CarbonSurface)
                                        .border(1.dp, if (isWinner || currentRank == 1) CyberGoldLight else CarbonBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isWinner || currentRank == 1) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Booyah Winner",
                                            tint = CarbonBackground,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "#$currentRank",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                            color = TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = participant.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isWinner || currentRank == 1) CyberGoldLight else TextPrimary
                                    )
                                    Text(
                                        text = "FF UID: $ffUid",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = NeonCyanLight
                                    )
                                }
                            }

                            // Reward & Controls
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (match.isSurvivalMode) "₹${earnedReward.toInt()} Prize" else "₹${earnedReward.toInt()} Bounty",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = if (earnedReward > 0.0) NeonGreen else TextMuted
                                )
                                Text(
                                    text = "$currentKills Kills",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = NeonCyan
                                )
                            }
                        }

                        // Stepper Controls Row: Ranks (for Survival) & Kills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (match.isSurvivalMode) {
                                // Survival Rank Stepper [-] Rank X [+]
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Finish Rank: ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )

                                    // Rank Minus
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CarbonSurface)
                                            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                adminViewModel.setPlayerRank(participant.name, (currentRank - 1).coerceAtLeast(1))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Rank -", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "Rank $currentRank",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp),
                                        color = if (currentRank <= 10) CyberGoldLight else TextSecondary
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Rank Plus
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberGold.copy(alpha = 0.2f))
                                            .border(1.dp, CyberGold, RoundedCornerShape(6.dp))
                                            .clickable {
                                                adminViewModel.setPlayerRank(participant.name, currentRank + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Rank +", tint = CyberGold, modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                Text(
                                    text = if (isWinner) "🏆 Booyah Winner" else "Tap name to set as Booyah",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isWinner) CyberGold else TextMuted
                                )
                            }

                            // Kills Stepper [-] Kills [+]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Kills: ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CarbonSurface)
                                        .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            adminViewModel.setPlayerKills(participant.name, currentKills - 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Kill -", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "$currentKills",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp),
                                    color = NeonCyan
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.2f))
                                        .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                                        .clickable {
                                            adminViewModel.setPlayerKills(participant.name, currentKills + 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Kill +", tint = NeonCyan, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Finalize Button
            item {
                if (match.status == MatchStatus.CANCELLED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚫 ROOM IS CANCELLED • 100% Entry fees refunded. Results cannot be declared.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = DangerRed
                        )
                    }
                } else {
                    Neon3DButton(
                        text = if (match.isSurvivalMode) {
                            "DISTRIBUTE SURVIVAL PRIZE POOL (₹${match.totalPrizePool.toInt()})"
                        } else {
                            "FINALISE MATCH & DISTRIBUTE KILL BOUNTIES"
                        },
                        onClick = { adminViewModel.finalizeMatchResults() },
                        style = Neon3DStyle.GOLD,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_finalize_results_button")
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
