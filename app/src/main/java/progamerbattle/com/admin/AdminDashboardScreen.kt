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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
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
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val matches by adminViewModel.matches.collectAsState()
    val deposits by adminViewModel.deposits.collectAsState()
    val withdrawals by adminViewModel.withdrawals.collectAsState()
    val allUsers by adminViewModel.allUsers.collectAsState()
    val activeUsers by adminViewModel.activeUsers.collectAsState()

    val upcomingMatches = matches.count { it.status == MatchStatus.UPCOMING }
    val ongoingMatches = matches.count { it.status == MatchStatus.ONGOING }
    val totalCollections = matches.sumOf { it.joinedPlayersCount * it.entryFee }
    val hostMarginTotal = totalCollections * 0.30
    val pendingDepositsCount = deposits.count { it.status.equals("Pending", ignoreCase = true) }
    val pendingWithdrawalsCount = withdrawals.count { it.status.equals("Pending", ignoreCase = true) }
    val pendingPayoutsCount = pendingDepositsCount + pendingWithdrawalsCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Lightweight 2x2 Summary Grid (only the 4 key numbers, calm spacing)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LightStatTile(
                        label = "Collection",
                        value = "₹${totalCollections.toInt()}",
                        sub = "Host 30%: ₹${hostMarginTotal.toInt()}",
                        accent = CyberGold,
                        modifier = Modifier.weight(1f)
                    )
                    LightStatTile(
                        label = "Pending Verify",
                        value = "$pendingPayoutsCount",
                        sub = "$pendingDepositsCount Add • $pendingWithdrawalsCount Pay",
                        accent = if (pendingPayoutsCount > 0) DangerRed else NeonGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { adminViewModel.selectTab(AdminTab.WITHDRAWALS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LightStatTile(
                        label = "Custom Rooms",
                        value = "${matches.size}",
                        sub = "$upcomingMatches Upcoming • $ongoingMatches Live",
                        accent = NeonCyan,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { adminViewModel.selectTab(AdminTab.TOURNAMENTS) }
                    )
                    LightStatTile(
                        label = "Players",
                        value = "${allUsers.size}",
                        sub = "${activeUsers.size} Online now",
                        accent = NeonGreen,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                adminViewModel.selectTab(AdminTab.PLAYER_LISTS)
                                adminViewModel.selectUserListSubTab(UserListSubTab.SIGNED_UP)
                            }
                    )
                }
            }
        }

        // 2. Primary Action: Create New Room
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonCyan.copy(alpha = 0.14f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable { adminViewModel.openCreateMatchDialog() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("admin_quick_action_create_room"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Room",
                            tint = CarbonBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Create New Custom Room",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Per Kill Arena ya Survival Prize Pool setup karein",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NeonCyanLight
                )
            }
        }

        // 3. Main Screen Modules Hub (Rosters & Results moved off bottom bar so it stays airy)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "MANAGEMENT HUB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                AdminHubRow(
                    title = "Player Rosters & Online Users",
                    subtitle = "${allUsers.size} Registered • ${activeUsers.size} Active now",
                    icon = Icons.Default.Group,
                    accent = CyberGold,
                    tag = "admin_quick_action_user_lists",
                    onClick = { adminViewModel.selectTab(AdminTab.PLAYER_LISTS) }
                )

                AdminHubRow(
                    title = "Declare Match Results & Kills",
                    subtitle = "Update winners, kill rewards & distribute prize pool",
                    icon = Icons.Default.EmojiEvents,
                    accent = NeonGreen,
                    tag = "admin_quick_action_declare_results",
                    onClick = { adminViewModel.selectTab(AdminTab.RESULTS) }
                )

                AdminHubRow(
                    title = "Add Cash & Withdrawal Queue",
                    subtitle = if (pendingPayoutsCount > 0) "$pendingPayoutsCount pending approvals waiting" else "All deposits & withdrawals up to date",
                    icon = Icons.Default.AccountBalanceWallet,
                    accent = if (pendingPayoutsCount > 0) DangerRed else NeonCyan,
                    tag = "admin_quick_action_payouts",
                    onClick = { adminViewModel.selectTab(AdminTab.WITHDRAWALS) }
                )
            }
        }

        // 4. Upcoming / Live Rooms (Compact 2-item preview)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ROOMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = TextMuted
                )
                Text(
                    text = "View All (${matches.size}) →",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = NeonCyan,
                    modifier = Modifier
                        .clickable { adminViewModel.selectTab(AdminTab.TOURNAMENTS) }
                        .padding(vertical = 4.dp)
                )
            }
        }

        items(matches.take(2), key = { it.id }) { match ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                    .clickable { adminViewModel.selectTab(AdminTab.TOURNAMENTS) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = CyberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = match.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${match.formattedDateTime()} • ${match.joinedPlayersCount}/${match.totalSlots} Joined",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = match.status.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (match.status) {
                        MatchStatus.UPCOMING -> NeonCyanLight
                        MatchStatus.ONGOING -> NeonGreen
                        MatchStatus.COMPLETED -> CyberGoldLight
                        MatchStatus.CANCELLED -> DangerRed
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun LightStatTile(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonCard)
            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = accent,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AdminHubRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonCard)
            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
            .testTag(tag),
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
                    .background(CarbonSurface)
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}
