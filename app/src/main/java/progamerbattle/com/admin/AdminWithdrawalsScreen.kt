package progamerbattle.com.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.data.AdminDepositRequest
import progamerbattle.com.data.AdminWithdrawalRequest
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
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminWithdrawalsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val deposits by adminViewModel.deposits.collectAsState()
    val withdrawals by adminViewModel.withdrawals.collectAsState()

    var selectedQueueTab by remember { mutableIntStateOf(0) } // 0 = Add Cash Verify, 1 = Withdrawals

    // Deposit approval / rejection dialog state
    var approvingDeposit by remember { mutableStateOf<AdminDepositRequest?>(null) }
    var approveNoteInput by remember { mutableStateOf("Screenshot verified on WhatsApp") }

    var rejectingDeposit by remember { mutableStateOf<AdminDepositRequest?>(null) }
    var rejectDepositReasonInput by remember { mutableStateOf("Screenshot not received on WhatsApp") }

    // Withdrawal approval / rejection dialog state
    var approvingItem by remember { mutableStateOf<AdminWithdrawalRequest?>(null) }
    var utrInput by remember { mutableStateOf("") }

    var rejectingItem by remember { mutableStateOf<AdminWithdrawalRequest?>(null) }
    var rejectReasonInput by remember { mutableStateOf("Invalid UPI ID") }

    val pendingDepositsCount = deposits.count { it.status.equals("Pending", ignoreCase = true) }
    val pendingDepositAmount = deposits.filter { it.status.equals("Pending", ignoreCase = true) }.sumOf { it.amount }

    val pendingWithdrawalsCount = withdrawals.count { it.status.equals("Pending", ignoreCase = true) }
    val totalPendingWithdrawalAmount = withdrawals.filter { it.status.equals("Pending", ignoreCase = true) }.sumOf { it.amount }

    val dateSdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonBackground)
            .testTag("admin_withdrawals_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // 3D Segmented Queue Switcher: Add Cash Verification vs Withdrawals
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
                val isAddCashSelected = selectedQueueTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isAddCashSelected) {
                                Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.10f)))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .border(
                            1.dp,
                            if (isAddCashSelected) NeonCyan else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedQueueTab = 0 }
                        .padding(vertical = 12.dp)
                        .testTag("admin_tab_add_cash"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Cash ($pendingDepositsCount)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isAddCashSelected) NeonCyanLight else TextSecondary
                    )
                }

                val isWithdrawalsSelected = selectedQueueTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isWithdrawalsSelected) {
                                Brush.horizontalGradient(listOf(CyberGold.copy(alpha = 0.25f), CyberGold.copy(alpha = 0.10f)))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .border(
                            1.dp,
                            if (isWithdrawalsSelected) CyberGold else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedQueueTab = 1 }
                        .padding(vertical = 12.dp)
                        .testTag("admin_tab_withdrawals"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Withdrawals ($pendingWithdrawalsCount)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isWithdrawalsSelected) CyberGoldLight else TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (selectedQueueTab == 0) {
            // --- ADD CASH VERIFICATION QUEUE ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonCard)
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ADD CASH UPI VERIFICATION",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Text(
                                text = "$pendingDepositsCount Pending • Total: ₹${pendingDepositAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyberGoldLight
                            )
                            Text(
                                text = "Official UPI: progamerbattle@slc",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyanLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = NeonCyanLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            if (deposits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NeonCyan.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = NeonCyanLight,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "No Add Cash Requests Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Jab user UPI (progamerbattle@slc) par payment karke WhatsApp par screenshot bhejega, uska Pending request yahan approve ya reject karne ke liye aayega.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(deposits, key = { it.id }) { dep ->
                    val isPending = dep.status.equals("Pending", ignoreCase = true)
                    val isSuccess = dep.status.equals("Success", ignoreCase = true) ||
                        dep.status.equals("Approved", ignoreCase = true)
                    val isRejected = dep.status.equals("Rejected", ignoreCase = true)
                    val statusColor = when {
                        isSuccess -> NeonGreen
                        isRejected -> DangerRed
                        else -> CyberGold
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CarbonCard)
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                            .testTag("admin_deposit_item_${dep.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "₹${dep.amount.toInt()}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = statusColor
                                    )
                                    Text(
                                        text = "Player: ${dep.playerName} (${dep.mobile.ifBlank { "N/A" }})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${dep.paymentMethod} • ${dateSdf.format(Date(dep.timestamp))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = dep.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CarbonSurface)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Paid to UPI: ${dep.merchantUpiId}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = NeonCyanLight
                                        )
                                    }
                                    if (isPending) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CyberGold.copy(alpha = 0.16f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = CyberGoldLight,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "ANTI-FRAUD CHECK",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                ),
                                                color = CyberGoldLight
                                            )
                                        }
                                    }
                                }
                                if (dep.utrNumber.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonCyan.copy(alpha = 0.08f))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "UTR / Ref No (Match with WhatsApp SS):",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = TextMuted
                                            )
                                            Text(
                                                text = dep.utrNumber,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.6.sp
                                                ),
                                                color = TextPrimary
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonCyan.copy(alpha = 0.18f))
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(dep.utrNumber))
                                                    Toast.makeText(
                                                        context,
                                                        "UTR copied: ${dep.utrNumber}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                                .testTag("admin_copy_utr_${dep.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy UTR",
                                                tint = NeonCyanLight,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "COPY UTR",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                ),
                                                color = NeonCyanLight
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Req ID: ${dep.id}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }

                            if (isSuccess && dep.adminNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✓ ${dep.adminNote} (Wallet Credited)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeonGreenLight
                                )
                            }

                            if (isRejected && dep.rejectReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Reject Reason: ${dep.rejectReason}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                            }

                            if (isPending) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            approvingDeposit = dep
                                            approveNoteInput = "Screenshot verified on WhatsApp"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("admin_approve_deposit_btn_${dep.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve & Add Cash", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            rejectingDeposit = dep
                                            rejectDepositReasonInput = "Screenshot not received on WhatsApp"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("admin_reject_deposit_btn_${dep.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // --- UPI WITHDRAWALS QUEUE ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "UPI WITHDRAWAL QUEUE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                            Text(
                                text = "$pendingWithdrawalsCount Pending Requests • Total: ₹${totalPendingWithdrawalAmount.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberGoldLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberGold.copy(alpha = 0.15f))
                                .border(1.dp, CyberGold, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (withdrawals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyberGold.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = CyberGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "No Withdrawal Requests",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "When players withdraw their tournament winnings, their UPI requests will appear here for 1-click approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(withdrawals, key = { it.id }) { req ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CarbonCard)
                            .border(
                                1.dp,
                                when (req.status) {
                                    "Approved" -> NeonGreen.copy(alpha = 0.5f)
                                    "Rejected" -> DangerRed.copy(alpha = 0.5f)
                                    else -> CarbonBorder
                                },
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp)
                            .testTag("admin_withdrawal_item_${req.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "₹${req.amount.toInt()}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = if (req.status == "Approved") NeonGreen else CyberGold
                                    )
                                    Text(
                                        text = "User: ${req.playerName} (${req.mobile})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (req.status) {
                                                "Approved" -> NeonGreen.copy(alpha = 0.15f)
                                                "Rejected" -> DangerRed.copy(alpha = 0.15f)
                                                else -> CyberGold.copy(alpha = 0.15f)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when (req.status) {
                                                "Approved" -> NeonGreen
                                                "Rejected" -> DangerRed
                                                else -> CyberGold
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = req.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        ),
                                        color = when (req.status) {
                                            "Approved" -> NeonGreen
                                            "Rejected" -> DangerRed
                                            else -> CyberGold
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CarbonSurface)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "PLAYER UPI ID",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = TextMuted
                                        )
                                        Text(
                                            text = req.upiId,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                            color = NeonCyanLight
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonCyan.copy(alpha = 0.18f))
                                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(req.upiId))
                                            Toast.makeText(
                                                context,
                                                "UPI ID copied: ${req.upiId}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                        .testTag("admin_copy_withdrawal_upi_${req.id}")
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = NeonCyanLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COPY UPI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = NeonCyanLight
                                    )
                                }
                            }

                            if (req.utrNumber.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bank Ref / UTR: ${req.utrNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen
                                )
                            }

                            if (req.rejectReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Reject Reason: ${req.rejectReason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DangerRed
                                )
                            }

                            if (req.status == "Pending") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            approvingItem = req
                                            utrInput = "UTR-${(10000000..99999999).random()}"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve & Pay", color = NeonGreen, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            rejectingItem = req
                                            rejectReasonInput = "Invalid UPI VPA address"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", color = DangerRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // --- MODAL 1: APPROVE ADD CASH DEPOSIT ---
    approvingDeposit?.let { dep ->
        AlertDialog(
            onDismissRequest = { approvingDeposit = null },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Approve Add Cash: ₹${dep.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = NeonGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "WhatsApp screenshot verify karne ke baad confirm karein. ₹${dep.amount.toInt()} directly ${dep.playerName} (${dep.mobile}) ke Deposit Wallet me add ho jayega aur status 'Success' ho jayega.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = approveNoteInput,
                        onValueChange = { approveNoteInput = it },
                        label = { Text("Verification Note (Visible to User)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonSurface,
                            unfocusedContainerColor = CarbonSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_approve_deposit_note_input")
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "APPROVE & ADD ₹${dep.amount.toInt()}",
                    onClick = {
                        adminViewModel.approveDeposit(dep.id, approveNoteInput)
                        approvingDeposit = null
                    },
                    style = Neon3DStyle.GREEN,
                    modifier = Modifier.testTag("admin_confirm_approve_deposit_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { approvingDeposit = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- MODAL 2: REJECT ADD CASH DEPOSIT (WITH REASON) ---
    rejectingDeposit?.let { dep ->
        val quickReasons = listOf(
            "Screenshot not received on WhatsApp",
            "Payment not received in progamerbattle@slc",
            "Amount mismatch / Wrong UTR",
            "Fake or duplicate screenshot"
        )
        AlertDialog(
            onDismissRequest = { rejectingDeposit = null },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Reject Add Cash (₹${dep.amount.toInt()})?",
                    color = DangerRed,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "User ke Pending status par ye Rejection Reason dikhega aur wallet me paise add nahi honge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReasons.forEach { reason ->
                            val selected = rejectDepositReasonInput == reason
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) DangerRed.copy(alpha = 0.25f) else CarbonSurface)
                                    .border(1.dp, if (selected) DangerRed else CarbonBorder, RoundedCornerShape(8.dp))
                                    .clickable { rejectDepositReasonInput = reason }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (selected) DangerRed else TextSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = rejectDepositReasonInput,
                        onValueChange = { rejectDepositReasonInput = it },
                        label = { Text("Rejection Reason") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DangerRed,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonSurface,
                            unfocusedContainerColor = CarbonSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_reject_deposit_reason_input")
                    )
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        adminViewModel.rejectDeposit(dep.id, rejectDepositReasonInput)
                        rejectingDeposit = null
                    },
                    modifier = Modifier.testTag("admin_confirm_reject_deposit_button")
                ) {
                    Text("Confirm Reject", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingDeposit = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- MODAL 3: APPROVE WITHDRAWAL DIALOG ---
    approvingItem?.let { req ->
        AlertDialog(
            onDismissRequest = { approvingItem = null },
            containerColor = CarbonCardElevated,
            title = {
                Text(
                    text = "Confirm UPI Payout: ₹${req.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = NeonGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transferring ₹${req.amount.toInt()} to UPI ID: ${req.upiId} (Player: ${req.playerName}). Enter bank transfer reference / UTR below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = req.upiId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = NeonCyanLight,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.18f))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(req.upiId))
                                    Toast.makeText(
                                        context,
                                        "UPI ID copied: ${req.upiId}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("admin_dialog_copy_upi_${req.id}")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy UPI ID",
                                tint = NeonCyanLight,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "COPY UPI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonCyanLight
                            )
                        }
                    }

                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it },
                        label = { Text("Bank UTR / Transaction ID") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonSurface,
                            unfocusedContainerColor = CarbonSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("admin_utr_input")
                    )
                }
            },
            confirmButton = {
                Neon3DButton(
                    text = "CONFIRM & MARK PAID",
                    onClick = {
                        adminViewModel.approveWithdrawal(req.id, utrInput)
                        approvingItem = null
                    },
                    style = Neon3DStyle.GREEN,
                    modifier = Modifier.testTag("admin_confirm_payout_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { approvingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // --- MODAL 4: REJECT WITHDRAWAL DIALOG ---
    rejectingItem?.let { req ->
        AlertDialog(
            onDismissRequest = { rejectingItem = null },
            containerColor = CarbonCardElevated,
            title = {
                Text("Reject Withdrawal Request?", color = DangerRed)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Rejecting this withdrawal will restore ₹${req.amount.toInt()} to ${req.playerName}'s winning balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Reason for Rejection") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DangerRed,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonSurface,
                            unfocusedContainerColor = CarbonSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        adminViewModel.rejectWithdrawal(req.id, rejectReasonInput)
                        rejectingItem = null
                    }
                ) {
                    Text("Confirm Rejection", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
