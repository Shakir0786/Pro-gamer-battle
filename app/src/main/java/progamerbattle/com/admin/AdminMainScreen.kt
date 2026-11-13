package progamerbattle.com.admin

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.MainActivity
import progamerbattle.com.R
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    adminViewModel: AdminViewModel
) {
    val state by adminViewModel.uiState.collectAsState()
    val deposits by adminViewModel.deposits.collectAsState()
    val withdrawals by adminViewModel.withdrawals.collectAsState()
    val pendingPaymentsCount = deposits.count { it.status.equals("Pending", ignoreCase = true) } +
        withdrawals.count { it.status.equals("Pending", ignoreCase = true) }
    val context = LocalContext.current

    // Display feedback toast
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearToast()
        }
    }

    // PIN lock screen
    if (!state.isAuthenticated) {
        AdminAuthScreen(adminViewModel = adminViewModel)
        return
    }

    val isSubPage = state.selectedTab == AdminTab.PLAYER_LISTS || state.selectedTab == AdminTab.RESULTS
    BackHandler(enabled = isSubPage) {
        adminViewModel.selectTab(AdminTab.DASHBOARD)
    }

    val topTitle = when (state.selectedTab) {
        AdminTab.PLAYER_LISTS -> "Player Rosters"
        AdminTab.RESULTS -> "Declare Results"
        AdminTab.TOURNAMENTS -> "Custom Rooms"
        AdminTab.WITHDRAWALS -> "Payouts & Verify"
        AdminTab.SETTINGS -> "App Controls"
        AdminTab.DASHBOARD -> "PGB Admin"
    }

    Scaffold(
        containerColor = CarbonBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (isSubPage) {
                        IconButton(
                            onClick = { adminViewModel.selectTab(AdminTab.DASHBOARD) },
                            modifier = Modifier.testTag("admin_back_to_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isSubPage) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_admin_3d),
                                contentDescription = "Admin Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .border(1.dp, CyberGold.copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                            )
                        }
                        Text(
                            text = topTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            ),
                            color = TextPrimary
                        )
                        if (!isSubPage) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("admin_switch_to_player_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Player App",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Player",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = NeonCyanLight
                        )
                    }

                    IconButton(
                        onClick = { adminViewModel.logOutAdmin() },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out Admin",
                            tint = DangerRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarbonBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (!isSubPage) {
                AdminCleanBottomNav(
                    selectedTab = state.selectedTab,
                    pendingPaymentsCount = pendingPaymentsCount,
                    onTabSelected = { adminViewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AdminTabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetTab ->
            when (targetTab) {
                AdminTab.DASHBOARD -> AdminDashboardScreen(adminViewModel = adminViewModel)
                AdminTab.TOURNAMENTS -> AdminTournamentsScreen(adminViewModel = adminViewModel)
                AdminTab.PLAYER_LISTS -> AdminPlayerListsScreen(adminViewModel = adminViewModel)
                AdminTab.RESULTS -> AdminResultsScreen(adminViewModel = adminViewModel)
                AdminTab.WITHDRAWALS -> AdminWithdrawalsScreen(adminViewModel = adminViewModel)
                AdminTab.SETTINGS -> AdminSettingsScreen(adminViewModel = adminViewModel)
            }
        }

        // Global Modal: Create New Tournament (accessible from Home and Rooms tabs)
        AdminCreateMatchDialog(adminViewModel = adminViewModel)
    }
}

private data class AdminNavDestination(
    val tab: AdminTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color
)

@Composable
private fun AdminCleanBottomNav(
    selectedTab: AdminTab,
    pendingPaymentsCount: Int,
    onTabSelected: (AdminTab) -> Unit
) {
    val destinations = listOf(
        AdminNavDestination(
            tab = AdminTab.DASHBOARD,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            accentColor = CyberGold
        ),
        AdminNavDestination(
            tab = AdminTab.TOURNAMENTS,
            label = "Rooms",
            selectedIcon = Icons.Filled.SportsEsports,
            unselectedIcon = Icons.Outlined.SportsEsports,
            accentColor = NeonCyan
        ),
        AdminNavDestination(
            tab = AdminTab.WITHDRAWALS,
            label = "Payouts",
            selectedIcon = Icons.Filled.AccountBalanceWallet,
            unselectedIcon = Icons.Outlined.AccountBalanceWallet,
            accentColor = CyberGold
        ),
        AdminNavDestination(
            tab = AdminTab.SETTINGS,
            label = "Controls",
            selectedIcon = Icons.Filled.Tune,
            unselectedIcon = Icons.Outlined.Tune,
            accentColor = NeonCyan
        )
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = CarbonBorder.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        NavigationBar(
            containerColor = CarbonCard,
            contentColor = TextPrimary,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            destinations.forEach { dest ->
                val isSelected = selectedTab == dest.tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(dest.tab) },
                    modifier = Modifier.testTag("admin_nav_${dest.label.lowercase()}"),
                    icon = {
                        val iconVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon
                        if (dest.tab == AdminTab.WITHDRAWALS && pendingPaymentsCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = DangerRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (pendingPaymentsCount > 99) "99+" else "$pendingPaymentsCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = dest.label,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = dest.accentColor,
                        selectedTextColor = dest.accentColor,
                        indicatorColor = dest.accentColor.copy(alpha = 0.14f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}
