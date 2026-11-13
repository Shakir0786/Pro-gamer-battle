package progamerbattle.com

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import progamerbattle.com.admin.AdminViewModel
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.UserRegistry
import progamerbattle.com.data.UserSessionManager
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.model.TransactionCategory
import progamerbattle.com.model.User
import progamerbattle.com.viewmodel.PaymentProcessingStatus
import progamerbattle.com.viewmodel.WalletViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class DepositVerificationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var walletRepo: WalletRepository
    private lateinit var matchRepo: MatchRepository
    private lateinit var userRegistry: UserRegistry
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var sessionManager: UserSessionManager
    private lateinit var authRepo: AuthRepository
    private lateinit var walletViewModel: WalletViewModel

    private val testMobile = "9876500112"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        walletRepo = WalletRepository()
        matchRepo = MatchRepository()
        userRegistry = UserRegistry.getInstance(null)
        adminViewModel = AdminViewModel(
            matchRepository = matchRepo,
            walletRepository = walletRepo,
            userRegistry = userRegistry
        )

        val fakePrefs = FakeSharedPreferences()
        sessionManager = UserSessionManager(fakePrefs)
        val user = User(
            uid = "user_$testMobile",
            fullName = "Arjun Pro",
            mobileNumber = testMobile,
            ffIgn = "ArjunFF",
            ffUid = "12345678",
            secretPin = "123456",
            depositBalance = 50.0,
            winningBalance = 100.0,
            bonusBalance = 10.0
        )
        sessionManager.saveUserSession(user)
        authRepo = AuthRepository(sessionManager)
        walletViewModel = WalletViewModel(walletRepo, authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMerchantUpiIdIsOfficial() {
        assertEquals("progamerbattle@slc", WalletRepository.MERCHANT_UPI_ID)
    }

    @Test
    fun testOpeningPaymentPageAndBackCancelAddsZeroHistoryAndLocksSubmit() {
        // 1. Open Add Cash sheet -> must be IDLE (Submit locked, 0 history)
        walletViewModel.openDepositSheet(150.0)
        assertTrue(walletViewModel.uiState.value.isDepositSheetOpen)
        assertEquals(PaymentProcessingStatus.IDLE, walletViewModel.uiState.value.paymentStatus)
        assertFalse(walletViewModel.uiState.value.canUnlockSubmitForVerification)
        assertEquals(0, walletRepo.adminDeposits.value.size)

        // 2. Proceed to UPI Payment Page -> must be PAYMENT_PAGE (still NOT Pending, Submit STILL LOCKED!)
        walletViewModel.openUpiPaymentPage()
        assertEquals(PaymentProcessingStatus.PAYMENT_PAGE, walletViewModel.uiState.value.paymentStatus)
        assertFalse(walletViewModel.uiState.value.upiPaymentReturned)
        assertFalse(walletViewModel.uiState.value.canUnlockSubmitForVerification)
        assertEquals(0, walletRepo.adminDeposits.value.size)
        assertFalse(walletRepo.transactions.value.any { it.status == "Pending" })

        // 3. Attempting to submit before completing UPI payment must fail & add 0 history
        walletViewModel.setUtrInput("429100881234")
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(PaymentProcessingStatus.PAYMENT_PAGE, walletViewModel.uiState.value.paymentStatus)
        assertEquals(0, walletRepo.adminDeposits.value.size)

        // 4. User cancels or backs out inside UPI app -> Submit remains LOCKED, upiPaymentCancelled = true, 0 history
        walletViewModel.onUpiAppResult("Status=FAILURE&txnRef=429100881234", isResultOk = false)
        assertTrue(walletViewModel.uiState.value.upiPaymentCancelled)
        assertFalse(walletViewModel.uiState.value.upiPaymentReturned)
        assertFalse(walletViewModel.uiState.value.canUnlockSubmitForVerification)
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(0, walletRepo.adminDeposits.value.size)

        // 5. User presses Back from Payment Page -> resets cleanly, 0 history added
        walletViewModel.backToDepositSetup()
        assertEquals(PaymentProcessingStatus.IDLE, walletViewModel.uiState.value.paymentStatus)
        assertFalse(walletViewModel.uiState.value.upiPaymentCancelled)
        assertEquals(0, walletRepo.adminDeposits.value.size)

        // 6. Re-open Payment Page and complete UPI payment -> unlocks Submit -> Pending recorded
        walletViewModel.openUpiPaymentPage()
        walletViewModel.onUpiAppResult("Status=SUCCESS&txnRef=429100881234", isResultOk = true)
        assertTrue(walletViewModel.uiState.value.upiPaymentReturned)
        assertFalse(walletViewModel.uiState.value.upiPaymentCancelled)
        assertEquals("429100881234", walletViewModel.uiState.value.utrInput)
        assertTrue(walletViewModel.uiState.value.canUnlockSubmitForVerification)

        walletViewModel.submitUpiDepositForVerification()
        assertEquals(PaymentProcessingStatus.PENDING_VERIFICATION, walletViewModel.uiState.value.paymentStatus)
        assertEquals(1, walletRepo.adminDeposits.value.size)
        assertEquals("Pending", walletRepo.adminDeposits.value.first().status)
        assertEquals(150.0, walletViewModel.uiState.value.lastSubmittedAmount, 0.01)
    }

    @Test
    fun testAntiFraudRejectsDummyUtrDuplicateUtrAndConcurrentPendingSpam() {
        // 1. Dummy / repeated UTR ("111111111111" or "123456789012") must be blocked
        walletViewModel.openDepositSheet(200.0)
        walletViewModel.openUpiPaymentPage()
        walletViewModel.onUpiAppResult("Status=SUCCESS", isResultOk = true)

        walletViewModel.setUtrInput("111111111111")
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(0, walletRepo.adminDeposits.value.size)
        assertNotNull(walletViewModel.uiState.value.paymentError)

        walletViewModel.setUtrInput("123456789012")
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(0, walletRepo.adminDeposits.value.size)
        assertNotNull(walletViewModel.uiState.value.paymentError)

        // 2. Valid 12-digit UTR succeeds once
        walletViewModel.setUtrInput("509812346789")
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(1, walletRepo.adminDeposits.value.size)
        assertEquals(PaymentProcessingStatus.PENDING_VERIFICATION, walletViewModel.uiState.value.paymentStatus)

        // 3. Second concurrent Pending Add Cash attempt by same user is blocked
        walletViewModel.openDepositSheet(300.0)
        assertTrue(walletViewModel.uiState.value.hasActivePendingDeposit)
        walletViewModel.openUpiPaymentPage()
        // Should NOT navigate to PAYMENT_PAGE while a Pending deposit exists
        assertEquals(PaymentProcessingStatus.IDLE, walletViewModel.uiState.value.paymentStatus)
        assertEquals(1, walletRepo.adminDeposits.value.size)

        // 4. After Admin approves the first deposit, reusing the same UTR ("509812346789") is STILL blocked!
        val firstId = walletRepo.adminDeposits.value.first().id
        adminViewModel.approveDeposit(firstId, "Verified")
        assertFalse(walletRepo.hasPendingDeposit(testMobile))
        assertTrue(walletRepo.isDuplicateUtr("509812346789"))

        walletViewModel.openDepositSheet(300.0)
        walletViewModel.openUpiPaymentPage()
        walletViewModel.onUpiAppResult("Status=SUCCESS", isResultOk = true)
        walletViewModel.setUtrInput("509812346789")
        walletViewModel.submitUpiDepositForVerification()
        assertEquals(1, walletRepo.adminDeposits.value.size)
        assertTrue(walletViewModel.uiState.value.paymentError?.contains("Fraud Alert") == true)
    }

    @Test
    fun testPendingDepositDoesNotCreditWalletUntilAdminApproves() {
        // 1. User submits Add Cash payment of ₹200 to progamerbattle@slc
        val pendingTx = walletRepo.recordPendingDeposit(
            amount = 200.0,
            method = "Google Pay",
            utrOrRef = "429100881234",
            mobile = testMobile,
            playerName = "ArjunFF"
        )

        // Status must be Pending and UPI ID must be progamerbattle@slc
        assertEquals("Pending", pendingTx.status)
        assertEquals("progamerbattle@slc", pendingTx.upiId)
        assertEquals(TransactionCategory.ADD_CASH, pendingTx.category)
        assertEquals("429100881234", pendingTx.utrNumber)

        // Wallet deposit balance must STILL be 50.0 (NOT credited yet)
        val userBeforeApprove = userRegistry.findUserByMobile(testMobile)
        assertNotNull(userBeforeApprove)
        assertEquals(50.0, userBeforeApprove!!.depositBalance, 0.01)

        // Admin queue must contain this Pending request
        val adminPending = adminViewModel.deposits.value.firstOrNull { it.id == pendingTx.id }
        assertNotNull(adminPending)
        assertEquals("Pending", adminPending!!.status)
        assertEquals(200.0, adminPending.amount, 0.01)

        // 2. Admin verifies WhatsApp screenshot and approves
        adminViewModel.approveDeposit(pendingTx.id, "WhatsApp screenshot verified")

        // Deposit transaction status updates to Success
        val updatedTx = walletRepo.transactions.value.firstOrNull { it.id == pendingTx.id }
        assertNotNull(updatedTx)
        assertEquals("Success", updatedTx!!.status)
        assertEquals("", updatedTx.rejectReason)

        // Deposit request status in admin queue updates to Success
        val updatedAdminReq = adminViewModel.deposits.value.firstOrNull { it.id == pendingTx.id }
        assertEquals("Success", updatedAdminReq?.status)

        // Wallet deposit balance must NOW increase from 50.0 to 250.0
        val userAfterApprove = userRegistry.findUserByMobile(testMobile)
        assertEquals(250.0, userAfterApprove!!.depositBalance, 0.01)

        // Double approval must NOT double-credit wallet
        val secondApprove = walletRepo.approveDeposit(pendingTx.id, "Retry")
        assertNull(secondApprove)
        assertEquals(250.0, userRegistry.findUserByMobile(testMobile)!!.depositBalance, 0.01)
    }

    @Test
    fun testRejectedDepositShowsReasonAndLeavesBalanceUnchanged() {
        val pendingTx = walletRepo.recordPendingDeposit(
            amount = 500.0,
            method = "PhonePe",
            utrOrRef = "998811223344",
            mobile = testMobile,
            playerName = "ArjunFF"
        )

        val balanceBefore = userRegistry.findUserByMobile(testMobile)!!.depositBalance

        // Admin rejects with clear reason
        val reason = "Screenshot not received on WhatsApp"
        adminViewModel.rejectDeposit(pendingTx.id, reason)

        // Transaction status becomes Rejected and holds rejection reason
        val updatedTx = walletRepo.transactions.value.firstOrNull { it.id == pendingTx.id }
        assertNotNull(updatedTx)
        assertEquals("Rejected", updatedTx!!.status)
        assertEquals(reason, updatedTx.rejectReason)
        assertTrue(updatedTx.description.contains(reason))

        // Admin queue entry holds Rejected status and reason
        val updatedReq = adminViewModel.deposits.value.firstOrNull { it.id == pendingTx.id }
        assertEquals("Rejected", updatedReq?.status)
        assertEquals(reason, updatedReq?.rejectReason)

        // Balance must NOT increase
        assertEquals(balanceBefore, userRegistry.findUserByMobile(testMobile)!!.depositBalance, 0.01)
    }
}
