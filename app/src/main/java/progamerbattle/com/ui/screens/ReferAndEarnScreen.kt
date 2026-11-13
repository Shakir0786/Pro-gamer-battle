package progamerbattle.com.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.data.UserRegistry
import progamerbattle.com.model.ReferralRecord
import progamerbattle.com.model.ReferralStatus
import progamerbattle.com.model.User
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
import progamerbattle.com.ui.theme.LocalCyberSurfaces
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReferAndEarnScreen(
    currentUser: User?
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    val userRegistry = UserRegistry.getInstance(context)
    val allReferrals by userRegistry.referrals.collectAsState()

    val myCleanMobile = currentUser?.mobileNumber?.trim()?.replace("+91", "")?.replace(" ", "")?.replace("-", "") ?: ""
    val myReferrals = allReferrals.filter { it.referrerMobile == myCleanMobile }

    val pendingCount = myReferrals.count { it.status == ReferralStatus.PENDING }
    val claimableCount = myReferrals.count { it.status == ReferralStatus.CLAIMABLE }
    val claimedCount = myReferrals.count { it.status == ReferralStatus.CLAIMED }

    val userCode = currentUser?.referralCode?.ifBlank { currentUser.myReferralCode } ?: "PGBGAMER"
    var isCopied by remember { mutableStateOf(false) }

    fun shareInvite() {
        val inviteMsg = """
            🔥 Hey Gamer! Join me on PRO GAMERBATTLE!
            ⚔️ Play Battle Gaming Tournaments & win real cash prizes!

            🎁 Use my Referral Code: $userCode
            💰 Get ₹10 Welcome Bonus directly in your bonus wallet on sign up!
            💳 Add minimum ₹10 cash to unlock both bonuses!

            👉 Download & Play: https://progamerbattle.com/download
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, inviteMsg)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Referral Code via")
        context.startActivity(shareIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header Card with 3D Cyber Theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = CyberGold)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CyberGold.copy(alpha = 0.22f),
                            CarbonCardElevated
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(CyberGoldLight, CyberGold.copy(alpha = 0.4f))
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Glowing Gift Icon Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CyberGold.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .border(2.dp, CyberGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Refer & Earn",
                        tint = CyberGoldLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "REFER & EARN ₹10 BONUS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = if (isDark) CyberGoldLight else Color(0xFFB45309),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Invite friends! When your friend adds min ₹10 deposit to their wallet, you get a CLAIM button to credit ₹10 into your Bonus Wallet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cyberSurfaces.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Referral Code Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = NeonCyan)
                .clip(RoundedCornerShape(20.dp))
                .background(cyberSurfaces.card)
                .border(1.2.dp, cyberSurfaces.border, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR EXCLUSIVE REFERRAL CODE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isDark) NeonCyan else Color(0xFF007A8A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The Code Display Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) CarbonBackground else Color(0xFFF1F5F9))
                        .border(
                            1.5.dp,
                            if (isCopied) NeonGreen else CyberGold.copy(alpha = 0.8f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userCode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.5.sp
                        ),
                        color = if (isCopied) NeonGreen else cyberSurfaces.textPrimary,
                        modifier = Modifier.testTag("referral_code_text")
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCopied) NeonGreen.copy(alpha = 0.2f) else CyberGold.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isCopied) NeonGreen else CyberGold,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                clipboardManager.setText(AnnotatedString(userCode))
                                isCopied = true
                                Toast.makeText(context, "Referral code copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("copy_referral_code_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (isCopied) NeonGreen else CyberGoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCopied) "COPIED" else "COPY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (isCopied) NeonGreen else CyberGoldLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Share Button
        Neon3DButton(
            text = "SHARE INVITE LINK VIA APPS",
            onClick = { shareInvite() },
            style = Neon3DStyle.CYAN,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("share_referral_button")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4-Way Live Referral Count Cards (Grid of 2x2)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReferralStatCard(
                    title = "TOTAL REFERRALS",
                    value = "${myReferrals.size}",
                    subtitle = "Friends Joined",
                    icon = Icons.Default.Groups,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                ReferralStatCard(
                    title = "PENDING",
                    value = "$pendingCount",
                    subtitle = "Needs min ₹10 add",
                    icon = Icons.Default.HourglassEmpty,
                    accentColor = CyberGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReferralStatCard(
                    title = "READY TO CLAIM",
                    value = "$claimableCount",
                    subtitle = if (claimableCount > 0) "Tap CLAIM below!" else "0 Ready",
                    icon = Icons.Default.CardGiftcard,
                    accentColor = if (claimableCount > 0) NeonGreen else TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                ReferralStatCard(
                    title = "SUCCESS (CLAIMED)",
                    value = "₹${claimedCount * 10}",
                    subtitle = "$claimedCount Friends",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = NeonGreenLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // MY REFERRALS HISTORY LIST & CLAIM ACTIONS
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cyberSurfaces.card)
                .border(1.dp, cyberSurfaces.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MY REFERRALS (HISTORY)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = cyberSurfaces.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonCardElevated)
                            .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${myReferrals.size} Total",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyanLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (myReferrals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonSurface)
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Referrals Yet",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share your code with friends! When they register and add min ₹10 cash, you can claim ₹10 bonus here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        myReferrals.forEach { referral ->
                            ReferralHistoryItem(
                                referral = referral,
                                onClaimClick = {
                                    val success = userRegistry.claimReferralReward(referral.id, myCleanMobile)
                                    if (success) {
                                        Toast.makeText(context, "₹10 Bonus credited to your Bonus Wallet!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Failed to claim reward. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // How it Works 3-Step Guide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cyberSurfaces.card)
                .border(1.dp, cyberSurfaces.border, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HOW REFER & EARN WORKS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = cyberSurfaces.textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                HowItWorksStep(
                    stepNumber = "1",
                    title = "Share Your Referral Code",
                    description = "Send your code to friends on WhatsApp, Telegram, or Instagram.",
                    icon = Icons.Default.Share,
                    accentColor = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksStep(
                    stepNumber = "2",
                    title = "Friend Registers & Adds Min ₹10",
                    description = "Friend gets ₹10 welcome bonus on sign-up. Status stays PENDING until they add min ₹10 deposit cash to their wallet.",
                    icon = Icons.Default.HourglassEmpty,
                    accentColor = CyberGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                HowItWorksStep(
                    stepNumber = "3",
                    title = "Claim ₹10 Bonus Cash",
                    description = "Once friend adds min ₹10, the CLAIM button unlocks! Tap Claim to credit ₹10 bonus into your Bonus Wallet.",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = NeonGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Withdrawal & Wallet Rule Reminder Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) CarbonCardElevated else Color(0xFFF8FAFC))
                .border(1.dp, if (isDark) NeonGreen.copy(alpha = 0.4f) else Color(0xFF10B981), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Wallet Rule",
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Fair Play & Bonus Wallet Rule",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Both referral earnings & welcome bonuses (₹10) are added strictly to your Bonus Wallet for match entries. Only match winnings can be withdrawn to UPI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cyberSurfaces.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReferralHistoryItem(
    referral: ReferralRecord,
    onClaimClick: () -> Unit
) {
    val cyberSurfaces = LocalCyberSurfaces.current
    val isDark = cyberSurfaces.isDark

    val dateFormatted = try {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(referral.registeredAt))
    } catch (_: Exception) {
        "Recent"
    }

    val maskedMobile = if (referral.refereeMobile.length == 10) {
        "${referral.refereeMobile.take(2)}******${referral.refereeMobile.takeLast(2)}"
    } else {
        referral.refereeMobile
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) CarbonSurface else Color(0xFFF8FAFC))
            .border(
                1.dp,
                when (referral.status) {
                    ReferralStatus.CLAIMABLE -> NeonGreen
                    ReferralStatus.CLAIMED -> CarbonBorder
                    ReferralStatus.PENDING -> CyberGold.copy(alpha = 0.5f)
                },
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${referral.refereeName} (${referral.refereeIgn})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = cyberSurfaces.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mobile: $maskedMobile",
                    style = MaterialTheme.typography.labelSmall,
                    color = cyberSurfaces.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Status Badge
            when (referral.status) {
                ReferralStatus.PENDING -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberGold.copy(alpha = 0.15f))
                                .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Pending",
                                    tint = CyberGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PENDING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = CyberGoldLight
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Friend must add min ₹10",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextMuted
                        )
                    }
                }

                ReferralStatus.CLAIMABLE -> {
                    // Glowing CLAIM Button
                    Box(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = NeonGreen)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonGreen, Color(0xFF00B359))
                                )
                            )
                            .clickable { onClaimClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("claim_referral_${referral.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Claim",
                                tint = CarbonBackground,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "CLAIM ₹10",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = CarbonBackground
                            )
                        }
                    }
                }

                ReferralStatus.CLAIMED -> {
                    // Success Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = NeonGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUCCESS (+₹10)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = NeonGreenLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val cyberSurfaces = LocalCyberSurfaces.current

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = accentColor)
            .clip(RoundedCornerShape(14.dp))
            .background(cyberSurfaces.card)
            .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = cyberSurfaces.textSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = accentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = cyberSurfaces.textSecondary
            )
        }
    }
}

@Composable
private fun HowItWorksStep(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color
) {
    val cyberSurfaces = LocalCyberSurfaces.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.18f))
                .border(1.2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = accentColor
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
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = cyberSurfaces.textSecondary
            )
        }
    }
}
