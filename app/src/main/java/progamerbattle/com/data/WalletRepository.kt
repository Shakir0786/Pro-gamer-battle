package progamerbattle.com.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import progamerbattle.com.model.TransactionType
import progamerbattle.com.model.WalletTransaction
import java.util.UUID

class WalletRepository {

    private val realtimeDatabase: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance("https://new-project-bf7df-default-rtdb.firebaseio.com")
    } catch (e: Throwable) {
        null
    }

    private var activeMobile: String? = null
    private var activeListener: ValueEventListener? = null

    // Empty list by default - No mock or hardcoded transactions
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _adminDeposits = MutableStateFlow<List<AdminDepositRequest>>(emptyList())
    val adminDeposits: StateFlow<List<AdminDepositRequest>> = _adminDeposits.asStateFlow()

    init {
        attachAdminDepositListener()
    }

    private fun attachAdminDepositListener() {
        val ref = realtimeDatabase?.getReference("deposit_requests") ?: return
        try {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        _adminDeposits.value = emptyList()
                        return
                    }
                    val loaded = mutableListOf<AdminDepositRequest>()
                    for (child in snapshot.children) {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                        val mobile = child.child("mobile").getValue(String::class.java) ?: ""
                        val playerName = child.child("playerName").getValue(String::class.java) ?: "Gamer"
                        val amount = child.child("amount").getValue(Double::class.java)
                            ?: (child.child("amount").value as? Number)?.toDouble()
                            ?: child.child("amount").value?.toString()?.toDoubleOrNull()
                            ?: 0.0
                        val merchantUpiId = child.child("merchantUpiId").getValue(String::class.java) ?: MERCHANT_UPI_ID
                        val utrNumber = child.child("utrNumber").getValue(String::class.java) ?: ""
                        val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: "UPI App"
                        val timestamp = child.child("timestamp").getValue(Long::class.java)
                            ?: (child.child("timestamp").value as? Number)?.toLong() ?: System.currentTimeMillis()
                        val status = child.child("status").getValue(String::class.java) ?: "Pending"
                        val rejectReason = child.child("rejectReason").getValue(String::class.java) ?: ""
                        val adminNote = child.child("adminNote").getValue(String::class.java) ?: ""

                        loaded.add(
                            AdminDepositRequest(
                                id = id,
                                mobile = mobile,
                                playerName = playerName,
                                amount = amount,
                                merchantUpiId = merchantUpiId,
                                utrNumber = utrNumber,
                                paymentMethod = paymentMethod,
                                timestamp = timestamp,
                                status = status,
                                rejectReason = rejectReason,
                                adminNote = adminNote
                            )
                        )
                    }
                    _adminDeposits.value = loaded.sortedByDescending { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Deposit requests listener cancelled: ${error.message}")
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Failed attaching deposit listener: ${e.message}")
        }
    }

    fun loadUserTransactions(mobile: String) {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanMobile.isEmpty()) {
            _transactions.value = emptyList()
            return
        }

        // Clean up previous listener if any
        activeListener?.let { listener ->
            activeMobile?.let { prevMobile ->
                realtimeDatabase?.getReference("wallet_transactions")?.child(prevMobile)?.removeEventListener(listener)
            }
        }

        activeMobile = cleanMobile
        val ref = realtimeDatabase?.getReference("wallet_transactions")?.child(cleanMobile)
        if (ref == null) {
            return
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    _transactions.value = emptyList()
                    return
                }
                val list = mutableListOf<WalletTransaction>()
                for (child in snapshot.children) {
                    val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                    val title = child.child("title").getValue(String::class.java) ?: "Wallet Transaction"
                    val subtitle = child.child("subtitle").getValue(String::class.java)
                        ?: child.child("upiId").getValue(String::class.java)?.let { "UPI: $it" }
                        ?: ""
                    val amount = child.child("amount").getValue(Double::class.java)
                        ?: (child.child("amount").value as? Number)?.toDouble()
                        ?: child.child("amount").value?.toString()?.toDoubleOrNull()
                        ?: 0.0
                    val typeStr = child.child("type").getValue(String::class.java) ?: "DEBIT"
                    val type = if (typeStr.equals("CREDIT", ignoreCase = true)) TransactionType.CREDIT else TransactionType.DEBIT
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                        ?: (child.child("timestamp").value as? Number)?.toLong() ?: System.currentTimeMillis()
                    val status = child.child("status").getValue(String::class.java) ?: "SUCCESS"
                    val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: "Wallet"

                    val categoryStr = child.child("category").getValue(String::class.java)
                    val category = when (categoryStr?.uppercase()) {
                        "ADD_CASH" -> progamerbattle.com.model.TransactionCategory.ADD_CASH
                        "ENTRY_FEE" -> progamerbattle.com.model.TransactionCategory.ENTRY_FEE
                        "REFUND" -> progamerbattle.com.model.TransactionCategory.REFUND
                        "WITHDRAWAL" -> progamerbattle.com.model.TransactionCategory.WITHDRAWAL
                        "WINNINGS" -> progamerbattle.com.model.TransactionCategory.WINNINGS
                        "BONUS" -> progamerbattle.com.model.TransactionCategory.BONUS
                        else -> if (type == TransactionType.CREDIT) progamerbattle.com.model.TransactionCategory.ADD_CASH else progamerbattle.com.model.TransactionCategory.ENTRY_FEE
                    }
                    val matchId = child.child("matchId").getValue(String::class.java)
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val upiId = child.child("upiId").getValue(String::class.java) ?: ""
                    val utrNumber = child.child("utrNumber").getValue(String::class.java) ?: ""
                    val rejectReason = child.child("rejectReason").getValue(String::class.java) ?: ""
                    val playerName = child.child("playerName").getValue(String::class.java) ?: ""

                    list.add(
                        WalletTransaction(
                            id = id,
                            mobileNumber = cleanMobile,
                            playerName = playerName,
                            title = title,
                            subtitle = subtitle,
                            description = description,
                            amount = amount,
                            type = type,
                            category = category,
                            timestamp = timestamp,
                            status = status,
                            paymentMethod = paymentMethod,
                            matchId = matchId,
                            upiId = upiId,
                            utrNumber = utrNumber,
                            rejectReason = rejectReason
                        )
                    )
                }
                _transactions.value = list.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed loading transactions: ${error.message}")
            }
        }

        activeListener = listener
        ref.addValueEventListener(listener)
    }

    fun recordDeposit(amount: Double, method: String, reference: String, mobile: String = activeMobile ?: "") {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val txId = "tx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val newTx = WalletTransaction(
            id = txId,
            mobileNumber = cleanMobile,
            title = "Add Cash - Top Up",
            subtitle = "$method Ref:$reference",
            description = "Deposit credited to game wallet",
            amount = amount,
            type = TransactionType.CREDIT,
            category = progamerbattle.com.model.TransactionCategory.ADD_CASH,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS",
            paymentMethod = method
        )
        _transactions.update { listOf(newTx) + it }
        _allLedgerTransactions.update { listOf(newTx) + it }

        if (cleanMobile.isNotEmpty()) {
            val txMap = hashMapOf<String, Any>(
                "id" to txId,
                "mobileNumber" to cleanMobile,
                "title" to newTx.title,
                "subtitle" to newTx.subtitle,
                "amount" to amount,
                "type" to "CREDIT",
                "category" to "ADD_CASH",
                "timestamp" to newTx.timestamp,
                "status" to "SUCCESS",
                "paymentMethod" to method
            )
            realtimeDatabase?.getReference("wallet_transactions")?.child(cleanMobile)?.child(txId)?.setValue(txMap)
        }
    }

    fun hasPendingDeposit(mobile: String): Boolean {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanMobile.isBlank()) return false
        val inAdminQueue = _adminDeposits.value.any {
            it.mobile == cleanMobile && it.status.equals("Pending", ignoreCase = true)
        }
        val inUserTx = _transactions.value.any {
            (it.mobileNumber.isBlank() || it.mobileNumber == cleanMobile) &&
                it.category == progamerbattle.com.model.TransactionCategory.ADD_CASH &&
                it.status.equals("Pending", ignoreCase = true)
        }
        return inAdminQueue || inUserTx
    }

    fun isDuplicateUtr(utr: String): Boolean {
        val cleanUtr = utr.trim().uppercase()
        if (cleanUtr.isBlank()) return false
        val usedInAdminQueue = _adminDeposits.value.any {
            it.utrNumber.trim().equals(cleanUtr, ignoreCase = true) &&
                !it.status.equals("Rejected", ignoreCase = true)
        }
        val usedInLedger = (_transactions.value + _allLedgerTransactions.value).any {
            it.utrNumber.trim().equals(cleanUtr, ignoreCase = true) &&
                !it.status.equals("Rejected", ignoreCase = true)
        }
        return usedInAdminQueue || usedInLedger
    }

    /**
     * Records an Add Cash UPI payment request to progamerbattle@slc as Pending.
     * Wallet balance is NOT credited until Admin verifies the WhatsApp screenshot and approves it.
     */
    fun recordPendingDeposit(
        amount: Double,
        method: String,
        utrOrRef: String,
        mobile: String = activeMobile ?: "",
        playerName: String = "Gamer"
    ): WalletTransaction {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val cleanUtr = utrOrRef.trim()
        val cleanPlayerName = playerName.trim().ifBlank { "Gamer" }
        val txId = "dep_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val now = System.currentTimeMillis()

        val subtitleText = buildString {
            append("Paid to: $MERCHANT_UPI_ID")
            if (cleanUtr.isNotBlank()) append(" • UTR: $cleanUtr")
        }

        val pendingTx = WalletTransaction(
            id = txId,
            mobileNumber = cleanMobile,
            playerName = cleanPlayerName,
            title = "Add Cash - Pending Verify",
            subtitle = subtitleText,
            description = "Send payment screenshot on WhatsApp for verification",
            amount = amount,
            type = TransactionType.CREDIT,
            category = progamerbattle.com.model.TransactionCategory.ADD_CASH,
            timestamp = now,
            status = "Pending",
            paymentMethod = method,
            upiId = MERCHANT_UPI_ID,
            utrNumber = cleanUtr,
            rejectReason = ""
        )

        _transactions.update { listOf(pendingTx) + it }
        _allLedgerTransactions.update { listOf(pendingTx) + it }

        val depositReq = AdminDepositRequest(
            id = txId,
            mobile = cleanMobile,
            playerName = cleanPlayerName,
            amount = amount,
            merchantUpiId = MERCHANT_UPI_ID,
            utrNumber = cleanUtr,
            paymentMethod = method,
            timestamp = now,
            status = "Pending",
            rejectReason = "",
            adminNote = ""
        )
        _adminDeposits.update { listOf(depositReq) + it }

        try {
            if (cleanMobile.isNotEmpty()) {
                val txMap = hashMapOf<String, Any>(
                    "id" to txId,
                    "mobileNumber" to cleanMobile,
                    "playerName" to cleanPlayerName,
                    "title" to pendingTx.title,
                    "subtitle" to pendingTx.subtitle,
                    "description" to pendingTx.description,
                    "amount" to amount,
                    "type" to "CREDIT",
                    "category" to "ADD_CASH",
                    "timestamp" to now,
                    "status" to "Pending",
                    "paymentMethod" to method,
                    "upiId" to MERCHANT_UPI_ID,
                    "utrNumber" to cleanUtr,
                    "rejectReason" to ""
                )
                realtimeDatabase?.getReference("wallet_transactions")?.child(cleanMobile)?.child(txId)?.setValue(txMap)
            }

            val reqMap = hashMapOf<String, Any>(
                "id" to txId,
                "mobile" to cleanMobile,
                "playerName" to cleanPlayerName,
                "amount" to amount,
                "merchantUpiId" to MERCHANT_UPI_ID,
                "utrNumber" to cleanUtr,
                "paymentMethod" to method,
                "timestamp" to now,
                "status" to "Pending",
                "rejectReason" to "",
                "adminNote" to ""
            )
            realtimeDatabase?.getReference("deposit_requests")?.child(txId)?.setValue(reqMap)
        } catch (e: Throwable) {
            Log.w(TAG, "RTDB sync error on recordPendingDeposit: ${e.message}")
        }

        return pendingTx
    }

    /**
     * Admin approves a Pending deposit after verifying WhatsApp screenshot.
     * Marks transaction as SUCCESS and returns the request so wallet depositBalance can be credited.
     */
    fun approveDeposit(txId: String, adminNote: String = "Screenshot verified on WhatsApp"): AdminDepositRequest? {
        val target = _adminDeposits.value.firstOrNull { it.id == txId } ?: return null
        if (!target.status.equals("Pending", ignoreCase = true)) return null

        val note = adminNote.trim().ifBlank { "Screenshot verified & cash added to wallet" }

        _adminDeposits.update { list ->
            list.map { req ->
                if (req.id == txId) req.copy(status = "Success", adminNote = note, rejectReason = "") else req
            }
        }

        val transformTx: (WalletTransaction) -> WalletTransaction = { tx ->
            if (tx.id == txId) {
                tx.copy(
                    status = "Success",
                    title = "Add Cash - Success",
                    description = note,
                    rejectReason = ""
                )
            } else tx
        }
        _transactions.update { it.map(transformTx) }
        _allLedgerTransactions.update { it.map(transformTx) }

        try {
            realtimeDatabase?.getReference("deposit_requests")?.child(txId)?.apply {
                child("status").setValue("Success")
                child("adminNote").setValue(note)
                child("rejectReason").setValue("")
            }
            if (target.mobile.isNotBlank()) {
                realtimeDatabase?.getReference("wallet_transactions")?.child(target.mobile)?.child(txId)?.apply {
                    child("status").setValue("Success")
                    child("title").setValue("Add Cash - Success")
                    child("description").setValue(note)
                    child("rejectReason").setValue("")
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "RTDB sync error on approveDeposit: ${e.message}")
        }

        return target.copy(status = "Success", adminNote = note)
    }

    /**
     * Admin rejects a Pending deposit with a reason.
     * Marks transaction as Rejected with rejectReason visible to user.
     */
    fun rejectDeposit(txId: String, reason: String): AdminDepositRequest? {
        val target = _adminDeposits.value.firstOrNull { it.id == txId } ?: return null
        if (!target.status.equals("Pending", ignoreCase = true)) return null

        val cleanReason = reason.trim().ifBlank { "Payment screenshot not verified" }

        _adminDeposits.update { list ->
            list.map { req ->
                if (req.id == txId) req.copy(status = "Rejected", rejectReason = cleanReason) else req
            }
        }

        val transformTx: (WalletTransaction) -> WalletTransaction = { tx ->
            if (tx.id == tx
