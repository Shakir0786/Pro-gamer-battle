package progamerbattle.com.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.model.TransactionCategory
import progamerbattle.com.model.TransactionType
import progamerbattle.com.model.User
import progamerbattle.com.ui.components.Neon3DButton
import progamerbattle.com.ui.components.Neon3DCard
import progamerbattle.com.ui.components.Neon3DStyle
import progamerbattle.com.ui.components.PaymentSimulatorSheet
import progamerbattle.com.ui.components.WithdrawalSheet
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.ElectricPurple
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.NeonMagenta
import progamerbattle.com.ui.theme.SpecularHighlightBright
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    currentUser: User?,
    whatsappLink: String = "https://wa.me/message/X53ULJ34V2FZA1",
    modifier: Modifier = Modifier
) {
    val uiState by walletViewModel.uiState.collectAsState()
    val transactions by walletViewModel.transactions.collectAsState()
    val depositSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val withdrawSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val pendingDeposits = transactions.filter {
        it.category == TransactionCategory.ADD_CASH && it.status.equals("Pending", ignoreCase = true)
    }
    val openWhatsAppForScreenshot: () -> Unit = {
        val target = whatsappLink.ifBlank { "https://wa.me/message/X53ULJ34V2FZA1" }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp link: $target", Toast.LENGTH_LONG).show()
        }
    }

    val depositBalance = currentUser?.depositBalance ?: uiState.depositBalance
    val winningBalance = currentUser?.winningBalance ?: uiState.winningBalance
    val bonusBalance = currentUser?.bonusBalance ?: uiState.bonusBalance
    val totalBalance = depositBalance + winningBalance + bonusBalance

    val quickDepositOptions = listOf("50", "100", "250", "500")

    LaunchedEffect(uiState.withdrawSuccessMessage) {
        uiState.withdrawSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val cyberSurfaces = progamerbattle.com.ui.theme.LocalCyberSurfaces.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(cyberSurfaces.background)
            .testTag("wallet_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Pending Add Cash Verification Banner
        if (pendingDeposits.isNotEmpty()) {
            val pendingSum = pendingDeposits.sumOf { it.amount }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberGold.copy(alpha = 0.14f))
                        .border(1.2.dp, CyberGold, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("wallet_pending_verification_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Pending",
                            tint = CyberGoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "₹${pendingSum.toInt()} ADD CASH PENDING VERIFICATION",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = CyberGoldLight
                            )
                            Text(
                                text = "UPI progamerbattle@slc par payment ke baad WhatsApp par screenshot send karein. Admin approve karte hi cash add ho jayega.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = openWhatsAppForScreenshot,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("wallet_pending_whatsapp_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Send Screenshot on WhatsApp",
                            tint = Color(0xFF06210E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEND SCREENSHOT ON WHATSAPP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF06210E)
                        )
                    }
                }
            }
        }

        // Success Notification Banner if present
        if (uiState.withdrawSuccessMessage != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonGreen.copy(alpha = 0.16f))
                        .border(1.2.dp, NeonGreen, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.withdrawSuccessMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { walletViewModel.clearFeedback() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Main 3D Cyber Vault Balance Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = CyberGold.copy(alpha = 0.35f),
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF221606),
                                CarbonCardElevated,
                                CarbonCard
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                CyberGoldLight,
                                CyberGold.copy(alpha = 0.6f),
                                CarbonBorder
                            )
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberGold.copy(alpha = 0.2f))
                                    .border(1.dp, CyberGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = CyberGoldLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "TOTAL BALANCE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "BATTLE GAMING SECURE VAULT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = CyberGoldLight
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.18f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = NeonGreenLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "₹${"%.2f".format(totalBalance)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3-Way Detailed 3D Balance Pedestals
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CarbonSurface)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Deposit Balance (locked for matches)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Deposit",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${depositBalance.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = NeonCyanLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Match Only 🔒",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(CarbonBorder)
                        )

                        // Winning Balance (withdrawable)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Winnings",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreenLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${winningBalance.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Withdrawable ⚡",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(CarbonBorder)
                        )

                        // Bonus Balance (locked for matches)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Bonus",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${bonusBalance.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = CyberGoldLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Match Only 🔒",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Explicit Lock Policy Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B1425))
                            .border(1.dp, ElectricPurple.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Only winnings can be withdrawn. Deposit and bonus balances are for match entries only.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Vibrant 3D Primary Action Buttons: Add Cash (Cyan) & Withdraw Winnings (Green)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 3D Add Cash Button
                        Neon3DButton(
                            text = "ADD CASH",
                            onClick = { walletViewModel.openDepositSheet(null) },
                            style = Neon3DStyle.CYAN,
                            icon = Icons.Default.Add,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_cash_button")
                        )

                        // 3D Withdraw Button
                        Neon3DButton(
                            text = "WITHDRAW",
                            onClick = { walletViewModel.openWithdrawSheet() },
                            style = Neon3DStyle.GREEN,
                            icon = Icons.Default.Payments,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("withdraw_winnings_button")
                        )
                    }
                }
            }
        }

        // 3D Quick Top Up Chips
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "QUICK TOP-UP (DEPOSIT)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = NeonCyanLight
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickDepositOptions.forEach { amount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color.Black)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(CarbonCardElevated, CarbonSurface)
                                )
                            )
                            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                            .clickable { walletViewModel.openDepositSheet(amount.toDouble()) }
                            .padding(vertical = 12.dp)
                            .testTag("quick_topup_$amount"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+₹$amount",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        // Transactions Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElectricPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRANSACTION HISTORY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${transactions.size} records",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Transaction Items or Empty State
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CarbonCardElevated, CarbonCard)
                            )
                        )
                        .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CarbonSurface)
                                .border(1.dp, CarbonBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No transactions yet. Join matches to start playing!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                val isCredit = tx.type == TransactionType.CREDIT
                val isPending = tx.status.equals("Pending", ignoreCase = true)
                val isRejected = tx.status.equals("Rejected", ignoreCase = true)
                val isSuccess = tx.status.equals("Success", ignoreCase = true) ||
                    tx.status.equals("Approved", ignoreCase = true)
                val accentColor = when {
                    isPending -> CyberGold
                    isRejected -> DangerRed
                    isCredit -> NeonGreen
                    else -> DangerRed
                }
                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(tx.timestamp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CarbonCardElevated, CarbonSurface)
                            )
                        )
                        .border(
                            1.dp,
                            accentColor.copy(alpha = 0.45f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                        .testTag("wallet_tx_card_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isPending -> Icons.Default.HourglassTop
                                        isRejected -> Icons.Default.Cancel
                                        isCredit -> Icons.AutoMirrored.Filled.CallReceived
                                        else -> Icons.AutoMirrored.Filled.CallMade
                                    },
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = tx.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${tx.subtitle} • $formattedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            // High-Visibility Amount Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val displayAmount = if (tx.amount % 1.0 == 0.0) tx.amount.toInt().toString() else tx.amount.toString()
                                Text(
                                    text = "${if (isCredit) "+" else "-"}₹$displayAmount",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = when {
                                        isPending -> CyberGoldLight
                                        isRejected -> DangerRed
                                        isCredit -> NeonGreenLight
                                        else -> DangerRed
                                    },
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status Pill (PENDING / SUCCESS / REJECTED)
                            val statusLabel = when {
                                isPending -> "PENDING"
                                isRejected -> "REJECTED"
                                isSuccess -> "SUCCESS"
                                else -> tx.status.uppercase()
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("tx_status_badge_${tx.id}")
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = accentColor
                                )
                            }
                        }
                    }

                    // Contextual Status Details: Pending WhatsApp Screenshot / Rejection Reason / Success Note
                    if (isPending && tx.category == TransactionCategory.ADD_CASH) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberGold.copy(alpha = 0.12f))
                                .border(1.dp, CyberGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Send payment screenshot on WhatsApp for verification",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = CyberGoldLight,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF25D366))
                                    .clickable { openWhatsAppForScreenshot() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("tx_whatsapp_screenshot_${tx.id}")
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF06210E)
                                )
                            }
                        }
                    } else if (isRejected) {
                        val reasonText = tx.rejectReason.ifBlank {
                            tx.description.removePrefix("Rejected:").trim().ifBlank { "Payment not verified by Admin" }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DangerRed.copy(alpha = 0.12f))
                                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("tx_reject_reason_${tx.id}")
                        ) {
                            Text(
                                text = "Rejection Reason: $reasonText",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DangerRed
                            )
                        }
                    } else if (isSuccess && tx.category == TransactionCategory.ADD_CASH && tx.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ ${tx.description}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = NeonGreenLight
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Official UPI Deposit (progamerbattle@slc)
    if (uiState.isDepositSheetOpen) {
        PaymentSimulatorSheet(
            sheetState = depositSheetState,
            amountInput = uiState.depositAmountInput,
            utrInput = uiState.utrInput,
            merchantUpiId = uiState.merchantUpiId,
            lastTransactionRef = uiState.lastTransactionRef,
            upiAppLaunched = uiState.upiAppLaunched,
            upiPaymentReturned = uiState.upiPaymentReturned,
            upiPaymentCancelled = uiState.upiPaymentCancelled,
            paymentError = uiState.paymentError,
            hasActivePendingDeposit = uiState.hasActivePendingDeposit,
            whatsappLink = whatsappLink,
            selectedMethod = uiState.selectedMethod,
            processingStatus = uiState.paymentStatus,
            onAmountChange = { walletViewModel.setDepositAmount(it) },
            onUtrChange = { walletViewModel.setUtrInput(it) },
            onMethodSelect = { walletViewModel.selectPaymentMethod(it) },
            onProceedToPaymentPage = { walletViewModel.openUpiPaymentPage() },
            onBackToSetup = { walletViewModel.backToDepositSetup() },
            onUpiAppLaunched = { walletViewModel.markUpiAppLaunched() },
            onUpiAppResult = { resp, ok -> walletViewModel.onUpiAppResult(resp, ok) },
            onNoUpiAppFallback = { walletViewModel.onNoUpiAppFallback() },
            onProceedPay = { walletViewModel.submitUpiDepositForVerification() },
            onDismiss = { walletViewModel.closeDepositSheet() }
        )
    }

    // Modal Sheet for Winning Withdrawal
    if (uiState.isWithdrawSheetOpen) {
        WithdrawalSheet(
            sheetState = withdrawSheetState,
            winningBalance = winningBalance,
            amountInput = uiState.withdrawAmountInput,
            upiIdInput = uiState.upiIdInput,
            errorMessage = uiState.withdrawError,
            isWithdrawing = uiState.isWithdrawing,
            onAmountChange = { walletViewModel.setWithdrawAmount(it) },
            onUpiIdChange = { walletViewModel.setUpiId(it) },
            onProceedWithdraw = { walletViewModel.executeWithdrawal() },
            onDismiss = { walletViewModel.closeWithdrawSheet() }
        )
    }
}
