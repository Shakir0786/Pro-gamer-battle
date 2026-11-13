package progamerbattle.com.model

enum class ReferralStatus(val label: String) {
    PENDING("Pending"),       // Friend registered, waiting for min ₹10 add cash
    CLAIMABLE("Claimable"),   // Friend added min ₹10 deposit, ready to claim
    CLAIMED("Success")        // Claimed and ₹10 credited to bonus wallet
}

data class ReferralRecord(
    val id: String = "",
    val referrerMobile: String = "",
    val refereeMobile: String = "",
    val refereeName: String = "",
    val refereeIgn: String = "",
    val rewardAmount: Double = 10.0,
    val status: ReferralStatus = ReferralStatus.PENDING,
    val registeredAt: Long = System.currentTimeMillis(),
    val depositAddedAt: Long? = null,
    val claimedAt: Long? = null
)
