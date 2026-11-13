package progamerbattle.com.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import progamerbattle.com.R
import progamerbattle.com.ui.components.AppDrawerContent
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.viewmodel.AuthViewModel
import progamerbattle.com.viewmodel.MatchViewModel
import progamerbattle.com.viewmodel.WalletViewModel

enum class Screen(val route: String, val title: String) {
    HOME("home", "Lobby"),
    MY_MATCHES("my_matches", "My Matches"),
    WALLET("wallet", "Wallet"),
    REFER_EARN("refer_earn", "Refer & Earn"),
    PROFILE("profile", "Profile"),
    SETTINGS("settings", "Settings"),
    RULES("rules", "Fair Play & Rules"),
    SUPPORT("support", "Help & Support")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    matchViewModel: MatchViewModel,
    walletViewModel: WalletViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    val remoteConfigManager = remember { progamerbattle.com.data.RemoteConfigManager.getInstance() }
    val remoteConfig by remoteConfigManager.config.collectAsState()

    // 1. Intercept Maintenance Mode from Remote Config
    if (remoteConfig.isMaintenanceMode) {
        MaintenanceModeScreen(
            config = remoteConfig,
            onRefreshStatus = { remoteConfigManager.fetchNow() }
        )
        return
    }

    // 2. Intercept Mandatory Update if min_version > installed app version
    if (remoteConfig.isUpdateRequired) {
        progamerbattle.com.ui.components.ForceUpdateDialog(config = remoteConfig)
    }

    // If not logged in, show AuthScreen (Mobile & PIN login/register)
    if (currentUser == null) {
        AuthScreen(
            authViewModel = authViewModel,
            onAuthSuccess = { /* state triggers recomposition automatically */ }
        )
        return
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Back handling: if drawer open, close it; if not on home, go to home
    BackHandler(enabled = drawerState.isOpen || currentScreen != Screen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentScreen = Screen.HOME
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                user = currentUser,
                currentDestination = currentScreen.route,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    when (route) {
                        "home" -> currentScreen = Screen.HOME
                        "my_matches" -> currentScreen = Screen.MY_MATCHES
                        "wallet" -> currentScreen = Screen.WALLET
                        "refer_earn" -> currentScreen = Screen.REFER_EARN
                        "profile" -> currentScreen = Screen.PROFILE
                        "settings" -> currentScreen = Screen.SETTINGS
                        "rules" -> currentScreen = Screen.RULES
                        "support" -> currentScreen = Screen.SUPPORT
                    }
                },
                onLogoutClick = {
                    coroutineScope.launch { drawerState.close() }
                    authViewModel.signOut()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = cyberSurfaces.background,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentScreen == Screen.HOME || currentScreen == Screen.MY_MATCHES || currentScreen == Screen.WALLET || currentScreen == Screen.PROFILE) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo_user_3d),
                                    contentDescription = "Pro GamerBattle Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            1.dp,
                                            if (isDark) NeonCyan.copy(alpha = 0.7f) else Color(0xFF009EB3),
                                            RoundedCornerShape(10.dp)
                                        )
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                if (isDark) {
                                                    listOf(NeonCyan.copy(alpha = 0.35f), NeonCyan.copy(alpha = 0.08f))
                                                } else {
                                                    listOf(Color(0xFF009EB3).copy(alpha = 0.25f), Color(0xFF009EB3).copy(alpha = 0.05f))
                                                }
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            if (isDark) NeonCyan.copy(alpha = 0.6f) else Color(0xFF009EB3),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (currentScreen) {
                                            Screen.SETTINGS -> Icons.Default.Settings
                                            Screen.RULES -> Icons.Default.Gavel
                                            Screen.SUPPORT -> Icons.AutoMirrored.Filled.Help
                                            Screen.REFER_EARN -> Icons.Default.Share
                                            else -> Icons.Default.SportsEsports
                                        },
                                        contentDescription = null,
                                        tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (currentScreen) {
                                        Screen.SETTINGS -> "SETTINGS"
                                        Screen.RULES -> "FAIR PLAY & RULES"
                                        Screen.SUPPORT -> "HELP & SUPPORT"
                                        Screen.REFER_EARN -> "REFER & EARN CASH"
                                        else -> "PRO GAMERBATTLE"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = cyberSurfaces.textPrimary
                                )
                                Text(
                                    text = when (currentScreen) {
                                        Screen.RULES -> "OFFICIAL ESPORTS RULES"
                                        Screen.SUPPORT -> "24/7 SUPPORT & FAQS"
                                        Screen.SETTINGS -> "APP PREFERENCES"
                                        Screen.REFER_EARN -> "INVITE & EARN ₹10 CASH"
                                        else -> "BATTLE GAMING ESPORTS HUB"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 9.sp
                                    ),
                                    color = if (isDark) NeonCyanLight else Color(0xFF0284C7)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        val isSecondaryScreen = currentScreen != Screen.HOME
                        IconButton(
                            onClick = {
                                if (isSecondaryScreen) {
                                    currentScreen = Screen.HOME
                                } else {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            },
                            modifier = Modifier.testTag("drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isSecondaryScreen) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                                contentDescription = if (isSecondaryScreen) "Back to Home" else "Open Drawer",
                                tint = if (isDark) NeonCyan else Color(0xFF007A8A)
                            )
                        }
                    },
                    actions = {
                        // 3D Cyber Vault Quick Balance Pill
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = CyberGold)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(cyberSurfaces.cardElevated, cyberSurfaces.card)
                                    )
                                )
                                .border(
                                    1.2.dp,
                                    Brush.verticalGradient(
                                        listOf(CyberGoldLight, CyberGold.copy(alpha = 0.4f))
                                    ),
                                    RoundedCornerShape(22.dp)
                                )
                                .clickable { currentScreen = Screen.WALLET }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("top_bar_wallet_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(CyberGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = CyberGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${(currentUser.walletBalance).toInt()}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (isDark) CyberGoldLight else Color(0xFF92400E)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cyberSurfaces.background,
                        titleContentColor = cyberSurfaces.textPrimary
                    )
                )
            },
            bottomBar = {
                // High-End 3D Cyber Navigation Dock
                Neon3DCyberDock(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> HomeScreen(
                        matchViewModel = matchViewModel,
                        currentUser = currentUser,
                        onNavigateToWallet = { neededAmount ->
                            walletViewModel.openDepositSheet(neededAmount)
                            currentScreen = Screen.WALLET
                        },
                        announcementMessage = remoteConfig.announcementMessage
                    )
                    Screen.MY_MATCHES -> MyMatchesScreen(
                        matchViewModel = matchViewModel,
                        currentUser = currentUser
                    )
                    Screen.WALLET -> WalletScreen(
                        walletViewModel = walletViewModel,
                        currentUser = currentUser,
                        whatsappLink = remoteConfig.whatsappLink
                    )
                    Screen.REFER_EARN -> ReferAndEarnScreen(
                        currentUser = currentUser
                    )
                    Screen.PROFILE -> ProfileScreen(
                        authViewModel = authViewModel,
                        currentUser = currentUser,
                        onNavigateToWallet = { currentScreen = Screen.WALLET }
                    )
                    Screen.SETTINGS -> SettingsScreen(currentUser = currentUser)
                    Screen.RULES -> RulesScreen()
                    Screen.SUPPORT -> SupportScreen(
                        whatsappLink = remoteConfig.whatsappLink,
                        telegramLink = remoteConfig.telegramLink
                    )
                }
            }

            // 2. Force Update Dialog if min_version requires it
            if (remoteConfig.isUpdateRequired) {
                progamerbattle.com.ui.components.ForceUpdateDialog(config = remoteConfig)
            }
        }
    }
}

/**
 * 3D Floating Cyber Dock with tactile pressed depth, neon halo indicators,
 * and edge-to-edge navigation insets handling.
 */
@Composable
private fun Neon3DCyberDock(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Floating 3D Base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = if (isDark) NeonCyan.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(cyberSurfaces.dockGradient))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        if (isDark) {
                            listOf(
                                SpecularHighlightBright.copy(alpha = 0.3f),
                                cyberSurfaces.border,
                                cyberSurfaces.border.copy(alpha = 0.2f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.8f),
                                cyberSurfaces.border,
                                cyberSurfaces.border.copy(alpha = 0.5f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberDockItem(
                    label = "Lobby",
                    icon = Icons.Default.EmojiEvents,
                    selected = currentScreen == Screen.HOME,
                    neonColor = if (isDark) NeonCyan else Color(0xFF007A8A),
                    testTag = "nav_home",
                    onClick = { onScreenSelected(Screen.HOME) }
                )
                CyberDockItem(
                    label = "Matches",
                    icon = Icons.Default.MilitaryTech,
                    selected = currentScreen == Screen.MY_MATCHES,
                    neonColor = if (isDark) NeonMagenta else Color(0xFFC2185B),
                    testTag = "nav_my_matches",
                    onClick = { onScreenSelected(Screen.MY_MATCHES) }
                )
                CyberDockItem(
                    label = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    selected = currentScreen == Screen.WALLET,
                    neonColor = if (isDark) CyberGold else Color(0xFFB45309),
                    testTag = "nav_wallet",
                    onClick = { onScreenSelected(Screen.WALLET) }
                )
                CyberDockItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    selected = currentScreen == Screen.PROFILE,
                    neonColor = if (isDark) ElectricPurple else Color(0xFF6B21A8),
                    testTag = "nav_profile",
                    onClick = { onScreenSelected(Screen.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun CyberDockItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    neonColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "dock_item_press"
    )

    Box(
        modifier = Modifier
            .testTag(testTag)
            .offset(y = pressOffset)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                if (selected) {
                    Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(neonColor.copy(alpha = 0.22f), neonColor.copy(alpha = 0.05f))
                            )
                        )
                        .border(
                            1.dp,
                            neonColor.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selected && isDark) {
                    // 3D Neon Backlight Halo
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .drawBehind {
                                drawCircle(
                                    color = neonColor.copy(alpha = 0.45f),
                                    radius = size.width * 0.7f
                                )
                            }
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) neonColor else cyberSurfaces.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (selected) neonColor else cyberSurfaces.textMuted,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium
            )
        }
    }
}
