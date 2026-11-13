package progamerbattle.com.data

import android.util.Log
import progamerbattle.com.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID
import android.content.Context

class AuthRepository(
    val sessionManager: UserSessionManager? = null
) {
    constructor(context: Context) : this(UserSessionManager(context))

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Throwable) {
        null
    }

    private val realtimeDatabase: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance("https://new-project-bf7df-default-rtdb.firebaseio.com")
    } catch (e: Throwable) {
        null
    }

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        null
    }

    // In-memory backing cache for fast, responsive UI
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var observedMobile: String? = null
    private var rtdbUserListener: ValueEventListener? = null

    init {
        // Automatically restore active persistent user session on launch
        sessionManager?.getUserSession()?.let { restoredUser ->
            _currentUser.value = restoredUser
            UserRegistry.getInstance().recordUser(restoredUser, isOnline = true)
            attachRealtimeUserObserver(restoredUser.mobileNumber)
            try {
                Log.i(TAG, "Restored active persistent user session for ${restoredUser.mobileNumber}")
            } catch (_: Throwable) {}
        }

        // Observe in-process UserRegistry updates (e.g., Admin approving Add Cash deposit)
        repoScope.launch {
            UserRegistry.getInstance().allUsers.drop(1).collect { users ->
                val current = _currentUser.value ?: return@collect
                val remoteMatch = users.firstOrNull { it.mobileNumber == current.mobileNumber } ?: return@collect
                if (remoteMatch.depositBalance != current.depositBalance ||
                    remoteMatch.winningBalance != current.winningBalance ||
                    remoteMatch.bonusBalance != current.bonusBalance ||
                    remoteMatch.isBanned != current.isBanned
                ) {
                    val synced = current.copy(
                        depositBalance = remoteMatch.depositBalance,
                        winningBalance = remoteMatch.winningBalance,
                        bonusBalance = remoteMatch.bonusBalance,
                        isBanned = remoteMatch.isBanned
                    )
                    _currentUser.value = synced
                    sessionManager?.saveUserSession(synced)
                }
            }
        }
    }

    private fun attachRealtimeUserObserver(mobileNumber: String) {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanMobile.isBlank()) return
        val rtdb = realtimeDatabase ?: return

        rtdbUserListener?.let { oldListener ->
            observedMobile?.let { oldMobile ->
                try {
                    rtdb.getReference("users").child(oldMobile).removeEventListener(oldListener)
                } catch (_: Throwable) {}
            }
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val current = _currentUser.value ?: return
                if (current.mobileNumber != cleanMobile) return

                val dep = snapshot.child("depositBalance").getValue(Double::class.java)
                    ?: (snapshot.child("depositBalance").value as? Number)?.toDouble()
                    ?: current.depositBalance
                val win = snapshot.child("winningBalance").getValue(Double::class.java)
                    ?: (snapshot.child("winningBalance").value as? Number)?.toDouble()
                    ?: current.winningBalance
                val bon = snapshot.child("bonusBalance").getValue(Double::class.java)
                    ?: (snapshot.child("bonusBalance").value as? Number)?.toDouble()
                    ?: current.bonusBalance

                if (dep != current.depositBalance || win != current.winningBalance || bon != current.bonusBalance) {
                    val synced = current.copy(
                        depositBalance = dep,
                        winningBalance = win,
                        bonusBalance = bon
                    )
                    _currentUser.value = synced
                    sessionManager?.saveUserSession(synced)
                    UserRegistry.getInstance().recordUser(synced, isOnline = true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                try { Log.w(TAG, "User RTDB listener cancelled: ${error.message}") } catch (_: Throwable) {}
            }
        }

        observedMobile = cleanMobile
        rtdbUserListener = listener
        try {
            rtdb.getReference("users").child(cleanMobile).addValueEventListener(listener)
        } catch (_: Throwable) {}
    }

    private fun detachRealtimeUserObserver() {
        val rtdb = realtimeDatabase ?: return
        rtdbUserListener?.let { oldListener ->
            observedMobile?.let { oldMobile ->
                try {
                    rtdb.getReference("users").child(oldMobile).removeEventListener(oldListener)
                } catch (_: Throwable) {}
            }
        }
        rtdbUserListener = null
        observedMobile = null
    }

    fun mapMobileToInternalEmail(mobile: String): String {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        return "$cleanMobile@progamerbattle.internal"
    }

    suspend fun signUpWithMobile(
        fullName: String,
        mobileNumber: String,
        inGameName: String,
        inGameUid: String,
        pin: String,
        referralCode: String = ""
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            withTimeout(12000L) {
                val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
                val cleanPin = pin.trim()
                val email = mapMobileToInternalEmail(cleanMobile)
                var generatedUid = "user_${cleanMobile}_${UUID.randomUUID().toString().take(6)}"

                // Anti-Fake Referral Validation & Reward
                var validReferredBy = ""
                val cleanReferral = referralCode.trim().uppercase()
                if (cleanReferral.isNotBlank()) {
                    // Rule 1: Strictly reject self-referral
                    if (cleanReferral == cleanMobile || cleanReferral.contains(cleanMobile)) {
                        return@withTimeout Result.failure(Exception("Self-referral is strictly prohibited. Enter your friend's code or leave blank."))
                    }

                    // Rule 2: Must be an authentic registered player
                    val referrer = UserRegistry.getInstance().findUserByReferralCode(cleanReferral)
                    if (referrer == null) {
                        return@withTimeout Result.failure(Exception("Invalid referral code '$cleanReferral'. Please check with your friend or leave empty."))
                    }

                    if (referrer.mobileNumber == cleanMobile) {
                        return@withTimeout Result.failure(Exception("Self-referral is strictly prohibited."))
                    }

                    validReferredBy = referrer.mobileNumber

                    // Record pending referral for referrer (stays PENDING until friend deposits min ₹10)
                    UserRegistry.getInstance().recordPendingReferral(
                        referrerMobile = referrer.mobileNumber,
                        refereeMobile = cleanMobile,
                        refereeName = fullName.trim(),
                        refereeIgn = inGameName.trim()
                    )
                }

                // 1. Firebase Auth (Optional helper)
                if (firebaseAuth != null) {
                    try {
                        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, cleanPin).await()
                        authResult.user?.uid?.let { generatedUid = it }
                    } catch (e: Exception) {
                        try {
                            val authResult = firebaseAuth.signInWithEmailAndPassword(email, cleanPin).await()
                            authResult.user?.uid?.let { generatedUid = it }
                        } catch (ignored: Exception) {
                            Log.w(TAG, "FirebaseAuth fallback skipped: ${ignored.message}")
                        }
                    }
                }

                val userReferralCode = "PGB${inGameName.trim().filter { it.isLetterOrDigit() }.take(4).uppercase()}${cleanMobile.takeLast(4)}"

                // 2. Realtime Database Save at users/{mobile}
                val userMap = hashMapOf<String, Any>(
                    "fullName" to fullName.trim(),
                    "mobileNumber" to cleanMobile,
                    "ffIgn" to inGameName.trim(),
                    "ffUid" to inGameUid.trim(),
                    "secretPin" to cleanPin,
                    "depositBalance" to 0.0,
                    "winningBalance" to 0.0,
                    "bonusBalance" to 10.0, // Sign-up bonus ₹10
                    "referralCode" to userReferralCode,
                    "referredBy" to validReferredBy,
                    "totalReferrals" to 0,
                    "referralEarnings" to 0.0,
                    "stats" to hashMapOf(
                        "matchesPlayed" to 0,
                        "matchesWon" to 0,
                        "totalKills" to 0
                    )
                )

                realtimeDatabase?.let { rtdb ->
                    try {
                        rtdb.getReference("users").child(cleanMobile).setValue(userMap).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed writing to Realtime Database users/$cleanMobile: ${e.message}")
                    }
                }

                val newUser = User(
                    uid = generatedUid,
                    fullName = fullName.trim(),
                    mobileNumber = cleanMobile,
                    ffIgn = inGameName.trim(),
                    ffUid = inGameUid.trim(),
                    secretPin = cleanPin,
                    depositBalance = 0.0,
                    winningBalance = 0.0,
                    bonusBalance = 10.0, // Sign-up bonus ₹10
                    joinedMatchIds = emptyList(),
                    totalKills = 0,
                    matchesWon = 0,
                    matchesPlayed = 0,
                    referralCode = userReferralCode,
                    referredBy = validReferredBy,
                    totalReferrals = 0,
                    referralEarnings = 0.0
                )

                _currentUser.value = newUser
                sessionManager?.saveUserSession(newUser)
                UserRegistry.getInstance().recordUser(newUser, isOnline = true)
                attachRealtimeUserObserver(newUser.mobileNumber)
                WalletRepository.getInstance().recordWelcomeBonus(cleanMobile, 10.0)
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during signUpWithMobile", e)
            val friendlyMsg = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                "Registration timed out. Please check your internet connection."
            } else {
                e.message ?: "Registration failed. Please try again."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun loginWithMobile(
        mobileNumber: String,
        pin: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            withTimeout(12000L) {
                val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
                val cleanPin = pin.trim()

                // Check Realtime Database users/{mobile}
                if (realtimeDatabase != null) {
                    try {
                        val snapshot = realtimeDatabase.getReference("users").child(cleanMobile).get().await()
                        if (!snapshot.exists()) {
                            return@withTimeout Result.failure(Exception("User not registered. Please register first."))
                        }

                        val storedPin = snapshot.child("secretPin").getValue(String::class.java)
                            ?: snapshot.child("secretPin").value?.toString()

                        if (storedPin != cleanPin) {
                            return@withTimeout Result.failure(Exception("Incorrect 6-digit Secret PIN."))
                        }

                        // Parse fields directly from users/{mobile}
                        val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Gamer"
                        val ffIgn = snapshot.child("ffIgn").getValue(String::class.java) ?: "ProGamer"
                        val ffUid = snapshot.child("ffUid").getValue(String::class.java) ?: "000000"
                        val deposit = snapshot.child("depositBalance").getValue(Double::class.java)
                            ?: (snapshot.child("depositBalance").value as? Number)?.toDouble() ?: 0.0
                        val winning = snapshot.child("winningBalance").getValue(Double::class.java)
                            ?: (snapshot.child("winningBalance").value as? Number)?.toDouble() ?: 0.0
                        val bonus = snapshot.child("bonusBalance").getValue(Double::class.java)
                            ?: (snapshot.child("bonusBalance").value as? Number)?.toDouble() ?: 10.0
                        val refCode = snapshot.child("referralCode").getValue(String::class.java) ?: ""
                        val refBy = snapshot.child("referredBy").getValue(String::class.java) ?: ""
                        val totalRefs = snapshot.child("totalReferrals").getValue(Int::class.java)
                            ?: (snapshot.child("totalReferrals").value as? Number)?.toInt() ?: 0
                        val refEarnings = snapshot.child("referralEarnings").getValue(Double::class.java)
                            ?: (snapshot.child("referralEarnings").value as? Number)?.toDouble() ?: 0.0

                        // Read career stats directly from users/{mobile}/stats, default to 0
                        val statsNode = snapshot.child("stats")
                        val matchesPlayed = statsNode.child("matchesPlayed").getValue(Int::class.java)
                            ?: (statsNode.child("matchesPlayed").value as? Number)?.toInt()
                            ?: snapshot.child("matchesPlayed").getValue(Int::class.java)
                            ?: (snapshot.child("matchesPlayed").value as? Number)?.toInt()
                            ?: 0
                        val matchesWon = statsNode.child("matchesWon").getValue(Int::class.java)
                            ?: (statsNode.child("matchesWon").value as? Number)?.toInt()
                            ?: snapshot.child("matchesWon").getValue(Int::class.java)
                            ?: (snapshot.child("matchesWon").value as? Number)?.toInt()
                            ?: 0
                        val totalKills = statsNode.child("totalKills").getValue(Int::class.java)
                            ?: (statsNode.child("totalKills").value as? Number)?.toInt()
                            ?: snapshot.child("totalKills").getValue(Int::class.java)
                            ?: (snapshot.child("totalKills").value as? Number)?.toInt()
                            ?: 0

                        val resolvedUser = User(
                            uid = "user_$cleanMobile",
                            fullName = fullName,
                            mobileNumber = cleanMobile,
                            ffIgn = ffIgn,
                            ffUid = ffUid,
                            secretPin = cleanPin,
                            depositBalance = deposit,
                            winningBalance = winning,
                            bonusBalance = bonus,
                            joinedMatchIds = emptyList(),
                            totalKills = totalKills,
                            matchesWon = matchesWon,
                            matchesPlayed = matchesPlayed,
                            referralCode = refCode,
                            referredBy = refBy,
                            totalReferrals = totalRefs,
                            referralEarnings = refEarnings
                        )

                        _currentUser.value = resolvedUser
                        sessionManager?.saveUserSession(resolvedUser)
                        UserRegistry.getInstance().recordUser(resolvedUser, isOnline = true)
                        attachRealtimeUserObserver(resolvedUser.mobileNumber)
                        return@withTimeout Result.success(resolvedUser)
                    } catch (e: Exception) {
                        Log.e(TAG, "Realtime Database login error: ${e.message}")
                        if (e.message?.contains("User not registered") == true || e.message?.contains("Incorrect") == true) {
                            return@withTimeout Result.failure(e)
                        }
                    }
                }

                val existing = _currentUser.value
                if (existing != null && existing.mobileNumber == cleanMobile) {
                    if (existing.secretPin.isNotEmpty() && existing.secretPin != cleanPin) {
                        return@withTimeout Result.failure(Exception("Incorrect 6-digit Secret PIN."))
                    }
                    sessionManager?.saveUserSession(existing)
                    return@withTimeout Result.success(existing)
                }

                // If not registered in database or local state, strictly block
                Result.failure(Exception("User not registered. Please register first."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during loginWithMobile", e)
            val friendlyMsg = if (e is kotlinx.coroutines.TimeoutCancellationException) {
                "Connection timed out. Please check network and try again."
            } else {
                e.message ?: "Login failed. Verify credentials."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun withdrawWinningBalance(
        amount: Double,
        upiId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value
            ?: return@withContext Result.failure(Exception("User session expired. Please log in again."))

        if (amount < 100.0) {
            return@withContext Result.failure(Exception("Minimum withdrawal amount is ₹100."))
        }

        if (amount > user.winningBalance) {
            return@withContext Result.failure(Exception("Insufficient winning balance. Only winnings can be withdrawn! Available winning: ₹${user.winningBalance.toInt()}"))
        }

        try {
            val cleanMobile = user.mobileNumber
            val newWinningBalance = user.winningBalance - amount

            // 1. Update users/{mobile}/winningBalance in Realtime Database
            realtimeDatabase?.let { rtdb ->
                try {
                    rtdb.getReference("users").child(cleanMobile).child("winningBalance").setValue(newWinningBalance).await()

                    // 2. Insert transaction record into wallet_transactions/{mobile}
                    val txId = "wd_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
                    val txMap = hashMapOf<String, Any>(
                        "id" to txId,
                        "amount" to amount,
                        "upiId" to upiId.trim(),
                        "timestamp" to System.currentTimeMillis(),
                        "type" to "WITHDRAWAL",
                        "status" to "Pending"
                    )
                    rtdb.getReference("wallet_transactions").child(cleanMobile).child(txId).setValue(txMap).await()
                } catch (e: Exception) {
                    Log.e(TAG, "RTDB withdrawal sync error: ${e.message}")
                }
            }

            // 3. Update local in-memory user
            val updated = user.copy(winningBalance = newWinningBalance)
            _currentUser.value = updated
            sessionManager?.saveUserSession(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Withdrawal execution error", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        val mobile = _currentUser.value?.mobileNumber
        detachRealtimeUserObserver()
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out exception: ${e.message}")
        }
        sessionManager?.clearSession()
        _currentUser.value = null
        if (!mobile.isNullOrBlank()) {
            UserRegistry.getInstance().markUserOffline(mobile)
        }
    }

    fun updateProfile(inGameName: String, inGameUid: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            ffIgn = inGameName.trim(),
            ffUid = inGameUid.trim()
        )
        _currentUser.value = updated
        sessionManager?.saveUserSession(updated)
        realtimeDatabase?.getReference("users")?.child(updated.mobileNumber)?.child("ffIgn")?.setValue(updated.ffIgn)
        realtimeDatabase?.getReference("users")?.child(updated.mobileNumber)?.child("ffUid")?.setValue(updated.ffUid)
    }

    fun deductBalance(amount: Double): Boolean {
        val user = _currentUser.value ?: return false
        if (user.walletBalance < amount) return false

        // Deduct entry fee prioritizing depositBalance, then bonusBalance, then winningBalance
        var remainingToDeduct = amount
        var newDeposit = user.depositBalance
        var newBonus = user.bonusBalance
        var newWinning = user.winningBalance

        if (newDeposit >= remainingToDeduct) {
            newDeposit -= remainingToDeduct
            remainingToDeduct = 0.0
        } else {
            remainingToDeduct -= newDeposit
            newDeposit = 0.0
        }

        if (remainingToDeduct > 0 && newBonus >= remainingToDeduct) {
            newBonus -= remainingToDeduct
            remainingToDeduct = 0.0
        } else if (remainingToDeduct > 0) {
            remainingToDeduct -= newBonus
            newBonus = 0.0
        }

        if (remainingToDeduct > 0 && newWinning >= remainingToDeduct) {
            newWinning -= remainingToDeduct
            remainingToDeduct = 0.0
        }

        val updated = user.copy(
            depositBalance = newDeposit,
            bonusBalance = newBonus,
            winningBalance = newWinning
        )
        _currentUser.value = updated
        sessionManager?.saveUserSession(updated)
        UserRegistry.getInstance().recordUser(updated, isOnline = true)

        realtimeDatabase?.getReference("users")?.child(updated.mobileNumber)?.let { ref ->
            ref.child("depositBalance").setValue(newDeposit)
            ref.child("bonusBalance").setValue(newBonus)
            ref.child("winningBalance").setValue(newWinning)
        }
        return true
    }

    fun addDeposit(amount: Double) {
        val user = _currentUser.value ?: return
        val updated = user.copy(depositBalance = user.depositBalance + amount)
        _currentUser.value = updated
        sessionManager?.saveUserSession(updated)
        UserRegistry.getInstance().recordUser(updated, isOnline = true)
        realtimeDatabase?.getReference("users")?.child(updated.mobileNumber)?.child("depositBalance")?.setValue(updated.depositBalance)
        // Unlock pending referral if deposit is min ₹10
        UserRegistry.getInstance().recordDepositAdded(updated.mobileNumber, amount)
    }

    fun addBalance(amount: Double) {
        addDeposit(amount)
    }

    fun markMatchJoined(matchId: String) {
        val user = _currentUser.value ?: return
        if (!user.joinedMatchIds.contains(matchId)) {
            val updated = user.copy(
                joinedMatchIds = user.joinedMatchIds + matchId,
                matchesPlayed = user.matchesPlayed + 1
            )
            _currentUser.value = updated
            sessionManager?.saveUserSession(updated)
        }
    }

    fun refundMatch(matchId: String, amount: Double) {
        val user = _currentUser.value ?: return
        if (user.joinedMatchIds.contains(matchId)) {
            val updated = user.copy(
                depositBalance = user.depositBalance + amount,
                joinedMatchIds = user.joinedMatchIds.filterNot { it == matchId },
                matchesPlayed = (user.matchesPlayed - 1).coerceAtLeast(0)
            )
            _currentUser.value = updated
            sessionManager?.saveUserSession(updated)
            realtimeDatabase?.getReference("users")?.child(updated.mobileNumber)?.child("depositBalance")?.setValue(updated.depositBalance)
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
