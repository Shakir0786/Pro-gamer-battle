package progamerbattle.com.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import progamerbattle.com.model.ReferralRecord
import progamerbattle.com.model.ReferralStatus
import progamerbattle.com.model.User

/**
 * Global User Registry that persists all registered players and real-time active sessions.
 * Shared between Player App and Admin Panel.
 */
class UserRegistry private constructor(
    private val context: Context? = null
) {
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val realtimeDatabase: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance("https://new-project-bf7df-default-rtdb.firebaseio.com")
    } catch (_: Throwable) {
        null
    }

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _activeUsers = MutableStateFlow<List<User>>(emptyList())
    val activeUsers: StateFlow<List<User>> = _activeUsers.asStateFlow()

    private val _referrals = MutableStateFlow<List<ReferralRecord>>(emptyList())
    val referrals: StateFlow<List<ReferralRecord>> = _referrals.asStateFlow()

    init {
        loadUsers()
        loadReferrals()
    }

    private fun loadUsers() {
        val savedJson = prefs?.getString(KEY_SAVED_USERS_JSON, null)
        val loadedList = mutableListOf<User>()

        if (!savedJson.isNullOrBlank()) {
            try {
                val array = JSONArray(savedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loadedList.add(
                        User(
                            uid = obj.optString("uid"),
                            fullName = obj.optString("fullName"),
                            mobileNumber = obj.optString("mobileNumber"),
                            ffIgn = obj.optString("ffIgn"),
                            ffUid = obj.optString("ffUid"),
                            secretPin = obj.optString("secretPin"),
                            depositBalance = obj.optDouble("depositBalance", 0.0),
                            winningBalance = obj.optDouble("winningBalance", 0.0),
                            bonusBalance = obj.optDouble("bonusBalance", 10.0),
                            totalKills = obj.optInt("totalKills", 0),
                            matchesWon = obj.optInt("matchesWon", 0),
                            matchesPlayed = obj.optInt("matchesPlayed", 0),
                            referralCode = obj.optString("referralCode"),
                            referredBy = obj.optString("referredBy"),
                            totalReferrals = obj.optInt("totalReferrals", 0),
                            referralEarnings = obj.optDouble("referralEarnings", 0.0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            isOnline = obj.optBoolean("isOnline", false),
                            lastActiveAt = obj.optLong("lastActiveAt", System.currentTimeMillis()),
                            isBanned = obj.optBoolean("isBanned", false)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // Filter out any legacy demo accounts so only real registered players remain
        val cleanList = loadedList.filterNot {
            it.uid.startsWith("pgb-demo-") ||
            it.mobileNumber == "9876543210" ||
            it.mobileNumber == "9123456780" ||
            it.mobileNumber == "9988776655" ||
            it.mobileNumber == "9811223344" ||
            it.mobileNumber == "9755443322" ||
            it.mobileNumber == "9611224455"
        }
        if (cleanList.size != loadedList.size) {
            saveUsersInternal(cleanList)
        }

        _allUsers.value = cleanList
        updateActiveUsers(cleanList)
    }

    private fun updateActiveUsers(users: List<User>) {
        _activeUsers.value = users.filter { it.isOnline }
    }

    private fun loadReferrals() {
        val savedJson = prefs?.getString(KEY_SAVED_REFERRALS_JSON, null) ?: return
        try {
            val array = JSONArray(savedJson)
            val list = mutableListOf<ReferralRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", ReferralStatus.PENDING.name)
                val status = try { ReferralStatus.valueOf(statusStr) } catch (_: Exception) { ReferralStatus.PENDING }
                list.add(
                    ReferralRecord(
                        id = obj.optString("id"),
                        referrerMobile = obj.optString("referrerMobile"),
                        refereeMobile = obj.optString("refereeMobile"),
                        refereeName = obj.optString("refereeName"),
                        refereeIgn = obj.optString("refereeIgn"),
                        rewardAmount = obj.optDouble("rewardAmount", 10.0),
                        status = status,
                        registeredAt = obj.optLong("registeredAt", System.currentTimeMillis()),
                        depositAddedAt = if (obj.has("depositAddedAt") && !obj.isNull("depositAddedAt")) obj.optLong("depositAddedAt") else null,
                        claimedAt = if (obj.has("claimedAt") && !obj.isNull("claimedAt")) obj.optLong("claimedAt") else null
                    )
                )
            }
            _referrals.value = list
        } catch (_: Exception) {}
    }

    private fun saveReferralsInternal(list: List<ReferralRecord>) {
        try {
            val array = JSONArray()
            list.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("referrerMobile", r.referrerMobile)
                    put("refereeMobile", r.refereeMobile)
                    put("refereeName", r.refereeName)
                    put("refereeIgn", r.refereeIgn)
                    put("rewardAmount", r.rewardAmount)
                    put("status", r.status.name)
                    put("registeredAt", r.registeredAt)
                    r.depositAddedAt?.let { put("depositAddedAt", it) }
                    r.claimedAt?.let { put("claimedAt", it) }
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_SAVED_REFERRALS_JSON, array.toString())?.apply()
        } catch (_: Exception) {}
    }

    private fun saveUsersInternal(users: List<User>) {
        try {
            val array = JSONArray()
            users.forEach { u ->
                val obj = JSONObject().apply {
                    put("uid", u.uid)
                    put("fullName", u.fullName)
                    put("mobileNumber", u.mobileNumber)
                    put("ffIgn", u.ffIgn)
                    put("ffUid", u.ffUid)
                    put("secretPin", u.secretPin)
                    put("depositBalance", u.depositBalance)
                    put("winningBalance", u.winningBalance)
                    put("bonusBalance", u.bonusBalance)
                    put("totalKills", u.totalKills)
                    put("matchesWon", u.matchesWon)
                    put("matchesPlayed", u.matchesPlayed)
                    put("referralCode", u.myReferralCode)
                    put("referredBy", u.referredBy)
                    put("totalReferrals", u.totalReferrals)
                    put("referralEarnings", u.referralEarnings)
                    put("createdAt", u.createdAt)
                    put("isOnline", u.isOnline)
                    put("lastActiveAt", u.lastActiveAt)
                    put("isBanned", u.isBanned)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_SAVED_USERS_JSON, array.toString())?.apply()
        } catch (_: Exception) {}
    }

    /**
     * Records a new sign up or updates an existing user profile and active status.
     */
    fun recordUser(user: User, isOnline: Boolean = true) {
        val now = System.currentTimeMillis()
        val cleanMobile = user.mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val updatedUser = user.copy(
            mobileNumber = cleanMobile,
            isOnline = isOnline,
            lastActiveAt = now
        )

        _allUsers.update { currentList ->
            val index = currentList.indexOfFirst {
                it.mobileNumber == cleanMobile || (it.uid.isNotBlank() && it.uid == user.uid)
            }
            val newList = if (index >= 0) {
                currentList.toMutableList().apply { set(index, updatedUser) }
            } else {
                listOf(updatedUser) + currentList
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)
    }

    /**
     * Mark user as online upon login or active session restore
     */
    fun markUserOnline(mobileNumber: String) {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    it.copy(isOnline = true, lastActiveAt = System.currentTimeMillis())
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)
    }

    /**
     * Mark user as offline upon logout
     */
    fun markUserOffline(mobileNumber: String) {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    it.copy(isOnline = false, lastActiveAt = System.currentTimeMillis())
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)
    }

    private fun syncUserBalancesToCloudAndSession(user: User) {
        try {
            realtimeDatabase?.getReference("users")?.child(user.mobileNumber)?.let { ref ->
                ref.child("depositBalance").setValue(user.depositBalance)
                ref.child("winningBalance").setValue(user.winningBalance)
                ref.child("bonusBalance").setValue(user.bonusBalance)
            }
        } catch (e: Throwable) {
            try { Log.w("UserRegistry", "RTDB balance sync error: ${e.message}") } catch (_: Throwable) {}
        }
        try {
            context?.let { ctx ->
                val sessionManager = UserSessionManager(ctx)
                val current = sessionManager.getUserSession()
                if (current != null && current.mobileNumber == user.mobileNumber) {
                    sessionManager.saveUserSession(
                        current.copy(
                            depositBalance = user.depositBalance,
                            winningBalance = user.winningBalance,
                            bonusBalance = user.bonusBalance
                        )
                    )
                }
            }
        } catch (_: Throwable) {}
    }

    /**
     * Gift bonus cash to a player from the Admin Panel
     */
    fun giftBonus(mobileNumber: String, amount: Double) {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var updatedUser: User? = null
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    val u = it.copy(bonusBalance = it.bonusBalance + amount)
                    updatedUser = u
                    u
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updatedUser?.let { syncUserBalancesToCloudAndSession(it) }
        updateActiveUsers(_allUsers.value)
    }

    /**
     * Toggle ban/suspend status for a user
     */
    fun toggleBan(mobileNumber: String) {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    val newBan = !it.isBanned
                    it.copy(isBanned = newBan, isOnline = if (newBan) false else it.isOnline)
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)
    }

    /**
     * Delete user account permanently from registry and clear active session if matching
     */
    fun deleteUser(mobileNumber: String): Boolean {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var removed = false
        _allUsers.update { currentList ->
            val filtered = currentList.filterNot { it.mobileNumber == cleanMobile }
            removed = filtered.size != currentList.size
            saveUsersInternal(filtered)
            filtered
        }
        updateActiveUsers(_allUsers.value)

        // If the deleted user is currently the active session in UserSessionManager, clear it
        try {
            context?.let { ctx ->
                val sessionManager = UserSessionManager(ctx)
                if (sessionManager.getUserSession()?.mobileNumber == cleanMobile) {
                    sessionManager.clearSession()
                }
            }
        } catch (_: Throwable) {}

        return removed
    }

    /**
     * Update user balance (Deposit, Winning, Bonus) directly
     */
    fun updateBalance(mobileNumber: String, deposit: Double, winning: Double, bonus: Double): Boolean {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var updatedUser: User? = null
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    val u = it.copy(
                        depositBalance = deposit.coerceAtLeast(0.0),
                        winningBalance = winning.coerceAtLeast(0.0),
                        bonusBalance = bonus.coerceAtLeast(0.0)
                    )
                    updatedUser = u
                    u
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updatedUser?.let { syncUserBalancesToCloudAndSession(it) }
        updateActiveUsers(_allUsers.value)
        return updatedUser != null
    }

    /**
     * Adjust user balance by adding/deducting amounts
     */
    fun adjustBalance(mobileNumber: String, depositDelta: Double, winningDelta: Double, bonusDelta: Double): Boolean {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var updatedUser: User? = null
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    val u = it.copy(
                        depositBalance = (it.depositBalance + depositDelta).coerceAtLeast(0.0),
                        winningBalance = (it.winningBalance + winningDelta).coerceAtLeast(0.0),
                        bonusBalance = (it.bonusBalance + bonusDelta).coerceAtLeast(0.0)
                    )
                    updatedUser = u
                    u
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        if (updatedUser != null) {
            syncUserBalancesToCloudAndSession(updatedUser!!)
        } else if (cleanMobile.isNotBlank() && realtimeDatabase != null) {
            // Remote player fallback: adjust directly in Firebase RTDB if not cached in local UserRegistry
            try {
                val userRef = realtimeDatabase.getReference("users").child(cleanMobile)
                userRef.get().addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        val curDep = (snap.child("depositBalance").value as? Number)?.toDouble() ?: 0.0
                        val curWin = (snap.child("winningBalance").value as? Number)?.toDouble() ?: 0.0
                        val curBon = (snap.child("bonusBalance").value as? Number)?.toDouble() ?: 0.0
                        userRef.child("depositBalance").setValue((curDep + depositDelta).coerceAtLeast(0.0))
                        userRef.child("winningBalance").setValue((curWin + winningDelta).coerceAtLeast(0.0))
                        userRef.child("bonusBalance").setValue((curBon + bonusDelta).coerceAtLeast(0.0))
                    }
                }
            } catch (_: Throwable) {}
        }
        updateActiveUsers(_allUsers.value)
        return updatedUser != null
    }

    /**
     * Credits match winnings directly to a player's winning balance.
     */
    fun creditTournamentWinning(playerIdentifier: String, winningAmount: Double): Boolean {
        if (winningAmount <= 0.0) return false
        val cleanId = playerIdentifier.trim()
        val updatedUsers = mutableListOf<User>()
        _allUsers.update { currentList ->
            val newList = currentList.map { user ->
                val matchesUser = user.uid == cleanId || 
                                  user.mobileNumber == cleanId || 
                                  user.ffIgn.equals(cleanId, ignoreCase = true) || 
                                  user.ffUid == cleanId
                if (matchesUser) {
                    val u = user.copy(
                        winningBalance = user.winningBalance + winningAmount,
                        matchesWon = user.matchesWon + 1
                    )
                    updatedUsers.add(u)
                    u
                } else user
            }
            saveUsersInternal(newList)
            newList
        }
        updatedUsers.forEach { syncUserBalancesToCloudAndSession(it) }
        updateActiveUsers(_allUsers.value)
        return updatedUsers.isNotEmpty()
    }

    /**
     * Refund entry fee for a cancelled match to an enrolled user
     */
    fun refundMatchEntry(mobileNumber: String, matchId: String, amount: Double): Boolean {
        val cleanMobile = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var updated = false
        _allUsers.update { currentList ->
            val newList = currentList.map {
                if (it.mobileNumber == cleanMobile) {
                    updated = true
                    it.copy(
                        depositBalance = it.depositBalance + amount,
                        joinedMatchIds = it.joinedMatchIds.filterNot { id -> id == matchId },
                        matchesPlayed = (it.matchesPlayed - 1).coerceAtLeast(0)
                    )
                } else it
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)
        return updated
    }

    /**
     * Find user by mobile number
     */
    fun findUserByMobile(mobileNumber: String): User? {
        val clean = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        return _allUsers.value.firstOrNull { it.mobileNumber == clean }
    }

    /**
     * Find user by unique referral code or phone number
     */
    fun findUserByReferralCode(rawCode: String): User? {
        val clean = rawCode.trim().uppercase()
        if (clean.isBlank()) return null
        return _allUsers.value.firstOrNull {
            it.myReferralCode.equals(clean, ignoreCase = true) ||
            it.referralCode.equals(clean, ignoreCase = true) ||
            it.mobileNumber == rawCode.trim().replace("+91", "").replace(" ", "").replace("-", "")
        }
    }

    /**
     * Record a newly registered friend under referrer's account in PENDING status.
     * The ₹10 reward stays PENDING until the friend deposits at least ₹10.
     */
    fun recordPendingReferral(
        referrerMobile: String,
        refereeMobile: String,
        refereeName: String,
        refereeIgn: String
    ): Boolean {
        val cleanReferrer = referrerMobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val cleanReferee = refereeMobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanReferrer.isBlank() || cleanReferrer == cleanReferee) {
            return false // Prevent self-referral
        }

        val refRecord = ReferralRecord(
            id = "ref_${System.currentTimeMillis()}_${cleanReferee.takeLast(4)}",
            referrerMobile = cleanReferrer,
            refereeMobile = cleanReferee,
            refereeName = refereeName.ifBlank { "Gamer" },
            refereeIgn = refereeIgn.ifBlank { "ProGamer" },
            rewardAmount = 10.0,
            status = ReferralStatus.PENDING,
            registeredAt = System.currentTimeMillis()
        )

        // Increment total referrals count on referrer (money stays pending until claimed)
        _allUsers.update { list ->
            val newList = list.map { user ->
                if (user.mobileNumber == cleanReferrer) {
                    user.copy(totalReferrals = user.totalReferrals + 1)
                } else user
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)

        // Update local session if referrer is currently logged in on this device
        try {
            context?.let { ctx ->
                val sessionManager = UserSessionManager(ctx)
                val current = sessionManager.getUserSession()
                if (current != null && current.mobileNumber == cleanReferrer) {
                    val updatedUser = current.copy(totalReferrals = current.totalReferrals + 1)
                    sessionManager.saveUserSession(updatedUser)
                }
            }
        } catch (_: Throwable) {}

        _referrals.update { listOf(refRecord) + it.filterNot { item -> item.refereeMobile == cleanReferee } }
        saveReferralsInternal(_referrals.value)
        return true
    }

    /**
     * Called when a player adds cash (deposit).
     * If deposit amount >= ₹10, any pending referral for this player unlocks with CLAIM button!
     */
    fun recordDepositAdded(refereeMobile: String, amount: Double) {
        val cleanReferee = refereeMobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (amount < 10.0) return // Friend must add minimum ₹10 to unlock referral reward

        var updated = false
        _referrals.update { list ->
            val newList = list.map { record ->
                if (record.refereeMobile == cleanReferee && record.status == ReferralStatus.PENDING) {
                    updated = true
                    record.copy(
                        status = ReferralStatus.CLAIMABLE,
                        depositAddedAt = System.currentTimeMillis()
                    )
                } else record
            }
            if (updated) saveReferralsInternal(newList)
            newList
        }
    }

    /**
     * Called when the referrer clicks "CLAIM" on a claimable referral.
     * Credits ₹10 directly into the referrer's BONUS WALLET ONLY, updates status to SUCCESS.
     */
    fun claimReferralReward(referralId: String, claimantMobile: String): Boolean {
        val cleanClaimant = claimantMobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        var claimedRecord: ReferralRecord? = null

        _referrals.update { list ->
            val newList = list.map { record ->
                if (record.id == referralId && record.referrerMobile == cleanClaimant && record.status == ReferralStatus.CLAIMABLE) {
                    claimedRecord = record
                    record.copy(
                        status = ReferralStatus.CLAIMED,
                        claimedAt = System.currentTimeMillis()
                    )
                } else record
            }
            if (claimedRecord != null) saveReferralsInternal(newList)
            newList
        }

        val ref = claimedRecord ?: return false

        // Credit ₹10 strictly into BONUS WALLET ONLY
        _allUsers.update { list ->
            val newList = list.map { user ->
                if (user.mobileNumber == cleanClaimant) {
                    user.copy(
                        bonusBalance = user.bonusBalance + ref.rewardAmount,
                        referralEarnings = user.referralEarnings + ref.rewardAmount
                    )
                } else user
            }
            saveUsersInternal(newList)
            newList
        }
        updateActiveUsers(_allUsers.value)

        // Record in Wallet ledger
        try {
            WalletRepository.getInstance().recordReferralCredit(
                mobile = cleanClaimant,
                friendName = ref.refereeName,
                amount = ref.rewardAmount
            )
        } catch (_: Throwable) {}

        // Update active session on this device
        try {
            context?.let { ctx ->
                val sessionManager = UserSessionManager(ctx)
                val current = sessionManager.getUserSession()
                if (current != null && current.mobileNumber == cleanClaimant) {
                    val updatedUser = current.copy(
                        bonusBalance = current.bonusBalance + ref.rewardAmount,
                        referralEarnings = current.referralEarnings + ref.rewardAmount
                    )
                    sessionManager.saveUserSession(updatedUser)
                }
            }
        } catch (_: Throwable) {}

        return true
    }

    /**
     * Backward-compatibility wrapper for instant registration reward
     */
    fun creditReferralReward(referrerMobile: String, refereeMobile: String, refereeName: String): Boolean {
        return recordPendingReferral(referrerMobile, refereeMobile, refereeName, refereeName)
    }

    companion object {
        private const val PREFS_NAME = "pgb_all_registered_users_pref"
        private const val KEY_SAVED_USERS_JSON = "key_all_users_json"
        private const val KEY_SAVED_REFERRALS_JSON = "key_saved_referrals_json"

        @Volatile
        private var instance: UserRegistry? = null

        fun getInstance(context: Context? = null): UserRegistry {
            return instance ?: synchronized(this) {
                instance ?: UserRegistry(context?.applicationContext).also { instance = it }
            }
        }
    }
}
