package progamerbattle.com.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val ffIgn: String = "",
    val ffUid: String = "",
    val secretPin: String = "",
    val depositBalance: Double = 0.0,
    val winningBalance: Double = 0.0,
    val bonusBalance: Double = 10.0,
    val joinedMatchIds: List<String> = emptyList(),
    val totalKills: Int = 0,
    val matchesWon: Int = 0,
    val matchesPlayed: Int = 0,
    val referralCode: String = "",
    val referredBy: String = "",
    val totalReferrals: Int = 0,
    val referralEarnings: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isBanned: Boolean = false
) {
    val inGameName: String get() = ffIgn
    val inGameUid: String get() = ffUid
    val walletBalance: Double get() = depositBalance + winningBalance + bonusBalance

    val myReferralCode: String
        get() = if (referralCode.isNotBlank()) referralCode else {
            val cleanIgn = ffIgn.filter { it.isLetterOrDigit() }.take(4).uppercase()
            val cleanPhone = mobileNumber.takeLast(4)
            val generated = "PGB$cleanIgn$cleanPhone"
            if (generated.length >= 6) generated else "PGB${mobileNumber.takeLast(6)}"
        }

    val daysSinceLastActive: Long
        get() = ((System.currentTimeMillis() - lastActiveAt) / (24L * 60 * 60 * 1000L)).coerceAtLeast(0)

    val isInactive30Days: Boolean
        get() = (System.currentTimeMillis() - lastActiveAt) >= (30L * 24 * 60 * 60 * 1000L)

    val activityStatus: UserActivityStatus
        get() = when {
            isBanned -> UserActivityStatus.BANNED
            isOnline -> UserActivityStatus.ONLINE
            isInactive30Days -> UserActivityStatus.INACTIVE_30_DAYS
            else -> UserActivityStatus.OFFLINE
        }
}

enum class UserActivityStatus(val label: String) {
    ONLINE("ONLINE"),
    OFFLINE("OFFLINE"),
    INACTIVE_30_DAYS("INACTIVE (30+ Days)"),
    BANNED("SUSPENDED")
}

