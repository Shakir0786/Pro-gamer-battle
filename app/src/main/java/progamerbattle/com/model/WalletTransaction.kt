package progamerbattle.com.model

enum class TransactionType {
    CREDIT,
    DEBIT
}

enum class TransactionCategory(val label: String) {
    ADD_CASH("Add Cash"),
    ENTRY_FEE("Entry Fee"),
    REFUND("Refund Fee"),
    WITHDRAWAL("Withdrawal"),
    WINNINGS("Prize Won"),
    BONUS("Bonus")
}

data class WalletTransaction(
    val id: String = "",
    val mobileNumber: String = "",
    val playerName: String = "",
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.CREDIT,
    val category: TransactionCategory = TransactionCategory.ADD_CASH,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val paymentMethod: String = "UPI",
    val matchId: String? = null,
    val upiId: String = "",
    val utrNumber: String = "",
    val rejectReason: String = ""
)
