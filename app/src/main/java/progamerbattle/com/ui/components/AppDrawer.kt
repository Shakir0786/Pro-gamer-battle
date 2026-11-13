package progamerbattle.com.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.User
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldDark
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.LocalThemeController
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright

/**
 * High-End 3D Glassmorphic Drawer Panel with dynamic Cyber-Dark and High-Contrast Light mode rendering
 */
@Composable
fun AppDrawerContent(
    user: User?,
    currentDestination: String,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val themeController = LocalThemeController.current
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = themeController.isDarkTheme
    val context = LocalContext.current

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = Color.Transparent
    ) {
        // High-End 3D Glassmorphic Panel (Adapts to both cyber-dark and high-contrast light)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                    ambientColor = if (isDark) NeonCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black
                )
                .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(cyberSurfaces.drawerGlassGradient))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(cyberSurfaces.drawerBorderGradient),
                    shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                )
        ) {
            // Cyber-Neon Corner Streaks (Ambient Lighting for 3D Depth)
            CyberCornerStreaks(isDark = isDark)

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP SECTION
                Column {
                    // Glowing Dual-Tone App Title & Esports Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PRO ",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = if (isDark) NeonCyan else Color(0xFF007A8A)
                                )
                                Text(
                                    text = "GAMERBATTLE",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = if (isDark) CyberGold else Color(0xFFB45309)
                                )
                            }
                            Text(
                                text = "BATTLE GAMING ESPORTS HUB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 9.sp
                                ),
                                color = if (isDark) NeonCyanLight.copy(alpha = 0.8f) else Color(0xFF0284C7)
                            )
                        }

                        // Small 3D Shield Badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) NeonCyan.copy(alpha = 0.15f) else Color(0xFF009EB3).copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    if (isDark) NeonCyan.copy(alpha = 0.6f) else Color(0xFF009EB3),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Avatar Framed with Bright Neon Border & Glow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isDark) {
                                        listOf(Color(0x3300F0FF), Color(0x1512192E), Color.Transparent)
                                    } else {
                                        listOf(Color(0x22009EB3), Color(0x11CBD5E1), Color.Transparent)
                                    }
                                )
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    if (isDark) {
                                        listOf(NeonCyan.copy(alpha = 0.7f), cyberSurfaces.border.copy(alpha = 0.3f))
                                    } else {
                                        listOf(Color(0xFF009EB3).copy(alpha = 0.8f), cyberSurfaces.border)
                                    }
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar framed with glowing double neon ring
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = if (isDark) NeonCyan.copy(alpha = 0.4f) else Color(0xFF009EB3).copy(alpha = 0.25f),
                                        radius = size.width * 0.65f
                                    )
                                }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        if (isDark) listOf(Color(0xFF003844), Color(0xFF04101A))
                                        else listOf(Color(0xFFE0F7FA), Color(0xFFFFFFFF))
                                    )
                                )
                                .border(2.dp, if (isDark) NeonCyan else Color(0xFF009EB3), CircleShape)
                                .border(3.5.dp, if (isDark) NeonCyanLight.copy(alpha = 0.35f) else Color(0x33009EB3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "User Avatar",
                                tint = if (isDark) NeonCyanLight else Color(0xFF007A8A),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.inGameName ?: "ProGamer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = cyberSurfaces.textPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) CyberGold else Color(0xFFD97706))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UID: ${user?.inGameUid?.ifBlank { "Not Set" } ?: "Not Set"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isDark) CyberGoldLight else Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Carbon-Fiber Textured Wallet Card with Gold Coin & Compact Glowing "Add Cash"
                    CarbonFiberWalletCard(
                        walletBalance = user?.walletBalance ?: 0.0,
                        onAddCashClick = { onNavigate("wallet") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section Divider
                    HorizontalDivider(
                        color = cyberSurfaces.border.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Crisp Dual-Tone Cyan & Amber 3D Outline Navigation Items
                    GlassmorphicNavItem(
                        title = "Tournaments Lobby",
                        icon = Icons.Default.EmojiEvents,
                        isSelected = currentDestination == "home",
                        testTag = "drawer_item_lobby",
                        onClick = { onNavigate("home") }
                    )
                    GlassmorphicNavItem(
                        title = "My Matches",
                        icon = Icons.Default.MilitaryTech,
                        isSelected = currentDestination == "my_matches",
                        testTag = "drawer_item_my_matches",
                        onClick = { onNavigate("my_matches") }
                    )
                    GlassmorphicNavItem(
                        title = "Gamer Wallet",
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = currentDestination == "wallet",
                        testTag = "drawer_item_wallet",
                        onClick = { onNavigate("wallet") }
                    )
                    GlassmorphicNavItem(
                        title = "Refer & Earn (Win ₹10)",
                        icon = Icons.Default.Share,
                        isSelected = currentDestination == "refer_earn",
                        testTag = "drawer_item_refer_earn",
                        onClick = { onNavigate("refer_earn") }
                    )
                    GlassmorphicNavItem(
                        title = "My Profile & Stats",
                        icon = Icons.Default.Person,
                        isSelected = currentDestination == "profile",
                        testTag = "drawer_item_profile",
                        onClick = { onNavigate("profile") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = cyberSurfaces.border.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassmorphicNavItem(
                        title = "Fair Play & Rules",
                        icon = Icons.Default.Gavel,
                        isSelected = currentDestination == "rules",
                        testTag = "drawer_item_rules",
                        onClick = { onNavigate("rules") }
                    )
                    GlassmorphicNavItem(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        isSelected = currentDestination == "settings",
                        testTag = "drawer_item_settings",
                        onClick = { onNavigate("settings") }
                    )
                    GlassmorphicNavItem(
                        title = "Help & Support",
                        icon = Icons.AutoMirrored.Filled.Help,
                        isSelected = currentDestination == "support",
                        testTag = "drawer_item_support",
                        onClick = { onNavigate("support") }
                    )
                }

                // BOTTOM SECTION: Theme Mode Switcher + Soft-Glowing Logout Button
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Sleek Glowing Theme Mode Switcher (Dark vs High-Contrast Light)
                    GlassmorphicThemeSwitcher(
                        isDark = isDark,
                        onToggle = { themeController.toggleTheme() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Redesigned Soft-Glowing Logout Button
                    SoftGlowingLogoutButton(onClick = onLogoutClick)

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

/**
 * Cyber-Neon Corner Streaks drawn behind panel content for high-tech specular depth
 */
@Composable
private fun CyberCornerStreaks(isDark: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-right cyan streak
        drawPath(
            path = Path().apply {
                moveTo(w - 70f, 0f)
                lineTo(w, 0f)
                lineTo(w, 90f)
                close()
            },
            brush = Brush.radialGradient(
                colors = listOf(
                    (if (isDark) NeonCyan else Color(0xFF009EB3)).copy(alpha = if (isDark) 0.35f else 0.2f),
                    Color.Transparent
                ),
                center = Offset(w, 0f),
                radius = 120f
            )
        )

        // Top-left cyber line
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    (if (isDark) NeonCyan else Color(0xFF009EB3)).copy(alpha = 0.8f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, 0f),
            end = Offset(w * 0.45f, 0f),
            strokeWidth = 3f
        )

        // Bottom-right amber streak
        drawPath(
            path = Path().apply {
                moveTo(w, h - 110f)
                lineTo(w, h)
                lineTo(w - 90f, h)
                close()
            },
            brush = Brush.radialGradient(
                colors = listOf(
                    (if (isDark) CyberGold else Color(0xFFF59E0B)).copy(alpha = if (isDark) 0.3f else 0.18f),
                    Color.Transparent
                ),
                center = Offset(w, h),
                radius = 140f
            )
        )
    }
}

/**
 * Carbon-Fiber Textured Wallet Card prominently displaying balance with
 * a 3D gold coin icon, status chip, and a perfectly fitted full-width "Add Cash" button.
 */
@Composable
private fun CarbonFiberWalletCard(
    walletBalance: Double,
    onAddCashClick: () -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) CyberGold.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    cyberSurfaces.walletCardGradient,
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            )
            // Carbon-fiber weave pattern overlay
            .drawBehind {
                val step = 8.dp.toPx()
                var x = 0f
                val lineColor = if (isDark) Color(0x18FFFFFF) else Color(0x0E000000)
                while (x < size.width + size.height) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x - size.height, size.height),
                        strokeWidth = 1.5f
                    )
                    x += step
                }
            }
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    if (isDark) {
                        listOf(CyberGold.copy(alpha = 0.85f), NeonCyan.copy(alpha = 0.5f), cyberSurfaces.border)
                    } else {
                        listOf(Color(0xFFD97706), Color(0xFF009EB3), cyberSurfaces.border)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Balance with Gold Coin Icon & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3D Embossed Gold Coin Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(4.dp, CircleShape, spotColor = CyberGold)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyberGoldLight, CyberGold, CyberGoldDark)
                                )
                            )
                            .border(1.5.dp, SpecularHighlightBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Gold Coin",
                            tint = Color(0xFF2A1900),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "VAULT BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            ),
                            color = cyberSurfaces.textSecondary
                        )
                        Text(
                            text = "₹${walletBalance.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (isDark) CyberGoldLight else Color(0xFF92400E)
                        )
                    }
                }

                // Active Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Perfectly fitted full-width 3D Add Cash action button
            DrawerAddCashButton(onClick = onAddCashClick)
        }
    }
}

/**
 * Perfectly fitted 3D "Add Cash" button with tactile press animation and neon cyan glow
 */
@Composable
private fun DrawerAddCashButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "add_cash_press"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = pressOffset)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = NeonCyan,
                spotColor = NeonCyan
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(NeonCyan, Color(0xFF00B4D8))
                )
            )
            .border(1.dp, SpecularHighlightBright, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 9.dp)
            .testTag("drawer_add_cash_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF070913),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+ ADD CASH TO WALLET",
                color = Color(0xFF070913),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Crisp Dual-Tone Cyan & Amber 3D Outline Navigation Item
 */
@Composable
private fun GlassmorphicNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
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
        label = "nav_item_press"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .offset(y = pressOffset)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            Brush.horizontalGradient(
                                if (isDark) {
                                    listOf(NeonCyan.copy(alpha = 0.22f), CyberGold.copy(alpha = 0.08f), Color.Transparent)
                                } else {
                                    listOf(Color(0xFF009EB3).copy(alpha = 0.18f), Color(0xFFD97706).copy(alpha = 0.08f), Color.Transparent)
                                }
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                if (isDark) listOf(NeonCyan, CyberGold.copy(alpha = 0.5f))
                                else listOf(Color(0xFF009EB3), Color(0xFFD97706).copy(alpha = 0.6f))
                            ),
                            RoundedCornerShape(12.dp)
                        )
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dual-tone 3D Outline Icon Frame
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                if (isDark) listOf(NeonCyan.copy(alpha = 0.28f), CyberGold.copy(alpha = 0.2f))
                                else listOf(Color(0xFF009EB3).copy(alpha = 0.2f), Color(0xFFD97706).copy(alpha = 0.15f))
                            )
                        } else {
                            Brush.linearGradient(
                                if (isDark) listOf(Color(0x2200F0FF), Color(0x18FFB800))
                                else listOf(Color(0x15009EB3), Color(0x12D97706))
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isSelected) {
                            Brush.linearGradient(
                                if (isDark) listOf(NeonCyan, CyberGold)
                                else listOf(Color(0xFF009EB3), Color(0xFFD97706))
                            )
                        } else {
                            Brush.linearGradient(
                                if (isDark) listOf(NeonCyan.copy(alpha = 0.5f), CyberGold.copy(alpha = 0.4f))
                                else listOf(Color(0xFF009EB3).copy(alpha = 0.4f), Color(0xFFD97706).copy(alpha = 0.3f))
                            )
                        },
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) {
                        if (isDark) NeonCyanLight else Color(0xFF007A8A)
                    } else {
                        if (isDark) NeonCyan else Color(0xFF009EB3)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                ),
                color = if (isSelected) cyberSurfaces.textPrimary else cyberSurfaces.textSecondary,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                // Amber Active Notch Indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CyberGold else Color(0xFFD97706))
                        .shadow(4.dp, CircleShape, spotColor = CyberGold)
                )
            }
        }
    }
}

/**
 * Sleek Glowing Theme Mode Switcher (Dark vs High-Contrast Light)
 */
@Composable
private fun GlassmorphicThemeSwitcher(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    if (isDark) {
                        listOf(Color(0x3300F0FF), Color(0x2212192E), Color(0x33FFB800))
                    } else {
                        listOf(Color(0x20009EB3), Color(0x15CBD5E1), Color(0x20F59E0B))
                    }
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    if (isDark) {
                        listOf(NeonCyan.copy(alpha = 0.5f), CyberGold.copy(alpha = 0.5f))
                    } else {
                        listOf(Color(0xFF009EB3).copy(alpha = 0.6f), Color(0xFFD97706).copy(alpha = 0.6f))
                    }
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) NeonCyan.copy(alpha = 0.2f)
                            else Color(0xFFD97706).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (isDark) NeonCyan else Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isDark) "CYBER DARK THEME" else "HIGH-CONTRAST LIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = cyberSurfaces.textPrimary
                    )
                    Text(
                        text = if (isDark) "Neon OLED Mode" else "Crisp Daylight Mode",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = cyberSurfaces.textSecondary
                    )
                }
            }

            Switch(
                checked = !isDark,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF009EB3),
                    uncheckedThumbColor = NeonCyan,
                    uncheckedTrackColor = Color(0xFF070913)
                ),
                modifier = Modifier.testTag("drawer_theme_toggle")
            )
        }
    }
}

/**
 * Clean, Soft-Glowing Logout Button placed prominently at the bottom
 */
@Composable
private fun SoftGlowingLogoutButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "logout_press"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = pressOffset)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = DangerRed.copy(alpha = 0.35f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x38FF2A4B),
                        Color(0x1AFF2A4B)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        DangerRed.copy(alpha = 0.7f),
                        DangerRed.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 13.dp)
            .testTag("drawer_logout_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Log Out",
                tint = DangerRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LOG OUT ACCOUNT",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                ),
                color = DangerRed
            )
        }
    }
}
