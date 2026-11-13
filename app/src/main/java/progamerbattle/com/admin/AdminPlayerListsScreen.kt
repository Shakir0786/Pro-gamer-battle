package progamerbattle.com.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import progamerbattle.com.model.User
import progamerbattle.com.model.UserActivityStatus
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldDark
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPlayerListsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by adminViewModel.allUsers.collectAsState()
    val activeUsers by adminViewModel.activeUsers.collectAsState()
    val matches by adminViewModel.matches.collectAsState()
    val state by adminViewModel.uiState.collectAsState()

    val searchQuery = state.playerSearchQuery.trim().lowercase()

    // Activity counts for quick filter chips
    val onlineCount = allUsers.count { it.isOnline }
    val offlineCount = allUsers.count { !it.isOnline && !it.isInactive30Days && !it.isBanned }
    val inactive30DaysCount = allUsers.count { it.isInactive30Days }

    // Filtered lists based on search & activity filter
    val filteredAllUsers = allUsers.filter { user ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            user.fullName.lowercase().contains(searchQuery) ||
            user.ffIgn.lowercase().contains(searchQuery) ||
            user.mobileNumber.contains(searchQuery) ||
            user.ffUid.contains(searchQuery)
        }
        val matchesFilter = when (state.userActivityFilter) {
            UserActivityFilter.ALL -> true
            UserActivityFilter.ONLINE -> user.isOnline
            UserActivityFilter.OFFLINE -> !user.isOnline && !user.isInactive30Days && !user.isBanned
            UserActivityFilter.INACTIVE_30_DAYS -> user.isInactive30Days
        }
        matchesSearch && matchesFilter
    }

    val filteredActiveUsers = if (searchQuery.isBlank()) {
        activeUsers
    } else {
        activeUsers.filter {
            it.fullName.lowercase().contains(searchQuery) ||
            it.ffIgn.lowercase().contains(searchQuery) ||
            it.mobileNumber.contains(searchQuery) ||
            it.ffUid.contains(searchQuery)
        }
    }

    val selectedMatch = matches.firstOrNull { it.id == state.selectedMatchId } ?: matches.firstOrNull()
    val participants = selectedMatch?.defaultEnteredParticipants ?: emptyList()
    val filteredParticipants = if (searchQuery.isBlank()) {
        participants
    } else {
        participants.filter {
            it.name.lowercase().contains(searchQuery) ||
            it.uidNumber.lowercase().contains(searchQuery)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_player_lists_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // TOP SUMMARY METRICS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Total Signed-Up
                AdminUserMetricCard(
                    title = "REGISTERED PLAYERS",
                    value = "${allUsers.size}",
                    subtitle = "All accounts in registry",
                    icon = Icons.Default.Group,
                    neonColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Currently Active
                AdminUserMetricCard(
                    title = "ONLINE NOW",
                    value = "$onlineCount",
                    subtitle = "App currently open",
                    icon = Icons.Default.Sensors,
                    neonColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: Inactive (30+ Days)
                AdminUserMetricCard(
                    title = "INACTIVE (30d+)",
                    value = "$inactive30DaysCount",
                    subtitle = "No login in 30 days",
                    icon = Icons.Default.Warning,
                    neonColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // SUB-NAVIGATION TABS (Signed-Up vs Active Now vs Match Rosters)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CarbonSurface)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 1: All Signed-Up Users
                SubTabButton(
                    title = "All Registered (${allUsers.size})",
                    isSelected = state.userListSubTab == UserListSubTab.SIGNED_UP,
                    neonColor = CyberGold,
                    modifier = Modifier.weight(1f),
                    onClick = { adminViewModel.selectUserListSubTab(UserListSubTab.SIGNED_UP) }
                )

                // Tab 2: Currently Active Players
                SubTabButton(
                    title = "Online Now ($onlineCount)",
                    isSelected = state.userListSubTab == UserListSubTab.ACTIVE_NOW,
                    neonColor = NeonGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { adminViewModel.selectUserListSubTab(UserListSubTab.ACTIVE_NOW) }
                )

                // Tab 3: Match Rosters
                SubTabButton(
                    title = "Room Rosters",
                    isSelected = state.userListSubTab == UserListSubTab.MATCH_ROSTER,
                    neonColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { adminViewModel.selectUserListSubTab(UserListSubTab.MATCH_ROSTER) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = state.playerSearchQuery,
                onValueChange = { adminViewModel.onPlayerSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = when (state.userListSubTab) {
                            UserListSubTab.SIGNED_UP -> "Search users by Name, Mobile, Game UID, IGN..."
                            UserListSubTab.ACTIVE_NOW -> "Search online players..."
                            UserListSubTab.MATCH_ROSTER -> "Search players in room..."
                        },
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = when (state.userListSubTab) {
                            UserListSubTab.SIGNED_UP -> CyberGold
                            UserListSubTab.ACTIVE_NOW -> NeonGreen
                            UserListSubTab.MATCH_ROSTER -> NeonCyan
                        },
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CarbonBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CarbonSurface,
                    unfocusedContainerColor = CarbonSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_user_search_field")
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // MAIN CONTENT AREA BASED ON SUB-TAB
        when (state.userListSubTab) {
            // ==========================================
            // 1. ALL SIGNED-UP USERS LIST
            // ==========================================
            UserListSubTab.SIGNED_UP -> {
                // Activity Filter Chips: All | Online | Offline | Inactive (30+ Days)
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            ActivityFilterChip(
                                label = "All (${allUsers.size})",
                                isSelected = state.userActivityFilter == UserActivityFilter.ALL,
                                activeColor = CyberGold,
                                onClick = { adminViewModel.selectUserActivityFilter(UserActivityFilter.ALL) }
                            )
                        }
                        item {
                            ActivityFilterChip(
                                label = "🟢 Online ($onlineCount)",
                                isSelected = state.userActivityFilter == UserActivityFilter.ONLINE,
                                activeColor = NeonGreen,
                                onClick = { adminViewModel.selectUserActivityFilter(UserActivityFilter.ONLINE) }
                            )
                        }
                        item {
                            ActivityFilterChip(
                                label = "⚪ Offline ($offlineCount)",
                                isSelected = state.userActivityFilter == UserActivityFilter.OFFLINE,
                                activeColor = TextSecondary,
                                onClick = { adminViewModel.selectUserActivityFilter(UserActivityFilter.OFFLINE) }
                            )
                        }
                        item {
                            ActivityFilterChip(
                                label = "⚠️ Inactive 30d+ ($inactive30DaysCount)",
                                isSelected = state.userActivityFilter == UserActivityFilter.INACTIVE_30_DAYS,
                                activeColor = Color(0xFFF59E0B),
                                onClick = { adminViewModel.selectUserActivityFilter(UserActivityFilter.INACTIVE_30_DAYS) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "USER ACCOUNTS (${filteredAllUsers.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CyberGold
                        )
                        Text(
                            text = "Full Admin Controls Active",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                if (filteredAllUsers.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = if (state.playerSearchQuery.isNotBlank()) "No users matching '${state.playerSearchQuery}'" else "No users found in this filter.",
                            icon = Icons.Default.Group
                        )
                    }
                } else {
                    items(filteredAllUsers, key = { it.mobileNumber + it.uid }) { user ->
                        SignedUpUserControlCard(
                            user = user,
                            onOpenBalanceControl = { adminViewModel.openUserBalanceDialog(user) },
                            onViewTransactions = { adminViewModel.openUserTransactions(user) },
                            onDeleteAccount = { adminViewModel.promptDeleteUser(user) },
                            onGiftBonus = { adminViewModel.giftBonusToPlayer(user.mobileNumber, 50.0) },
                            onToggleBan = { adminViewModel.togglePlayerBan(user.mobileNumber) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // ==========================================
            // 2. CURRENTLY ACTIVE USERS LIST
            // ==========================================
            UserListSubTab.ACTIVE_NOW -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PLAYERS ONLINE NOW (${filteredActiveUsers.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = NeonGreen
                            )
                        }
                        Text(
                            text = "App is currently open",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = NeonGreen.copy(alpha = 0.8f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }

                if (filteredActiveUsers.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = if (state.playerSearchQuery.isNotBlank()) "No online players matching '${state.playerSearchQuery}'" else "No players currently have the app open.",
                            icon = Icons.Default.Sensors
                        )
                    }
                } else {
                    items(filteredActiveUsers, key = { it.mobileNumber + it.uid }) { user ->
                        SignedUpUserControlCard(
                            user = user,
                            onOpenBalanceControl = { adminViewModel.openUserBalanceDialog(user) },
                            onViewTransactions = { adminViewModel.openUserTransactions(user) },
                            onDeleteAccount = { adminViewModel.promptDeleteUser(user) },
                            onGiftBonus = { adminViewModel.giftBonusToPlayer(user.mobileNumber, 50.0) },
                            onToggleBan = { adminViewModel.togglePlayerBan(user.mobileNumber) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // ==========================================
            // 3. MATCH ROSTERS
            // ==========================================
            UserListSubTab.MATCH_ROSTER -> {
                item {
                    Text(
                        text = "SELECT TOURNAMENT ROOM",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CyberGold
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    if (matches.isEmpty()) {
                        EmptyStateCard(
                            message = "No custom rooms created yet.",
                            icon = Icons.Default.SportsEsports
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(matches) { match ->
                                val isSelected = match.id == selectedMatch?.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CyberGold.copy(alpha = 0.2f) else CarbonSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) CyberGold else CarbonBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { adminViewModel.selectMatch(match.id) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = match.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                            ),
                                            color = if (isSelected) CyberGoldLight else TextPrimary
                                        )
                                        Text(
                                            text = "⏰ ${match.formattedDateTime()}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = if (isSelected) CarbonBackground else NeonCyanLight
                                        )
                                        Text(
                                            text = "${match.joinedPlayersCount}/${match.totalSlots} Slots",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isSelected) CyberGold else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENROLLED ROSTER (${filteredParticipants.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = "Room: ${selectedMatch?.roomId ?: "N/A"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (filteredParticipants.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No enrolled players found in this custom room.",
                            icon = Icons.Default.SportsEsports
                        )
                    }
                } else {
                    itemsIndexed(filteredParticipants) { index, player ->
                        ParticipantRosterItem(index = index + 1, player = player)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // ==========================================
    // MODALS: BALANCE CONTROL & DELETE CONFIRMATION
    // ==========================================

    // 1. User Balance Control Modal
    if (state.isUserBalanceDialogOpen && state.selectedUserForBalance != null) {
        val targetUser = state.selectedUserForBalance!!
        UserBalanceControlDialog(
            user = targetUser,
            depositInput = state.editDepositInput,
            winningInput = state.editWinningInput,
            bonusInput = state.editBonusInput,
            onFieldsChanged = { dep, win, bon ->
                adminViewModel.onUserBalanceFieldsChanged(dep, win, bon)
            },
            onSave = { adminViewModel.saveUserBalance() },
            onDismiss = { adminViewModel.closeUserBalanceDialog() }
        )
    }

    // 2. User Account Deletion Confirmation Modal
    state.userToDelete?.let { user ->
        UserDeleteConfirmDialog(
            user = user,
            onConfirm = { adminViewModel.executeDeleteUser() },
            onDismiss = { adminViewModel.cancelDeleteUser() }
        )
    }

    // 3. User Transactions & Ledger Modal
    state.selectedUserForTransactions?.let { user ->
        UserTransactionsHistoryDialog(
            user = user,
            adminViewModel = adminViewModel,
            onDismiss = { adminViewModel.closeUserTransactions() }
        )
    }
}

// ==========================================
// COMPONENT CARDS & SUB-VIEWS
// ==========================================

@Composable
private fun AdminUserMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    neonColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = neonColor)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(CarbonCardElevated, CarbonSurface)))
            .border(1.dp, neonColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.sp
                    ),
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = neonColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = neonColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SubTabButton(
    title: String,
    isSelected: Boolean,
    neonColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) neonColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) neonColor.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) neonColor else TextMuted
        )
    }
}

@Composable
private fun ActivityFilterChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else CarbonSurface)
            .border(
                1.dp,
                if (isSelected) activeColor else CarbonBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) activeColor else TextMuted
        )
    }
}

/**
 * High-End User Control Card with Balance Adjustment & Account Deletion Controls
 */
@Composable
private fun SignedUpUserControlCard(
    user: User,
    onOpenBalanceControl: () -> Unit,
    onViewTransactions: () -> Unit,
    onDeleteAccount: () -> Unit,
    onGiftBonus: () -> Unit,
    onToggleBan: () -> Unit
) {
    val dateStr = try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(user.createdAt))
    } catch (_: Exception) {
        "Recent"
    }

    val status = user.activityStatus
    val statusColor = when (status) {
        UserActivityStatus.ONLINE -> NeonGreen
        UserActivityStatus.INACTIVE_30_DAYS -> Color(0xFFF59E0B)
        UserActivityStatus.BANNED -> DangerRed
        UserActivityStatus.OFFLINE -> TextMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonCard)
            .border(
                1.2.dp,
                when (status) {
                    UserActivityStatus.ONLINE -> NeonGreen.copy(alpha = 0.6f)
                    UserActivityStatus.INACTIVE_30_DAYS -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                    UserActivityStatus.BANNED -> DangerRed.copy(alpha = 0.6f)
                    UserActivityStatus.OFFLINE -> CarbonBorder
                },
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            // Top Row: Avatar, IGN, Real Name, and Online / Inactive / Offline Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(statusColor.copy(alpha = 0.35f), statusColor.copy(alpha = 0.08f))
                            )
                        )
                        .border(1.2.dp, statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.ffIgn.take(2).uppercase().ifBlank { "FF" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.ffIgn,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Dynamic Status Badge (ONLINE / INACTIVE 30d+ / OFFLINE / BANNED)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(0.8.dp, statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (status) {
                                    UserActivityStatus.ONLINE -> "🟢 ONLINE"
                                    UserActivityStatus.INACTIVE_30_DAYS -> "⚠️ INACTIVE (30d+)"
                                    UserActivityStatus.BANNED -> "⛔ SUSPENDED"
                                    UserActivityStatus.OFFLINE -> "⚪ OFFLINE (${user.daysSinceLastActive}d)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = statusColor
                            )
                        }
                    }

                    Text(
                        text = "${user.fullName} • Reg: $dateStr",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }

                // Total Wallet Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${user.walletBalance.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = CyberGoldLight
                    )
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Credentials Strip: Mobile & Game UID
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CarbonSurface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+91 ${user.mobileNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyanLight
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = CyberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "UID: ${user.ffUid}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyberGoldLight
                    )
                }

                Text(
                    text = "${user.matchesPlayed} Matches • ${user.totalKills} Kills",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detailed Balance Breakdown
            Text(
                text = "Deposit: ₹${user.depositBalance.toInt()} | Winning: ₹${user.winningBalance.toInt()} | Bonus: ₹${user.bonusBalance.toInt()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = CarbonBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // VIEW ALL TRANSACTIONS BUTTON (Withdrawals, Add Cash, Entry Fees, Refund Fees)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonCyan.copy(alpha = 0.12f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onViewTransactions() }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📜 View All Transactions (Withdrawals, Add Cash, Entry, Refund)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = NeonCyanLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ADMIN CONTROL ACTION BUTTONS (Balance Control, Delete Account, Gift Bonus, Ban)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Balance Control Button
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberGold.copy(alpha = 0.15f))
                        .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onOpenBalanceControl() }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = CyberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Balance Control",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = CyberGoldLight
                        )
                    }
                }

                // 2. Gift Bonus (+₹50)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.12f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onGiftBonus() }
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Redeem,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+₹50 Gift",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = NeonGreen
                        )
                    }
                }

                // 3. Ban / Unban Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (user.isBanned) DangerRed.copy(alpha = 0.25f) else CarbonSurface)
                        .border(1.dp, if (user.isBanned) DangerRed else CarbonBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleBan() }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (user.isBanned) "Unban" else "Ban",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = if (user.isBanned) DangerRed else TextMuted
                    )
                }

                // 4. Delete Account Permanently
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, DangerRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onDeleteAccount() }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to adjust Deposit, Winning, and Bonus balances for a registered player
 */
@Composable
private fun UserBalanceControlDialog(
    user: User,
    depositInput: String,
    winningInput: String,
    bonusInput: String,
    onFieldsChanged: (deposit: String?, winning: String?, bonus: String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCardElevated,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = CyberGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "USER BALANCE CONTROL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CyberGold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${user.ffIgn} • +91 ${user.mobileNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Total Balance Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CarbonSurface)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Wallet Total",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${user.walletBalance.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = CyberGoldLight
                        )
                    }
                }

                // 1. Deposit Balance Input
                OutlinedTextField(
                    value = depositInput,
                    onValueChange = { onFieldsChanged(it, null, null) },
                    label = { Text("Deposit Balance (₹)") },
                    singleLine = true,
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Winning Balance Input
                OutlinedTextField(
                    value = winningInput,
                    onValueChange = { onFieldsChanged(null, it, null) },
                    label = { Text("Winning Balance (₹)") },
                    singleLine = true,
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Bonus Balance Input
                OutlinedTextField(
                    value = bonusInput,
                    onValueChange = { onFieldsChanged(null, null, it) },
                    label = { Text("Bonus Balance (₹)") },
                    singleLine = true,
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Increment Chips
                Text(
                    text = "QUICK ADD DEPOSIT (₹)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = NeonCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(50, 100, 200, 500).forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonSurface)
                                .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = depositInput.toDoubleOrNull() ?: 0.0
                                    onFieldsChanged((current + amount).toInt().toString(), null, null)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+₹$amount",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonCyanLight
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Neon3DButton(
                text = "SAVE NEW BALANCE",
                onClick = onSave,
                style = Neon3DStyle.GOLD,
                modifier = Modifier.testTag("admin_save_balance_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

/**
 * Confirmation dialog before permanently deleting a user account
 */
@Composable
private fun UserDeleteConfirmDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCardElevated,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "DELETE USER ACCOUNT?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = DangerRed
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to permanently delete the account for:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonSurface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "${user.ffIgn} (${user.fullName})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Text(
                            text = "Mobile: +91 ${user.mobileNumber} • UID: ${user.ffUid}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "Remaining Balance: ₹${user.walletBalance.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberGoldLight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ This action cannot be undone. All wallet balance, match stats, and active sessions will be deleted permanently.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = DangerRed.copy(alpha = 0.9f)
                )
            }
        },
        confirmButton = {
            Neon3DButton(
                text = "CONFIRM DELETE",
                onClick = onConfirm,
                style = Neon3DStyle.RED,
                modifier = Modifier.testTag("admin_confirm_delete_user_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

/**
 * Item for Match Roster
 */
@Composable
private fun ParticipantRosterItem(
    index: Int,
    player: progamerbattle.com.model.ParticipantRecord
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonCard)
            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$index",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = CyberGold,
                    modifier = Modifier.width(32.dp)
                )
                Column {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "UID: ${player.uidNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonCyan.copy(alpha = 0.12f))
                    .border(0.8.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Enrolled",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = NeonCyanLight
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarbonSurface)
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CarbonBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CarbonSurface,
    unfocusedContainerColor = CarbonSurface
)

/**
 * Modal dialog showing all user transactions (Withdrawals, Add Cash, Entry Fees, Refund Fees)
 */
@Composable
fun UserTransactionsHistoryDialog(
    user: User,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    val state by adminViewModel.uiState.collectAsState()
    val allUserTransactions: List<progamerbattle.com.model.WalletTransaction> = remember(user.mobileNumber) {
        adminViewModel.getUserTransactions(user)
    }

    val selectedCategory = state.selectedTransactionFilter

    val filteredTransactions: List<progamerbattle.com.model.WalletTransaction> = if (selectedCategory == null) {
        allUserTransactions
    } else {
        allUserTransactions.filter { it.category == selectedCategory }
    }

    // Counts for filter chips
    val allCount = allUserTransactions.size
    val addCashCount = allUserTransactions.count { it.category == progamerbattle.com.model.TransactionCategory.ADD_CASH }
    val entryFeeCount = allUserTransactions.count { it.category == progamerbattle.com.model.TransactionCategory.ENTRY_FEE }
    val refundCount = allUserTransactions.count { it.category == progamerbattle.com.model.TransactionCategory.REFUND }
    val withdrawalCount = allUserTransactions.count { it.category == progamerbattle.com.model.TransactionCategory.WITHDRAWAL }
    val winningsCount = allUserTransactions.count { it.category == progamerbattle.com.model.TransactionCategory.WINNINGS || it.category == progamerbattle.com.model.TransactionCategory.BONUS }

    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCardElevated,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TRANSACTION HISTORY & LEDGER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = NeonCyan
                        )
                        Text(
                            text = "${user.fullName} (${user.ffIgn})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    // Current Total Balance Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberGold.copy(alpha = 0.15f))
                            .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "₹${user.walletBalance.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = CyberGoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+91 ${user.mobileNumber}  •  UID: ${user.ffUid}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
                Text(
                    text = "Deposit: ₹${user.depositBalance.toInt()} | Winning: ₹${user.winningBalance.toInt()} | Bonus: ₹${user.bonusBalance.toInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // CATEGORY FILTER CHIPS
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterCategoryChip(
                            label = "All ($allCount)",
                            isSelected = selectedCategory == null,
                            activeColor = NeonCyan,
                            onClick = { adminViewModel.selectTransactionCategoryFilter(null) }
                        )
                    }
                    item {
                        FilterCategoryChip(
                            label = "Add Cash ($addCashCount)",
                            isSelected = selectedCategory == progamerbattle.com.model.TransactionCategory.ADD_CASH,
                            activeColor = NeonGreen,
                            onClick = { adminViewModel.selectTransactionCategoryFilter(progamerbattle.com.model.TransactionCategory.ADD_CASH) }
                        )
                    }
                    item {
                        FilterCategoryChip(
                            label = "Entry Fee ($entryFeeCount)",
                            isSelected = selectedCategory == progamerbattle.com.model.TransactionCategory.ENTRY_FEE,
                            activeColor = Color(0xFFF97316),
                            onClick = { adminViewModel.selectTransactionCategoryFilter(progamerbattle.com.model.TransactionCategory.ENTRY_FEE) }
                        )
                    }
                    item {
                        FilterCategoryChip(
                            label = "Refund Fee ($refundCount)",
                            isSelected = selectedCategory == progamerbattle.com.model.TransactionCategory.REFUND,
                            activeColor = Color(0xFF06B6D4),
                            onClick = { adminViewModel.selectTransactionCategoryFilter(progamerbattle.com.model.TransactionCategory.REFUND) }
                        )
                    }
                    item {
                        FilterCategoryChip(
                            label = "Withdrawal ($withdrawalCount)",
                            isSelected = selectedCategory == progamerbattle.com.model.TransactionCategory.WITHDRAWAL,
                            activeColor = DangerRed,
                            onClick = { adminViewModel.selectTransactionCategoryFilter(progamerbattle.com.model.TransactionCategory.WITHDRAWAL) }
                        )
                    }
                    item {
                        FilterCategoryChip(
                            label = "Prize/Bonus ($winningsCount)",
                            isSelected = selectedCategory == progamerbattle.com.model.TransactionCategory.WINNINGS,
                            activeColor = CyberGold,
                            onClick = { adminViewModel.selectTransactionCategoryFilter(progamerbattle.com.model.TransactionCategory.WINNINGS) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TRANSACTION ITEMS LIST
                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions found in this category.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { tx ->
                            TransactionLedgerItemCard(tx = tx, sdf = sdf)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Neon3DButton(
                text = "CLOSE LEDGER",
                onClick = onDismiss,
                style = Neon3DStyle.CYAN
            )
        }
    )
}

@Composable
private fun FilterCategoryChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else CarbonSurface)
            .border(
                1.dp,
                if (isSelected) activeColor else CarbonBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = if (isSelected) activeColor else TextSecondary
        )
    }
}

@Composable
private fun TransactionLedgerItemCard(
    tx: progamerbattle.com.model.WalletTransaction,
    sdf: SimpleDateFormat
) {
    val isCredit = tx.type == progamerbattle.com.model.TransactionType.CREDIT

    val categoryColor = when (tx.category) {
        progamerbattle.com.model.TransactionCategory.ADD_CASH -> NeonGreen
        progamerbattle.com.model.TransactionCategory.ENTRY_FEE -> Color(0xFFF97316)
        progamerbattle.com.model.TransactionCategory.REFUND -> Color(0xFF06B6D4)
        progamerbattle.com.model.TransactionCategory.WITHDRAWAL -> DangerRed
        progamerbattle.com.model.TransactionCategory.WINNINGS -> CyberGold
        progamerbattle.com.model.TransactionCategory.BONUS -> ElectricPurple
    }

    val dateFormatted = try {
        sdf.format(Date(tx.timestamp))
    } catch (_: Exception) {
        "Recent"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CarbonSurface)
            .border(1.dp, CarbonBorder.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Row 1: Category Badge, Status Badge & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .border(0.8.dp, categoryColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.category.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp
                            ),
                            color = categoryColor
                        )
                    }

                    // Status
                    Text(
                        text = tx.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = when (tx.status.uppercase()) {
                            "SUCCESS", "APPROVED" -> NeonGreen
                            "PENDING" -> Color(0xFFF59E0B)
                            "REJECTED", "FAILED" -> DangerRed
                            else -> TextMuted
                        }
                    )
                }

                // Amount
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${tx.amount.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = if (isCredit) NeonGreen else DangerRed
                )
            }

            // Row 2: Title
            Text(
                text = tx.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            // Row 3: Subtitle / Description / Match Name
            if (tx.subtitle.isNotBlank()) {
                Text(
                    text = tx.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }

            // Row 4: Timestamp & Method & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dateFormatted • ${tx.paymentMethod}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextMuted
                )
                Text(
                    text = "ID: ${tx.id.takeLast(10)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = TextMuted.copy(alpha = 0.7f)
                )
            }
        }
    }
}
