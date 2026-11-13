package progamerbattle.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.DeepAmber
import progamerbattle.com.ui.theme.FlameOrange
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.MIN_WITHDRAWAL_AMOUNT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalSheet(
    sheetState: SheetState,
    winningBalance: Double,
    amountInput: String,
    upiIdInput: String,
    errorMessage: String?,
    isWithdrawing: Boolean,
    onAmountChange: (String) -> Unit,
    onUpiIdChange: (String) -> Unit,
    onProceedWithdraw: () -> Unit,
    onDismiss: () -> Unit
) {
    val amountNum = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountValid = amountNum >= MIN_WITHDRAWAL_AMOUNT && amountNum <= winningBalance
    val isUpiValid = upiIdInput.isNotBlank() && upiIdInput.contains("@") && upiIdInput.length >= 5
    val canSubmit = isAmountValid && isUpiValid && !isWithdrawing

    val quickPresetAmounts = listOf("100", "200", "500", "1000")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CarbonCardElevated,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("withdrawal_bottom_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WITHDRAW WINNINGS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Text(
                            text = "Instant UPI Bank Transfer",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepAmber.copy(alpha = 0.15f))
                        .border(1.dp, DeepAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MIN ₹100",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = DeepAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Winning Balance banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Available Winning Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${winningBalance.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = NeonGreen
                        )
                    }

                    if (winningBalance >= MIN_WITHDRAWAL_AMOUNT) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .clickable { onAmountChange(winningBalance.toInt().toString()) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "WITHDRAW ALL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mandatory Policy Note: Lock note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FlameOrange.copy(alpha = 0.10f))
                    .border(1.dp, FlameOrange.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = FlameOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Only winnings can be withdrawn. Deposit and bonus balances are for match entries only.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    color = FlameOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount OutlinedTextField
            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_amount_input"),
                label = { Text("Withdrawal Amount (Min ₹100)") },
                prefix = {
                    Text(
                        "₹ ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (amountNum >= MIN_WITHDRAWAL_AMOUNT && amountNum <= winningBalance) NeonGreen else FlameOrange
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = amountInput.isNotEmpty() && (amountNum < MIN_WITHDRAWAL_AMOUNT || amountNum > winningBalance),
                supportingText = {
                    when {
                        amountInput.isEmpty() -> {
                            Text(
                                text = "Minimum withdrawal amount is ₹100.",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        amountNum < MIN_WITHDRAWAL_AMOUNT -> {
                            Text(
                                text = "Minimum withdrawal amount is ₹100.",
                                color = DangerRed,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        amountNum > winningBalance -> {
                            Text(
                                text = "Insufficient winning balance. Only winnings can be withdrawn! Available winning: ₹${winningBalance.toInt()}",
                                color = DangerRed,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        else -> {
                            Text(
                                text = "Eligible for instant withdrawal",
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CarbonBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CarbonBackground,
                    unfocusedContainerColor = CarbonBackground,
                    errorBorderColor = DangerRed
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Amount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPresetAmounts.forEach { preset ->
                    val isSelected = amountInput == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else CarbonCard)
                            .border(1.dp, if (isSelected) NeonGreen else CarbonBorder, RoundedCornerShape(8.dp))
                            .clickable { onAmountChange(preset) }
                            .padding(vertical = 8.dp)
                            .testTag("withdraw_preset_$preset"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹$preset",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) NeonGreen else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // UPI ID input
            OutlinedTextField(
                value = upiIdInput,
                onValueChange = onUpiIdChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdraw_upi_input"),
                label = { Text("UPI ID / VPA") },
                placeholder = { Text("e.g. mobile@upi, yourname@oksbi", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = DeepAmber,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepAmber,
                    unfocusedBorderColor = CarbonBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CarbonBackground,
                    unfocusedContainerColor = CarbonBackground
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRed,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Withdraw Button
            Button(
                onClick = onProceedWithdraw,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_withdraw_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    disabledContainerColor = CarbonBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isWithdrawing) {
                    CircularProgressIndicator(
                        color = CarbonBackground,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PROCESSING TRANSFER...",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = CarbonBackground
                    )
                } else {
                    Text(
                        text = if (amountNum >= MIN_WITHDRAWAL_AMOUNT) "WITHDRAW ₹${amountNum.toInt()}" else "ENTER AMOUNT (MIN ₹100)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = if (canSubmit) CarbonBackground else TextMuted
                    )
                }
            }
        }
    }
}
