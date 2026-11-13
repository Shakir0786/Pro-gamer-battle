package progamerbattle.com.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonMagenta

@Composable
fun RulesScreen(
    modifier: Modifier = Modifier
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("rules_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Hero Fair Play Protocol Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = if (isDark) NeonCyan.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isDark) listOf(Color(0xFF0F1A2E), Color(0xFF070B14))
                            else listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.horizontalGradient(
                            if (isDark) listOf(NeonCyan, CyberGold)
                            else listOf(Color(0xFF009EB3), Color(0xFFD97706))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
                    .testTag("rules_hero_banner")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        if (isDark) listOf(NeonCyan.copy(alpha = 0.3f), Color(0xFF003844))
                                        else listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                                    )
                                )
                                .border(1.5.dp, if (isDark) NeonCyan else Color(0xFF009EB3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Fair Play Badge",
                                tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "FAIR PLAY ESPORTS PROTOCOL",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = cyberSurfaces.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Zero Tolerance for Hacks • 100% Skill Verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isDark) NeonCyanLight else Color(0xFF0284C7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        color = cyberSurfaces.border.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Every match on Pro GamerBattle is governed by official battle gaming competitive esports standards. All players are strictly audited for fair gameplay.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = cyberSurfaces.textSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Section 1 Header: Tournament Guidelines
        item {
            RulesSectionHeader(
                title = "TOURNAMENT GUIDELINES",
                icon = Icons.Default.SportsEsports,
                accentColor = if (isDark) NeonCyan else Color(0xFF007A8A)
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rules_section_guidelines"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RuleItemCard(
                    number = "01",
                    title = "Room Credentials & 7-Minute Window",
                    description = "Custom Room ID and Password unlock exactly 7 minutes before match start time. Ensure you have the game app updated and enter the room within this window.",
                    icon = Icons.Default.Timer,
                    badgeColor = NeonCyan
                )

                RuleItemCard(
                    number = "02",
                    title = "Strict Slot & Lobby Discipline",
                    description = "Solo players must occupy the designated slot. Slot hopping, spectating without clearance, or inviting unauthorized players will result in instant room ejection.",
                    icon = Icons.Default.MilitaryTech,
                    badgeColor = CyberGold
                )

                RuleItemCard(
                    number = "03",
                    title = "Kill Bounties & Winnings Calculation",
                    description = "Kill bounties (₹20 per kill) and final placement rankings are calculated strictly from the official end-game scoreboard. Winnings are automatically credited to your wallet.",
                    icon = Icons.Default.CheckCircle,
                    badgeColor = NeonGreen
                )

                RuleItemCard(
                    number = "04",
                    title = "Device & Network Responsibility",
                    description = "Matches launch strictly at the scheduled time. Disconnections caused by player device or internet failures cannot be refunded once the custom room begins.",
                    icon = Icons.Default.Warning,
                    badgeColor = CyberGold
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Section 2 Header: Anti-Hack & Anti-Cheat Policies
        item {
            RulesSectionHeader(
                title = "ANTI-HACK & PENALTY POLICIES",
                icon = Icons.Default.Gavel,
                accentColor = DangerRed
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rules_section_antihack"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RuleItemCard(
                    number = "01",
                    title = "Prohibited Software & Scripting",
                    description = "Use of aimbots, auto-headshot configs, wallhacks, speed modifiers, altered APKs, or third-party memory injection software is strictly detected by our server telemetry and results in permanent hardware and UID ban.",
                    icon = Icons.Default.Block,
                    badgeColor = DangerRed
                )

                RuleItemCard(
                    number = "02",
                    title = "Teaming & Collusion in Solo Mode",
                    description = "Teaming up with another player in Solo mode is strictly illegal. Any mutual ceasefire or coordinated combat against other players leads to instant disqualification and forfeiture of all balances.",
                    icon = Icons.Default.Block,
                    badgeColor = DangerRed
                )

                RuleItemCard(
                    number = "03",
                    title = "Emulators & PC Peripherals Policy",
                    description = "Custom rooms operate strictly in Mobile-Only mode. Playing via PC emulators (BlueStacks, LDPlayer) without designated emulator room tags is forbidden.",
                    icon = Icons.Default.Warning,
                    badgeColor = NeonMagenta
                )

                RuleItemCard(
                    number = "04",
                    title = "Dispute & Video Proof Requirement",
                    description = "In case of any rule violations or suspicious opponent gameplay, players must record their match or capture the death replay to file an official report within 30 minutes of match completion.",
                    icon = Icons.Default.Security,
                    badgeColor = NeonCyan
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Final Compliance Footer Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) Color(0xFF131A26)
                        else Color(0xFFE2E8F0)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF26354A) else Color(0xFFCBD5E1),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = if (isDark) CyberGoldLight else Color(0xFFB45309),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Enrolling in any custom tournament constitutes legal acceptance of these Fair Play and Anti-Hack terms. Management decisions on verified cheats are final and binding.",
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                        color = cyberSurfaces.textSecondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun RulesSectionHeader(
    title: String,
    icon: ImageVector,
    accentColor: Color
) {
    val cyberSurfaces = LocalCyberSurfaces.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = cyberSurfaces.textPrimary
        )
    }
}

@Composable
private fun RuleItemCard(
    number: String,
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = if (isDark) badgeColor.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(14.dp))
            .background(cyberSurfaces.card)
            .border(1.dp, cyberSurfaces.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Number & Icon Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.14f))
                    .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cyberSurfaces.textPrimary
                    )
                    Text(
                        text = "#$number",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = badgeColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = cyberSurfaces.textSecondary
                )
            }
        }
    }
}
