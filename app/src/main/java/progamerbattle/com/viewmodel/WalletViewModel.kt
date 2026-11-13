package progamerbattle.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.model.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentProcessingStatus {
    IDLE,
    PAYMENT_PAGE,
    PROCESSING,
    PENDING_VERIFICATION,
    SUCCESS,
    FAILED
}

enum class PaymentMethod(val label: String, val subtitle: String, val packageName: String?) {
    GPAY("Google Pay", "Pay directly to progamerbattle@slc", "com.google.android.apps.nbu.paisa.user"),
    PHONEPE("PhonePe", "UPI transfer to progamerbattle@slc", "com.phonepe.app"),
    PAYTM("Paytm UPI", "Instant Paytm to progamerbattle@slc", "net.one97.paytm"),
    ANY_UPI("Any UPI App", "BHIM, Amazon Pay, FamPay, CRED", null)
}

const val MIN_WITHDRAWAL_AMOUNT = 100.0

data class WalletUiState(
    val balance: Double = 0.0,
    val depositBalance: Double = 0.0,
    val winningBalance: Double = 0.0,
    val bonusBalance: Double = 0.0,
    val isDepositSheetOpen: Boolean = false,
    val depositAmountInput: String = "100",
    val selectedMethod: PaymentMethod = PaymentMethod.GPAY,
    val paymentStatus: PaymentProcessingStatus = PaymentProcessingStatus.IDLE,
    val lastTransactionRef: String? = null,
    val lastSubmittedAmount: Double = 0.0,
    val utrInput: String = "",
    val upiAppLaunched: Boolean = false,
    val upiPaymentReturned: Boolean = false,
    val upiPaymentCancelled: Boolean = false,
    val paymentError: String? = null,
    val hasActivePendingDeposit: Boolean = false,
    val merchantUpiId: String = WalletRepository.MERCHANT_UPI_ID,
    val feedbackMessage: String? = null,
    // Withdrawal State
    val isWithdrawSheetOpen: Boolean = false,
    val withdrawAmountInput: String = "100",
    val upiIdInput: String = "",
    val withdrawError: String? = null,
    val isWithdrawing: Boolean = false,
    val withdrawSuccessMessage: String? = null
) {
    val canUnlockSubmitForVerification: Boolean
        get() = upiPaymentReturned && !upiPaymentCancelled && utrInput.trim().length >= 12
}

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    val transactions: StateFlow<List<WalletTransaction>> = walletRepository.transactions

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        balance = user?.walletBalance ?: 0.0,
                        depositBalance = user?.depositBalance ?: 0.0,
                        winningBalance = user?.winningBalance ?: 0.0,
                        bonusBalance = user?.bonusBalance ?: 0.0
                    )
                }
                if (user != null && user.mobileNumber.isNotEmpty()) {
                    walletRepository.loadUserTransactions(user.mobileNumber)
                }
            }
        }
    }

    fun openDepositSheet(initialAmount: Double? = null) {
        val amountStr = initialAmount?.toInt()?.toString() ?: _uiState.value.depositAmountInput
        val userMobile = authRepository.currentUser.value?.mobileNumber ?: ""
        val alreadyPending = walletRepository.hasPendingDeposit(userMobile)
        _uiState.update {
            it.copy(
                isDepositSheetOpen = true,
                depositAmountInput = amountStr,
                paymentStatus = PaymentProcessingStatus.IDLE,
                utrInput = "",
                upiAppLaunched = false,
                upiPaymentReturned = false,
                upiPaymentCancelled = false,
                paymentError = if (alreadyPending) {
                    "Aapka ek Add Cash request pehle se Pending hai. Pehle WhatsApp par screenshot send karke verify karwayein."
                } else null,
                hasActivePendingDeposit = alreadyPending,
                feedbackMessage = null
            )
        }
    }

    fun closeDepositSheet() {
        _uiState.update {
            it.copy(
                isDepositSheetOpen = false,
                paymentStatus = PaymentProcessingStatus.IDLE,
                utrInput = "",
                upiAppLaunched = false,
                upiPaymentReturned = false,
                upiPaymentCancelled = false,
                paymentError = null
            )
        }
    }

    fun setDepositAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() }.take(5)
        _uiState.update { it.copy(depositAmountInput = filtered, paymentError = null) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method, paymentError = null) }
    }

    fun setUtrInput(utr: String) {
        val cleaned = utr.filter { it.isLetterOrDigit() }.uppercase().take(24)
        _uiState.update { it.copy(utrInput = cleaned, paymentError = null) }
    }

    fun markUpiAppLaunched() {
        _uiState.update {
            it.copy(
                upiAppLaunched = true,
                upiPaymentCancelled = false,
                paymentError = null
            )
        }
    }

    fun onUpiAppResult(response: String?, isResultOk: Boolean) {
        val params = mutableMapOf<String, String>()
        response?.split("&")?.forEach { pair ->
            val idx = pair.indexOf('=')
            if (idx > 0) {
                val key = pair.substring(0, idx).trim().lowercase()
                val value = pair.substring(idx + 1).trim()
                params[key] = value
            }
        }
        val status = (params["status"] ?: "").uppercase()
        val txnRef = params["approvalrefno"]?.takeIf { it.isNotBlank() }
            ?: params["txnref"]?.takeIf { it.isNotBlank() }
            ?: params["txnid"]?.takeIf { it.isNotBlank() }
            ?: ""

        val explicitlyFailed = status.contains("FAIL") || status.contains("CANCEL")
        val explicitlySucceeded = status.contains("SUCCESS") || status.contains("SUBMITTED")

        if (explicitlyFailed || (!isResultOk && !explicitlySucceeded)) {
            // User backed out or cancelled inside UPI app -> keep Submit LOCKED & add NOTHING to history
            _uiState.update {
                it.copy(
                    upiPaymentReturned = false,
                    upiPaymentCancelled = true,
                    paymentError = "UPI payment cancel ya incomplete raha. History me koi entry add nahi hui hai."
                )
            }
        } else {
            // Payment completed in UPI app -> unlock UTR + Submit for verification
            _uiState.update { state ->
                val autoUtr = if (state.utrInput.isBlank() && txnRef.length >= 10) txnRef.uppercase() else state.utrInput
                state.copy(
                    upiPaymentReturned = true,
                    upiPaymentCancelled = false,
                    utrInput = autoUtr,
                    paymentError = null
                )
            }
        }
    }

    fun onNoUpiAppFallback() {
        _uiState.update {
            it.copy(
                upiAppLaunched = true,
                upiPaymentReturned = true,
                upiPaymentCancelled = false,
                paymentError = null
            )
        }
    }

    fun openUpiPaymentPage() {
        val userMobile = authRepository.currentUser.value?.mobileNumber ?: ""
        if (walletRepository.hasPendingDeposit(userMobile)) {
            _uiState.update {
                it.copy(
                    hasActivePendingDeposit = true,
                    paymentError = "Aapka ek Add Cash request pehle se Pending verification me hai. Fraud/spam rokne ke liye ek samay par sirf 1 hi request bhej sakte hain."
                )
            }
            return
        }

        val amount = _uiState.value.depositAmountInput.toDoubleOrNull()
        if (amount == null || amount < 10) {
            _uiState.update {
                it.copy(
                    paymentError = "Minimum deposit amount ₹10 hai.",
                    feedbackMessage = "Minimum deposit amount is ₹10"
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                paymentStatus = PaymentProcessingStatus.PAYMENT_PAGE,
                upiAppLaunched = false,
                upiPaymentReturned = false,
                upiPaymentCancelled = false,
                utrInput = "",
                paymentError = null,
                hasActivePendingDeposit = false,
                feedbackMessage = null
            )
        }
    }

    fun backToDepositSetup() {
        // Backing out from Payment Page discards everything -> NOTHING added to history
        _uiState.update {
            it.copy(
                paymentStatus = PaymentProcessingStatus.IDLE,
                upiAppLaunched = false,
                upiPaymentReturned = false,
                upiPaymentCancelled = false,
                utrInput = "",
                paymentError = null,
                feedbackMessage = null
            )
        }
    }

    fun isValidUtr(utr: String): Boolean {
        val clean = utr.trim()
        if (clean.length < 12) return false
        if (clean.count { it.isDigit() } < 10) return false
        if (clean.all { it == clean.first() }) return false
        if (clean == "123456789012" || clean == "012345678901" || clean == "987654321098") return false
        return true
    }

    fun submitUpiDepositForVerification() {
        val state = _uiState.value
        val amount = state.depositAmountInput.toDoubleOrNull()
        if (amount == null || amount < 10) {
            _uiState.update { it.copy(paymentError = "Minimum deposit amount is ₹10") }
            return
        }

        // Anti-Fraud Gate 1: Must have completed/returned from UPI payment (not cancelled/backed out)
        if (!state.upiPaymentReturned || state.upiPaymentCancelled) {
            _uiState.update {
                it.copy(paymentError = "Pehle UPI App se ₹${amount.toInt()} payment complete karein. Back/Cancel karne par submit nahi hoga.")
            }
            return
        }

        // Anti-Fraud Gate 2: Valid 12-digit UTR / Ref ID required
        val cleanUtr = state.utrInput.trim()
        if (!isValidUtr(cleanUtr)) {
            _uiState.update {
                it.copy(paymentError = "Valid 12-digit UTR / UPI Transaction ID daalein (e.g. 429184729104). Fake number allowed nahi hai.")
            }
            return
        }

        val currentUser = authRepository.currentUser.value
        val userMobile = currentUser?.mobileNumber ?: ""
        val playerName = currentUser?.ffIgn?.ifBlank { currentUser.fullName } ?: "Gamer"

        // Anti-Fraud Gate 3: Block spam if user already has a Pending deposit
        if (walletRepository.hasPendingDeposit(userMobile)) {
            _uiState.update {
                it.copy(
                    hasActivePendingDeposit = true,
                    paymentError = "Aapka ek payment pehle se Pending verification me hai. Pehle WhatsApp par screenshot bhej kar verify karwayein."
                )
            }
            return
        }

        // Anti-Fraud Gate 4: Block Duplicate / Reused UTR number
        if (walletRepository.isDuplicateUtr(cleanUtr)) {
            _uiState.update {
                it.copy(paymentError = "Fraud Alert: Yeh UTR number ($cleanUtr) pehle hi use ho chuka hai! Apne naye payment ka exact UTR daalein.")
            }
            return
        }

        val methodLabel = state.selectedMethod.label

        // Record as Pending only; wallet is NOT credited until Admin verifies screenshot
        val pendingTx = walletRepository.recordPendingDeposit(
            amount = amount,
            method = methodLabel,
            utrOrRef = cleanUtr,
            mobile = userMobile,
            playerName = playerName
        )

        _uiState.update {
            it.copy(
                paymentStatus = PaymentProcessingStatus.PENDING_VERIFICATION,
                lastTransactionRef = pendingTx.id,
                lastSubmittedAmount = amount,
                paymentError = null,
                hasActivePendingDeposit = true,
                feedbackMessage = "₹${amount.toInt()} request submitted (Pending). Send payment screenshot on WhatsApp for verification!"
            )
        }
    }

    fun executeSimulatedPayment() {
        submitUpiDepositForVerification()
    }

    // --- WITHDRAWAL MODULE ---
    fun openWithdrawSheet() {
        val currentWinning = _uiState.value.winningBalance
        val defaultAmt = if (currentWinning >= MIN_WITHDRAWAL_AMOUNT) "100" else ""
        _uiState.update {
            it.copy(
                isWithdrawSheetOpen = true,
                withdrawAmountInput = defaultAmt,
                upiIdInput = "",
                withdrawError = null,
                withdrawSuccessMessage = null,
                isWithdrawing = false
            )
        }
    }

    fun closeWithdrawSheet() {
        _uiState.update {
            it.copy(
                isWithdrawSheetOpen = false,
                withdrawError = null
            )
        }
    }

    fun setWithdrawAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() }.take(6)
        val amtDouble = filtered.toDoubleOrNull() ?: 0.0
        val winning = _uiState.value.winningBalance

        val error = when {
            filtered.isNotEmpty() && amtDouble < MIN_WITHDRAWAL_AMOUNT ->
                "Minimum withdrawal amount is ₹100."
            filtered.isNotEmpty() && amtDouble > winning ->
                "Insufficient winning balance. Only winnings can be withdrawn! Available winning: ₹${winning.toInt()}"
            else -> null
        }

        _uiState.update {
            it.copy(
                withdrawAmountInput = filtered,
                withdrawError = error
            )
        }
    }

    fun setUpiId(upiId: String) {
        _uiState.update {
            it.copy(
                upiIdInput = upiId.trim(),
                withdrawError = if (it.withdrawError?.contains("UPI") == true) null else it.withdrawError
            )
        }
    }

    fun executeWithdrawal() {
        val state = _uiState.value
        val amount = state.withdrawAmountInput.toDoubleOrNull() ?: 0.0
        val winning = state.winningBalance
        val upi = state.upiIdInput.trim()

        if (amount < MIN_WITHDRAWAL_AMOUNT) {
            _uiState.update { it.copy(withdrawError = "Minimum withdrawal amount is ₹100.") }
            return
        }

        if (amount > winning) {
            _uiState.update { it.copy(withdrawError = "Insufficient winning balance. Only winnings can be withdrawn! Available winning: ₹${winning.toInt()}") }
            return
        }

        if (upi.isEmpty() || !upi.contains("@") || upi.length < 5) {
            _uiState.update { it.copy(withdrawError = "Please enter a valid UPI ID (e.g. name@okhdfcbank)") }
            return
        }

        _uiState.update { it.copy(isWithdrawing = true, withdrawError = null) }

        viewModelScope.launch {
            try {
                val result = authRepository.withdrawWinningBalance(amount, upi)
                if (result.isSuccess) {
                    walletRepository.recordWithdrawal(amount, upi, "Pending")
                    val successMsg = "Withdrawal request of ₹${amount.toInt()} submitted successfully! Amount will be transferred to $upi within 24 hours."
                    _uiState.update {
                        it.copy(
                            isWithdrawing = false,
                            isWithdrawSheetOpen = false,
                            withdrawAmountInput = "100",
                            upiIdInput = "",
                            withdrawError = null,
                            withdrawSuccessMessage = successMsg,
                            feedbackMessage = successMsg
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isWithdrawing = false,
                            withdrawError = result.exceptionOrNull()?.message ?: "Withdrawal failed."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isWithdrawing = false,
                        withdrawError = e.message ?: "Failed to process withdrawal."
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                feedbackMessage = null,
                withdrawSuccessMessage = null
            )
        }
    }
}
