package progamerbattle.com.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.CarbonBorder
import progamerbattle.com.ui.theme.CarbonCard
import progamerbattle.com.ui.theme.CarbonCardElevated
import progamerbattle.com.ui.theme.CarbonSurface
import progamerbattle.com.ui.theme.CyberGold
import progamerbattle.com.ui.theme.CyberGoldLight
import progamerbattle.com.ui.theme.DangerRed
import progamerbattle.com.ui.theme.DeepAmber
import progamerbattle.com.ui.theme.NeonCyan
import progamerbattle.com.ui.theme.NeonCyanLight
import progamerbattle.com.ui.theme.NeonGreen
import progamerbattle.com.ui.theme.NeonGreenLight
import progamerbattle.com.ui.theme.TextMuted
import progamerbattle.com.ui.theme.TextPrimary
import progamerbattle.com.ui.theme.TextSecondary
import progamerbattle.com.viewmodel.PaymentMethod
import progamerbattle.com.viewmodel.PaymentProcessingStatus
import java.util.Locale

private val WhatsAppGreen = Color(0xFF25D366)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSimulatorSheet(
    sheetState: SheetState,
    amountInput: String,
    utrInput: String = "",
    merchantUpiId: String = WalletRepository.MERCHANT_UPI_ID,
    lastTransactionRef: String? = null,
    upiAppLaunched: Boolean = false,
    upiPaymentReturned: Boolean = false,
    upiPaymentCancelled: Boolean = false,
    paymentError: String? = null,
    hasActivePendingDeposit: Boolean = false,
    whatsappLink: String = "https://wa.me/message/X53ULJ34V2FZA1",
    selectedMethod: PaymentMethod,
    processingStatus: PaymentProcessingStatus,
    onAmountChange: (String) -> Unit,
    onUtrChange: (String) -> Unit = {},
    onMethodSelect: (PaymentMethod) -> Unit,
    onProceedToPaymentPage: () -> Unit = {},
    onBackToSetup: () -> Unit = {},
    onUpiAppLaunched: () -> Unit = {},
    onUpiAppResult: (response: String?, isResultOk: Boolean) -> Unit = { _, _ -> },
    onNoUpiAppFallback: () -> Unit = {},
    onProceedPay: () -> Unit,
    onDismiss: () -> Unit
) {
    val quickAmounts = listOf("50", "100", "200", "500")
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val validAmount = (amountInput.toIntOrNull() ?: 0) >= 10

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = result.data?.getStringExtra("response")
            ?: result.data?.getStringExtra("Status")
        onUpiAppResult(response, result.resultCode == Activity.RESULT_OK)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CarbonCardElevated,
        scrimColor = Color.Black.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("payment_simulator_sheet")
        ) {
            when (processingStatus) {
                PaymentProcessingStatus.PROCESSING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Recording Payment Request...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Linking UPI $merchantUpiId for verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // STEP 2: DEDICATED UPI PAYMENT PAGE (Nothing added to history unless completed!)
                PaymentProcessingStatus.PAYMENT_PAGE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_payment_page_view")
                    ) {
                        // Top Back Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onBackToSetup() }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .testTag("payment_page_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = NeonCyanLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BACK / CANCEL",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyanLight
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberGold.copy(alpha = 0.16f))
                                    .border(1.dp, CyberGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UPI PAYMENT PAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = CyberGoldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pay Summary Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0E222D), CarbonSurface)
                                    )
                                )
                                .border(1.4.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AMOUNT TO PAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹$amountInput",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                color = CyberGoldLight
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Official Merchant UPI Row with Copy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CarbonBackground)
                                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "UPI ID",
                                        tint = NeonCyanLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Pro Gamer Battle Official UPI",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextMuted
                                        )
                                        Text(
                                            text = merchantUpiId,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                            color = TextPrimary,
                                            modifier = Modifier.testTag("merchant_upi_id_text")
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(merchantUpiId))
                                        Toast.makeText(context, "UPI Copied: $merchantUpiId", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonCyan.copy(alpha = 0.16f))
                                        .testTag("copy_merchant_upi_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = NeonCyanLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Primary Button: Open UPI App via StartActivityForResult so cancel/back is detected!
                            Button(
                                onClick = {
                                    val amtNum = amountInput.toDoubleOrNull() ?: 0.0
                                    val formattedAmt = String.format(Locale.US, "%.2f", amtNum)
                                    val upiUri = Uri.parse(
                                        "upi://pay?pa=${Uri.encode(merchantUpiId)}" +
                                            "&pn=${Uri.encode("Pro Gamer Battle")}" +
                                            "&am=${Uri.encode(formattedAmt)}" +
                                            "&cu=INR" +
                                            "&tn=${Uri.encode("ProGamerBattle Add Cash")}"
                                    )

                                    var launched = false
                                    val targetPkg = selectedMethod.packageName
                                    if (!targetPkg.isNullOrBlank()) {
                                        try {
                                            val pkgIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                                                setPackage(targetPkg)
                                            }
                                            onUpiAppLaunched()
                                            upiLauncher.launch(pkgIntent)
                                            launched = true
                                        } catch (_: Exception) {
                                            launched = false
                                        }
                                    }

                                    if (!launched) {
                                        try {
                                            val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                            val chooser = Intent.createChooser(genericIntent, "Pay ₹$amountInput via UPI")
                                            onUpiAppLaunched()
                                            upiLauncher.launch(chooser)
                                            launched = true
                                        } catch (_: ActivityNotFoundException) {
                                            launched = false
                                        } catch (_: Exception) {
                                            launched = false
                                        }
                                    }

                                    if (!launched) {
                                        clipboard.setText(AnnotatedString(merchantUpiId))
                                        onNoUpiAppFallback()
                                        Toast.makeText(
                                            context,
                                            "No UPI app found on this device. '$merchantUpiId' copied — pay ₹$amountInput and enter your 12-digit UTR below.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("launch_upi_app_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = CarbonBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OPEN ${selectedMethod.label.uppercase()} & PAY ₹$amountInput",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = CarbonBackground
                                )
                            }
                        }

                        if (!paymentError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DangerRed.copy(alpha = 0.14f))
                                    .border(1.dp, DangerRed, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("payment_error_banner"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = paymentError,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CarbonBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // STRICT ANTI-FRAUD LOCK: Only unlock UTR & Submit button AFTER UPI payment returns positively!
                        if (!upiPaymentReturned || upiPaymentCancelled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CarbonSurface)
                                    .border(1.dp, CyberGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                                    .testTag("submit_locked_notice"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked until payment",
                                    tint = CyberGoldLight,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SUBMIT BUTTON LOCKED (ANTI-FRAUD)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = CyberGoldLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Upar wale button se ₹$amountInput payment complete hone ke baad hi 'I Have Paid' submit button khulega. Agar aap Back/Cancel karenge toh history me kuch bhi add nahi hoga.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            // Unlocked ONLY after returning from UPI payment
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ENTER 12-DIGIT UTR TO UNLOCK SUBMIT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = NeonGreenLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Anti-fraud verification ke liye apne UPI payment receipt ka 12-digit UTR / Ref ID daalein:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = utrInput,
                                onValueChange = onUtrChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_utr_input"),
                                label = { Text("12-Digit UTR / UPI Ref No. (Required)") },
                                placeholder = { Text("e.g. 429184729104") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGold,
                                    unfocusedBorderColor = CarbonBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = CarbonBackground,
                                    unfocusedContainerColor = CarbonBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Submit button appears ONLY once payment returned AND 12+ char UTR entered
                            AnimatedVisibility(visible = utrInput.trim().length >= 12) {
                                Button(
                                    onClick = onProceedPay,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("submit_payment_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = CarbonBackground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "I HAVE PAID • SUBMIT FOR VERIFICATION",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                        color = CarbonBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 3: PENDING VERIFICATION + WHATSAPP SCREENSHOT
                PaymentProcessingStatus.PENDING_VERIFICATION -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_pending_verification_view"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(CyberGold.copy(alpha = 0.18f))
                                .border(1.5.dp, CyberGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Pending Verification",
                                tint = CyberGoldLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "PAYMENT PENDING VERIFICATION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = CyberGoldLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Send payment screenshot on WhatsApp for verification. Verify hone ke baad ₹$amountInput aapke wallet me add kar diya jayega.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CarbonSurface)
                                .border(1.dp, CyberGold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Paid", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "₹$amountInput",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = CyberGoldLight
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Official UPI ID", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    merchantUpiId,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyanLight
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Status", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "PENDING",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = CyberGoldLight
                                    )
                                }
                            }
                            if (!lastTransactionRef.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Request ID", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(
                                        lastTransactionRef,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                            }
                            if (utrInput.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("UTR / Ref", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(
                                        utrInput,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NeonCyanLight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val targetUrl = whatsappLink.ifBlank { "https://wa.me/message/X53ULJ34V2FZA1" }
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "WhatsApp open nahi ho paya. Link: $targetUrl",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("send_screenshot_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Send Screenshot on WhatsApp",
                                tint = Color(0xFF06210E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SEND SCREENSHOT ON WHATSAPP",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF06210E)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pending_done_view_wallet_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "DONE • VIEW WALLET STATUS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }
                }

                PaymentProcessingStatus.SUCCESS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Deposit Approved!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹$amountInput added to your Pro GamerBattle Wallet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // STEP 1 (IDLE): ENTER AMOUNT & SELECT UPI APP -> PROCEED TO PAYMENT PAGE
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = CyberGoldLight,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ADD CASH TO WALLET",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.16f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STEP 1 / 2",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = NeonCyanLight
                            )
                        }
                    }

                    if (!paymentError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerRed.copy(alpha = 0.14f))
                                .border(1.dp, DangerRed, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("setup_payment_error_banner"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Pending Lock",
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = paymentError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = DangerRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = onAmountChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input"),
                        label = { Text("Deposit Amount (Min ₹10)") },
                        prefix = {
                            Text(
                                "₹ ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyberGoldLight
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CarbonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CarbonBackground,
                            unfocusedContainerColor = CarbonBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { chipAmount ->
                            val isSelected = amountInput == chipAmount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan else CarbonCard)
                                    .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
                                    .clickable { onAmountChange(chipAmount) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+₹$chipAmount",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) CarbonBackground else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SELECT UPI PAYMENT METHOD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = DeepAmber
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PaymentMethod.entries.forEach { method ->
                        val isSelected = selectedMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CarbonBackground else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else CarbonBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onMethodSelect(method) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onMethodSelect(method) },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = method.label,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = method.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onProceedToPaymentPage,
                        enabled = validAmount && !hasActivePendingDeposit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("proceed_to_payment_page_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            disabledContainerColor = CarbonBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (hasActivePendingDeposit) {
                                "1 REQUEST ALREADY PENDING"
                            } else {
                                "PROCEED TO PAY ₹$amountInput"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = CarbonBackground
                        )
                        if (!hasActivePendingDeposit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = CarbonBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
