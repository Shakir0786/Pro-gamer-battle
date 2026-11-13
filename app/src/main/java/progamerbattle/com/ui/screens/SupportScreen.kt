package progamerbattle.com.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String
)

@Composable
fun SupportScreen(
    modifier: Modifier = Modifier,
    whatsappLink: String = "https://wa.me/message/X53ULJ34V2FZA1",
    telegramLink: String = "https://t.me/ProGamerBattle"
) {
    val context = LocalContext.current
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    val faqs = remember {
        listOf(
            FaqItem(
                id = "faq_room_credentials",
                question = "When do I get the Custom Room ID & Password?",
                answer = "Room credentials unlock on the match card exactly 7 minutes before the scheduled match start time. The 'Waiting for Entry Room' countdown will transition to 'Play & Accept Room Request' with your credentials visible."
            ),
            FaqItem(
                id = "faq_winnings_credit",
                question = "How are kill bounties and winnings credited?",
                answer = "Our tournament bots audit the official in-game match results within 10 to 15 minutes of match conclusion. Your per-kill bounties (₹20 per kill) and rank rewards are automatically deposited into your Winning Balance."
            ),
            FaqItem(
                id = "faq_withdrawal_process",
                question = "What is the minimum withdrawal limit and process?",
                answer = "The minimum withdrawal threshold is ₹100 from your Winning Balance. Withdrawals are processed directly to your linked UPI ID (Google Pay, PhonePe, Paytm, BHIM) within 1 to 2 business hours."
            ),
            FaqItem(
                id = "faq_device_disconnect",
                question = "What happens if the game crashes or disconnects?",
                answer = "The game features an automatic match reconnect prompt if you relaunch immediately. Matches start strictly on schedule, and entry fees cannot be refunded once custom rooms are locked."
            ),
            FaqItem(
                id = "faq_report_hacker",
                question = "How do I report an unfair player or suspected hacker?",
                answer = "Record a video of the death replay or take clear screenshots showing the player's Game UID and the Match ID. Contact our WhatsApp support desk within 30 minutes of match completion for an immediate review."
            ),
            FaqItem(
                id = "faq_update_profile",
                question = "Can I change my registered Player IGN or Game UID?",
                answer = "Yes! Open the navigation drawer or bottom dock, navigate to 'Profile', and tap 'Edit Profile'. Update your Player IGN and Game UID and save your changes before joining new custom rooms."
            )
        )
    }

    var expandedFaqId by remember { mutableStateOf<String?>(faqs.firstOrNull()?.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("support_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Hero Support Banner
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
                    .testTag("support_hero_banner")
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
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = "Support Icon",
                                tint = if (isDark) NeonCyan else Color(0xFF007A8A),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "24/7 ESPORTS SUPPORT DESK",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = cyberSurfaces.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instant Tournament & Wallet Assistance",
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isDark) CyberGoldLight else Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Match Desk: 10:00 AM - 11:30 PM IST (Daily)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = cyberSurfaces.textPrimary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Contact Channels Section Header
        item {
            SupportSectionHeader(
                title = "OFFICIAL CONTACT CHANNELS",
                icon = Icons.Default.Phone,
                accentColor = if (isDark) NeonCyan else Color(0xFF007A8A)
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Contact Cards
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_contact_channels"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // WhatsApp Support
                val cleanWhatsApp = remember(whatsappLink) {
                    val raw = whatsappLink.trim()
                    when {
                        raw.startsWith("http://") || raw.startsWith("https://") -> raw
                        raw.startsWith("wa.me/") -> "https://$raw"
                        else -> "https://wa.me/${raw.replace("+", "").replace(" ", "").replace("-", "")}"
                    }
                }
                ContactChannelCard(
                    title = "Official WhatsApp Support",
                    subtitle = "Direct Help Desk • Instant match queries",
                    actionLabel = "Chat on WhatsApp",
                    icon = Icons.Default.Phone,
                    badgeColor = NeonGreen,
                    testTag = "support_contact_whatsapp",
                    onAction = {
                        openExternalIntent(
                            context = context,
                            uri = cleanWhatsApp,
                            fallbackMsg = "WhatsApp: $whatsappLink"
                        )
                    }
                )

                // Email Support Desk
                ContactChannelCard(
                    title = "Support & Disputes Desk",
                    subtitle = "www.progamerbattle@gmail.com • Response < 2 hrs",
                    actionLabel = "Email Support Desk",
                    icon = Icons.Default.Email,
                    badgeColor = NeonCyan,
                    testTag = "support_contact_email",
                    onAction = {
                        openExternalIntent(
                            context = context,
                            uri = "mailto:www.progamerbattle@gmail.com?subject=ProGamerBattle%20Player%20Assistance",
                            fallbackMsg = "Email: www.progamerbattle@gmail.com"
                        )
                    }
                )

                // Telegram Official Channel
                val cleanTelegram = remember(telegramLink) {
                    val raw = telegramLink.trim()
                    when {
                        raw.startsWith("http://") || raw.startsWith("https://") -> raw
                        raw.startsWith("t.me/") -> "https://$raw"
                        else -> "https://t.me/${raw.removePrefix("@")}"
                    }
                }
                ContactChannelCard(
                    title = "Official Telegram Community",
                    subtitle = "Official Channel • Live updates & room alerts",
                    actionLabel = "Join Telegram",
                    icon = Icons.Default.Send,
                    badgeColor = CyberGold,
                    testTag = "support_contact_telegram",
                    onAction = {
                        openExternalIntent(
                            context = context,
                            uri = cleanTelegram,
                            fallbackMsg = "Telegram: $telegramLink"
                        )
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // FAQ Section Header
        item {
            SupportSectionHeader(
                title = "FREQUENTLY ASKED QUESTIONS",
                icon = Icons.AutoMirrored.Filled.Help,
                accentColor = if (isDark) CyberGold else Color(0xFFB45309)
            )
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Expandable FAQ Items
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_faq_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                faqs.forEachIndexed { index, faq ->
                    val isExpanded = expandedFaqId == faq.id

                    FaqExpandableCard(
                        faq = faq,
                        index = index + 1,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedFaqId = if (isExpanded) null else faq.id
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Dispute Instructions Card
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
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = if (isDark) NeonCyanLight else Color(0xFF007A8A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "When contacting support regarding room entries or prize disputes, please include your registered 10-digit mobile number, Player Game UID, and the Match Arena title for fastest resolution.",
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
private fun SupportSectionHeader(
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
private fun ContactChannelCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    icon: ImageVector,
    badgeColor: Color,
    testTag: String,
    onAction: () -> Unit
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
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = cyberSurfaces.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = cyberSurfaces.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Neon3DButton(
                text = actionLabel,
                onClick = onAction,
                style = if (badgeColor == NeonGreen) Neon3DStyle.GREEN else Neon3DStyle.CYAN,
                modifier = Modifier.width(115.dp)
            )
        }
    }
}

@Composable
private fun FaqExpandableCard(
    faq: FaqItem,
    index: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cyberSurfaces.card)
            .border(
                1.dp,
                if (isExpanded) {
                    if (isDark) NeonCyan.copy(alpha = 0.6f) else Color(0xFF009EB3)
                } else cyberSurfaces.border,
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .padding(14.dp)
            .testTag("faq_card_${faq.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Q$index.",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = if (isDark) NeonCyan else Color(0xFF007A8A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = faq.question,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = cyberSurfaces.textPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) {
                        if (isDark) NeonCyan else Color(0xFF007A8A)
                    } else cyberSurfaces.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        color = cyberSurfaces.border.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = cyberSurfaces.textSecondary
                    )
                }
            }
        }
    }
}

private fun openExternalIntent(context: Context, uri: String, fallbackMsg: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, fallbackMsg, Toast.LENGTH_SHORT).show()
    }
}
