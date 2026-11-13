package progamerbattle.com.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import progamerbattle.com.data.AdminDepositRequest
import progamerbattle.com.data.AdminWithdrawalRequest
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.model.MatchPlayer
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.ParticipantRecord
import progamerbattle.com.model.TournamentMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import progamerbattle.com.data.UserRegistry
import progamerbattle.com.model.User

enum class AdminTab(val label: String) {
    DASHBOARD("Overview"),
    TOURNAMENTS("Custom Rooms"),
    PLAYER_LISTS("User Lists"),
    RESULTS("Results & Kills"),
    WITHDRAWALS("UPI Payouts"),
    SETTINGS("App Controls")
}

enum class UserListSubTab(val label: String) {
    SIGNED_UP("Signed-Up Users"),
    ACTIVE_NOW("Currently Active"),
    MATCH_ROSTER("Match Rosters")
}

enum class UserActivityFilter(val label: String) {
    ALL("All Users"),
    ONLINE("🟢 Online"),
    OFFLINE("⚪ Offline"),
    INACTIVE_30_DAYS("⚠️ Inactive (30+ Days)")
}

data class AdminUiState(
    val isAuthenticated: Boolean = false,
    val adminPinInput: String = "",
    val authError: String? = null,
    val selectedTab: AdminTab = AdminTab.DASHBOARD,
    val userListSubTab: UserListSubTab = UserListSubTab.SIGNED_UP,
    val userActivityFilter: UserActivityFilter = UserActivityFilter.ALL,
    val selectedMatchId: String? = null,
    val playerSearchQuery: String = "",
    // Notification & Feedback
    val toastMessage: String? = null,
    // Modals
    val isCreateMatchDialogOpen: Boolean = false,
    val isBroadcastCredentialsDialogOpen: Boolean = false,
    val broadcastMatchId: String? = null,
    val broadcastRoomIdInput: String = "",
    val broadcastPasswordInput: String = "",
    // User Control Modals
    val isUserBalanceDialogOpen: Boolean = false,
    val selectedUserForBalance: User? = null,
    val editDepositInput: String = "0",
    val editWinningInput: String = "0",
    val editBonusInput: String = "0",
    val userToDelete: User? = null,
    // Match Cancellation & Automatic Refund State
    val matchToCancel: TournamentMatch? = null,
    // User Transaction History & Ledger
    val selectedUserForTransactions: User? = null,
    val selectedTransactionFilter: progamerbattle.com.model.TransactionCategory? = null,
    // New / Edit Match Form State
    val editingMatchId: String? = null,
    val newMatchTitle: String = "Bermuda Solo Arena",
    val newMatchStartTimeMillis: Long = System.currentTimeMillis() + 30 * 60 * 1000L,
    val newMatchDateFormatted: String = "",
    val newMatchTimeFormatted: String = "",
    val newMatchStartMinutes: Int = 30,
    val newMatchEntryFee: Double = 30.0,
    val newMatchEntryFeeInput: String = "30",
    val newMatchPerKill: Double = 20.0,
    val newMatchPerKillInput: String = "20",
    val newMatchTotalSlots: Int = 48,
    val newMatchSlotInput: String = "48",
    val newMatchMatchType: MatchType = MatchType.SOLO,
    val newMatchIsSurvival: Boolean = false,
    val newMatchSurvivalPrizePool: Double = 1000.0,
    val newMatchPrizePoolInput: String = "1000",
    val newMatchRankPrizes: Map<Int, Double> = defaultTop10Prizes(1000.0),
    val newMatchRoomId: String = "ROOM-FF-",
    val newMatchPassword: String = "pgb",
    // Result Declaration State
    val resultSelectedMatchId: String? = null,
    val resultBooyahWinnerName: String = "",
    val resultPlayerKills: Map<String, Int> = emptyMap(),
    val resultPlayerRanks: Map<String, Int> = emptyMap(),
    // App Controls & Remote Config State
    val maintenanceModeEnabled: Boolean = false,
    val maintenanceMessageInput: String = "Server upgrade in progress. Matches will resume shortly!",
    val announcementMessageInput: String = "",
    val minVersionInput: String = "1.0",
    val updateUrlInput: String = "https://progamerbattle.com/download",
    val whatsappLinkInput: String = "https://wa.me/message/X53ULJ34V2FZA1",
    val telegramLinkInput: String = "https://t.me/ProGamerBattle",
    val isFetchingRemoteConfig: Boolean = false
)

fun defaultTop10Prizes(pool: Double = 1000.0): Map<Int, Double> {
    if (pool <= 0.0) return emptyMap()
    val raw = mutableMapOf(
        1 to kotlin.math.round(pool * 0.35),
        2 to kotlin.math.round(pool * 0.20),
        3 to kotlin.math.round(pool * 0.12),
        4 to kotlin.math.round(pool * 0.08),
        5 to kotlin.math.round(pool * 0.06),
        6 to kotlin.math.round(pool * 0.05),
        7 to kotlin.math.round(pool * 0.04),
        8 to kotlin.math.round(pool * 0.04),
        9 to kotlin.math.round(pool * 0.03),
        10 to kotlin.math.round(pool * 0.03)
    )
    val diff = pool - raw.values.sum()
    if (diff != 0.0) {
        val currentBooyah = raw[1] ?: 0.0
        raw[1] = (currentBooyah + diff).coerceAtLeast(0.0)
    }
    return raw
}

class AdminViewModel(
    private val matchRepository: MatchRepository = MatchRepository.getInstance(),
    private val walletRepository: WalletRepository = WalletRepository.getInstance(),
    private val userRegistry: UserRegistry = UserRegistry.getInstance(),
    private val remoteConfigManager: progamerbattle.com.data.RemoteConfigManager = progamerbattle.com.data.RemoteConfigManager.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            remoteConfigManager.config.collect { rc ->
                _uiState.update { s ->
                    s.copy(
                        maintenanceModeEnabled = rc.isMaintenanceMode,
                        maintenanceMessageInput = if (s.maintenanceMessageInput.isBlank() || s.maintenanceMessageInput == "Server upgrade in progress. Matches will resume shortly!") rc.announcementMessage.ifBlank { s.maintenanceMessageInput } else s.maintenanceMessageInput,
                        announcementMessageInput = if (s.announcementMessageInput.isBlank()) rc.announcementMessage else s.announcementMessageInput,
                        minVersionInput = rc.minVersionStr,
                        updateUrlInput = rc.updateUrl,
                        whatsappLinkInput = rc.whatsappLink,
                        telegramLinkInput = rc.telegramLink,
                        isFetchingRemoteConfig = rc.isFetching
                    )
                }
            }
        }
    }

    val matches: StateFlow<List<TournamentMatch>> = matchRepository.matches
    val deposits: StateFlow<List<AdminDepositRequest>> = walletRepository.adminDeposits
    val withdrawals: StateFlow<List<AdminWithdrawalRequest>> = walletRepository.adminWithdrawals
    val allUsers: StateFlow<List<User>> = userRegistry.allUsers
    val activeUsers: StateFlow<List<User>> = userRegistry.activeUsers

    // Allowed Master Admin PINs for tournament organizers
    private val validAdminPins = setOf("759902", "123456", "999999")

    fun onPinInputChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(adminPinInput = filtered, authError = null) }
        if (filtered.length == 6) {
            verifyPin(filtered)
        }
    }

    fun verifyPin(pin: String = _uiState.value.adminPinInput) {
        if (validAdminPins.contains(pin.trim())) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    adminPinInput = "",
                    authError = null,
                    toastMessage = "Admin access granted. Welcome Host!"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    authError = "Invalid 6-digit Master Admin PIN. Access denied.",
                    adminPinInput = ""
                )
            }
        }
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectMatch(matchId: String) {
        _uiState.update {
            it.copy(
                selectedMatchId = matchId,
                resultSelectedMatchId = matchId
            )
        }
    }

    fun onPlayerSearchQueryChanged(query: String) {
        _uiState.update { it.copy(playerSearchQuery = query) }
    }

    fun selectUserListSubTab(subTab: UserListSubTab) {
        _uiState.update { it.copy(userListSubTab = subTab) }
    }

    fun giftBonusToPlayer(mobileNumber: String, amount: Double = 50.0) {
        userRegistry.giftBonus(mobileNumber, amount)
        _uiState.update { it.copy(toastMessage = "₹${amount.toInt()} Bonus successfully gifted to $mobileNumber!") }
    }

    fun togglePlayerBan(mobileNumber: String) {
        userRegistry.toggleBan(mobileNumber)
        _uiState.update { it.copy(toastMessage = "Player status updated for $mobileNumber.") }
    }

    // --- USER MANAGEMENT & CONTROL ---

    fun selectUserActivityFilter(filter: UserActivityFilter) {
        _uiState.update { it.copy(userActivityFilter = filter) }
    }

    fun openUserBalanceDialog(user: User) {
        _uiState.update {
            it.copy(
                isUserBalanceDialogOpen = true,
                selectedUserForBalance = user,
                editDepositInput = user.depositBalance.toInt().toString(),
                editWinningInput = user.winningBalance.toInt().toString(),
                editBonusInput = user.bonusBalance.toInt().toString()
            )
        }
    }

    fun closeUserBalanceDialog() {
        _uiState.update {
            it.copy(
                isUserBalanceDialogOpen = false,
                selectedUserForBalance = null
            )
        }
    }

    fun onUserBalanceFieldsChanged(deposit: String? = null, winning: String? = null, bonus: String? = null) {
        _uiState.update {
            it.copy(
                editDepositInput = deposit ?: it.editDepositInput,
                editWinningInput = winning ?: it.editWinningInput,
                editBonusInput = bonus ?: it.editBonusInput
            )
        }
    }

    fun saveUserBalance() {
        val user = _uiState.value.selectedUserForBalance ?: return
        val deposit = _uiState.value.editDepositInput.toDoubleOrNull() ?: 0.0
        val winning = _uiState.value.editWinningInput.toDoubleOrNull() ?: 0.0
        val bonus = _uiState.value.editBonusInput.toDoubleOrNull() ?: 0.0

        val winningDiff = winning - user.winningBalance
        userRegistry.updateBalance(user.mobileNumber, deposit, winning, bonus)
        if (winningDiff > 0.0) {
            walletRepository.recordWinnings(
                matchTitle = "Admin Winning Credit",
                amount = winningDiff,
                mobile = user.mobileNumber
            )
        }
        _uiState.update {
            it.copy(
                isUserBalanceDialogOpen = false,
                selectedUserForBalance = null,
                toastMessage = "Balance updated for ${user.ffIgn}: Total ₹${(deposit + winning + bonus).toInt()}"
            )
        }
    }

    fun promptDeleteUser(user: User) {
        _uiState.update { it.copy(userToDelete = user) }
    }

    fun cancelDeleteUser() {
        _uiState.update { it.copy(userToDelete = null) }
    }

    fun executeDeleteUser() {
        val user = _uiState.value.userToDelete ?: return
        val success = userRegistry.deleteUser(user.mobileNumber)
        _uiState.update {
            it.copy(
                userToDelete = null,
                toastMessage = if (success) "Account deleted successfully: ${user.ffIgn} (${user.mobileNumber})"
                               else "Failed to delete account."
            )
        }
    }

    // --- USER TRANSACTIONS & HISTORY MODAL ---

    fun openUserTransactions(user: User) {
        _uiState.update {
            it.copy(
                selectedUserForTransactions = user,
                selectedTransactionFilter = null
            )
        }
    }

    fun closeUserTransactions() {
        _uiState.update { it.copy(selectedUserForTransactions = null, selectedTransactionFilter = null) }
    }

    fun selectTransactionCategoryFilter(category: progamerbattle.com.model.TransactionCategory?) {
        _uiState.update { it.copy(selectedTransactionFilter = category) }
    }

    fun getUserTransactions(user: User): List<progamerbattle.com.model.WalletTransaction> {
        return walletRepository.getTransactionsForUser(user.mobileNumber)
    }

    // --- MATCH CANCELLATION & AUTOMATIC REFUND ---

    fun promptCancelMatch(match: TournamentMatch) {
        _uiState.update { it.copy(matchToCancel = match) }
    }

    fun dismissCancelMatch() {
        _uiState.update { it.copy(matchToCancel = null) }
    }

    fun confirmCancelMatch() {
        val match = _uiState.value.matchToCancel ?: return
        val cancelled = matchRepository.cancelMatch(match.id) ?: return

        // 1. Gather all players who enrolled in this match
        val enrolledMobiles = mutableSetOf<String>()
        // From match.enrolledPlayers
        match.enrolledPlayers.forEach { player ->
            if (player.mobile.isNotBlank()) {
                enrolledMobiles.add(player.mobile)
            }
        }
        // From userRegistry users whose joinedMatchIds contains matchId or uid matches
        userRegistry.allUsers.value.forEach { user ->
            if (user.joinedMatchIds.contains(match.id) || match.joinedPlayerUids.contains(user.uid)) {
                enrolledMobiles.add(user.mobileNumber)
            }
        }

        var totalRefunded = 0.0
        var refundCount = 0

        enrolledMobiles.forEach { mobile ->
            val clean = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
            if (clean.isNotBlank()) {
                userRegistry.refundMatchEntry(clean, match.id, match.entryFee)
                walletRepository.recordRefund(
                    matchTitle = match.title,
                    amount = match.entryFee,
                    mobile = clean,
                    matchId = match.id
                )
                totalRefunded += match.entryFee
                refundCount++
            }
        }

        _uiState.update {
            it.copy(
                matchToCancel = null,
                toastMessage = "Match '${match.title}' cancelled. ₹${totalRefunded.toInt()} refunded to $refundCount enrolled players!"
            )
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun logOutAdmin() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                adminPinInput = "",
                authError = null,
                selectedTab = AdminTab.DASHBOARD
            )
        }
    }

    // --- TOURNAMENT CREATION ---

    fun openCreatePerKillMatchDialog() {
        val nextNum = (matches.value.size + 101)
        val defaultStart = System.currentTimeMillis() + 30 * 60 * 1000L
        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        _uiState.update {
            it.copy(
                isCreateMatchDialogOpen = true,
                editingMatchId = null,
                newMatchIsSurvival = false,
                newMatchTitle = "Bermuda Per Kill Arena #$nextNum",
                newMatchStartTimeMillis = defaultStart,
                newMatchDateFormatted = dateSdf.format(Date(defaultStart)),
                newMatchTimeFormatted = timeSdf.format(Date(defaultStart)),
                newMatchStartMinutes = 30,
                newMatchEntryFee = 30.0,
                newMatchEntryFeeInput = "30",
                newMatchPerKill = 20.0,
                newMatchPerKillInput = "20",
                newMatchTotalSlots = 48,
                newMatchSlotInput = "48",
                newMatchMatchType = MatchType.SOLO,
                newMatchRoomId = "FF-KILL-$nextNum",
                newMatchPassword = "pgb$nextNum"
            )
        }
    }

    fun openCreateSurvivalMatchDialog() {
        val nextNum = (matches.value.size + 201)
        val defaultStart = System.currentTimeMillis() + 45 * 60 * 1000L
        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val defaultPool = 1000.0

        _uiState.update {
            it.copy(
                isCreateMatchDialogOpen = true,
                editingMatchId = null,
                newMatchIsSurvival = true,
                newMatchTitle = "Survival Championship (Top 10) #$nextNum",
                newMatchStartTimeMillis = defaultStart,
                newMatchDateFormatted = dateSdf.format(Date(defaultStart)),
                newMatchTimeFormatted = timeSdf.format(Date(defaultStart)),
                newMatchStartMinutes = 45,
                newMatchEntryFee = 30.0,
                newMatchEntryFeeInput = "30",
                newMatchPerKill = 0.0,
                newMatchPerKillInput = "0",
                newMatchTotalSlots = 48,
                newMatchSlotInput = "48",
                newMatchMatchType = MatchType.SOLO,
                newMatchSurvivalPrizePool = defaultPool,
                newMatchPrizePoolInput = defaultPool.toInt().toString(),
                newMatchRankPrizes = defaultTop10Prizes(defaultPool),
                newMatchRoomId = "FF-SURVIVAL-$nextNum",
                newMatchPassword = "pgb$nextNum"
            )
        }
    }

    fun openCreateMatchDialog() {
        openCreatePerKillMatchDialog()
    }

    fun openEditMatchDialog(match: TournamentMatch) {
        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val initialPrizes = if (match.customRankPrizes != null && match.customRankPrizes.isNotEmpty()) {
            match.customRankPrizes
        } else {
            (1..10).associateWith { match.getSurvivalPrizeForRank(it) }
        }

        _uiState.update {
            it.copy(
                isCreateMatchDialogOpen = true,
                editingMatchId = match.id,
                newMatchTitle = match.title,
                newMatchStartTimeMillis = match.startTimeMillis,
                newMatchDateFormatted = dateSdf.format(Date(match.startTimeMillis)),
                newMatchTimeFormatted = timeSdf.format(Date(match.startTimeMillis)),
                newMatchStartMinutes = ((match.startTimeMillis - System.currentTimeMillis()) / 60000L).toInt().coerceAtLeast(0),
                newMatchEntryFee = match.entryFee,
                newMatchEntryFeeInput = match.entryFee.toInt().toString(),
                newMatchPerKill = match.perKillReward,
                newMatchPerKillInput = match.perKillReward.toInt().toString(),
                newMatchTotalSlots = match.totalSlots,
                newMatchSlotInput = match.totalSlots.toString(),
                newMatchMatchType = match.matchType,
                newMatchIsSurvival = match.isSurvivalMode,
                newMatchSurvivalPrizePool = match.survivalPrizePool,
                newMatchPrizePoolInput = match.survivalPrizePool.toInt().toString(),
                newMatchRankPrizes = initialPrizes,
                newMatchRoomId = match.roomId,
                newMatchPassword = match.roomPassword
            )
        }
    }

    fun closeCreateMatchDialog() {
        _uiState.update { it.copy(isCreateMatchDialogOpen = false, editingMatchId = null) }
    }

    fun onNewMatchStartTimeChanged(newMillis: Long) {
        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val minutesFromNow = ((newMillis - System.currentTimeMillis()) / 60000L).toInt().coerceAtLeast(1)

        _uiState.update { state ->
            state.copy(
                newMatchStartTimeMillis = newMillis,
                newMatchDateFormatted = dateSdf.format(Date(newMillis)),
                newMatchTimeFormatted = timeSdf.format(Date(newMillis)),
                newMatchStartMinutes = minutesFromNow
            )
        }
    }

    fun onNewMatchFormatSelected(format: MatchType, defaultSlots: Int) {
        val nextNum = (allUsers.value.size + 10).coerceAtLeast(10)
        val defaultTitle = when (format) {
            MatchType.ONE_VS_ONE -> "1 vs 1 Clash Arena #$nextNum"
            MatchType.TWO_VS_TWO -> "2 vs 2 Duo Clash #$nextNum"
            MatchType.FOUR_VS_FOUR -> "4 vs 4 Squad Arena #$nextNum"
            MatchType.SOLO -> "Bermuda Solo Arena #$nextNum"
            MatchType.DUO -> "Duo Arena Battle #$nextNum"
            MatchType.SQUAD -> "Squad War Championship #$nextNum"
            MatchType.CUSTOM -> "Custom Room (1-48) #$nextNum"
        }
        val safeSlots = defaultSlots.coerceIn(1, 48)
        _uiState.update { state ->
            state.copy(
                newMatchMatchType = format,
                newMatchTotalSlots = safeSlots,
                newMatchSlotInput = safeSlots.toString(),
                newMatchTitle = defaultTitle
            )
        }
    }

    fun onNewMatchSlotsChanged(slots: Int) {
        val validSlots = slots.coerceIn(1, 48)
        _uiState.update { state ->
            state.copy(
                newMatchTotalSlots = validSlots,
                newMatchSlotInput = validSlots.toString()
            )
        }
    }

    fun onNewMatchSlotInputChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(2)
        val parsed = digitsOnly.toIntOrNull()
        _uiState.update { state ->
            state.copy(
                newMatchSlotInput = digitsOnly,
                newMatchTotalSlots = if (parsed != null) parsed.coerceIn(1, 48) else state.newMatchTotalSlots
            )
        }
    }

    fun onEntryFeeInputChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(5)
        val parsed = digitsOnly.toDoubleOrNull() ?: 0.0
        _uiState.update { state ->
            state.copy(
                newMatchEntryFeeInput = digitsOnly,
                newMatchEntryFee = parsed
            )
        }
    }

    fun onPerKillInputChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(5)
        val parsed = digitsOnly.toDoubleOrNull() ?: 0.0
        _uiState.update { state ->
            state.copy(
                newMatchPerKillInput = digitsOnly,
                newMatchPerKill = parsed
            )
        }
    }

    fun onPrizePoolInputChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(6)
        val parsed = digitsOnly.toDoubleOrNull() ?: 0.0
        _uiState.update { state ->
            val updatedPrizes = if (parsed > 0.0) defaultTop10Prizes(parsed) else state.newMatchRankPrizes
            state.copy(
                newMatchPrizePoolInput = digitsOnly,
                newMatchSurvivalPrizePool = parsed,
                newMatchRankPrizes = updatedPrizes
            )
        }
    }

    fun onUpdateRankPrize(rank: Int, amount: Double) {
        _uiState.update { state ->
            val updated = state.newMatchRankPrizes.toMutableMap()
            updated[rank] = amount.coerceAtLeast(0.0)
            val newTotal = updated.values.sum()
            state.copy(
                newMatchRankPrizes = updated,
                newMatchSurvivalPrizePool = newTotal,
                newMatchPrizePoolInput = newTotal.toInt().toString()
            )
        }
    }

    fun onDeleteRankPrize(rank: Int) {
        _uiState.update { state ->
            val updated = state.newMatchRankPrizes.toMutableMap()
            updated.remove(rank)
            val newTotal = updated.values.sum()
            state.copy(
                newMatchRankPrizes = updated,
                newMatchSurvivalPrizePool = newTotal,
                newMatchPrizePoolInput = newTotal.toInt().toString()
            )
        }
    }

    fun onAddRankPrize(rank: Int, amount: Double = 20.0) {
        _uiState.update { state ->
            val updated = state.newMatchRankPrizes.toMutableMap()
            updated[rank] = amount
            state.copy(newMatchRankPrizes = updated)
        }
    }

    fun onAutoDistributePrizes(totalPool: Double) {
        val autoPrizes = defaultTop10Prizes(totalPool)
        _uiState.update { state ->
            state.copy(
                newMatchSurvivalPrizePool = totalPool,
                newMatchPrizePoolInput = totalPool.toInt().toString(),
                newMatchRankPrizes = autoPrizes
            )
        }
    }

    fun onNewMatchFieldChanged(
        title: String? = null,
        startMinutes: Int? = null,
        entryFee: Double? = null,
        perKill: Double? = null,
        slots: Int? = null,
        roomId: String? = null,
        pass: String? = null
    ) {
        _uiState.update { state ->
            val updatedMins = startMinutes ?: state.newMatchStartMinutes
            val updatedStartMillis = if (startMinutes != null) {
                System.currentTimeMillis() + (updatedMins * 60 * 1000L)
            } else state.newMatchStartTimeMillis

            val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val validatedSlots = slots?.coerceIn(1, 48) ?: state.newMatchTotalSlots
            val fee = entryFee ?: state.newMatchEntryFee
            val kill = perKill ?: state.newMatchPerKill

            state.copy(
                newMatchTitle = title ?: state.newMatchTitle,
                newMatchStartMinutes = updatedMins,
                newMatchStartTimeMillis = updatedStartMillis,
                newMatchDateFormatted = dateSdf.format(Date(updatedStartMillis)),
                newMatchTimeFormatted = timeSdf.format(Date(updatedStartMillis)),
                newMatchEntryFee = fee,
                newMatchEntryFeeInput = fee.toInt().toString(),
                newMatchPerKill = kill,
                newMatchPerKillInput = kill.toInt().toString(),
                newMatchTotalSlots = validatedSlots,
                newMatchSlotInput = validatedSlots.toString(),
                newMatchRoomId = roomId ?: state.newMatchRoomId,
                newMatchPassword = pass ?: state.newMatchPassword
            )
        }
    }

    fun onNewMatchIsSurvivalChanged(isSurvival: Boolean) {
        val nextNum = (matches.value.size + 101)
        val defaultTitle = if (isSurvival) "Bermuda Survival Arena #$nextNum" else "Bermuda Solo Arena #$nextNum"
        _uiState.update {
            it.copy(
                newMatchIsSurvival = isSurvival,
                newMatchTitle = defaultTitle
            )
        }
    }

    fun onNewMatchSurvivalPrizePoolChanged(pool: Double) {
        _uiState.update {
            it.copy(
                newMatchSurvivalPrizePool = pool,
                newMatchPrizePoolInput = pool.toInt().toString(),
                newMatchRankPrizes = defaultTop10Prizes(pool)
            )
        }
    }

    fun createTournament() {
        val state = _uiState.value
        val safeSlots = state.newMatchTotalSlots.coerceIn(1, 48)
        val startTime = state.newMatchStartTimeMillis
        val isEditing = state.editingMatchId != null

        if (isEditing) {
            val existing = matches.value.firstOrNull { it.id == state.editingMatchId }
            if (existing != null) {
                val updatedMatch = existing.copy(
                    title = state.newMatchTitle.trim().ifBlank { existing.title },
                    matchType = state.newMatchMatchType,
                    startTimeMillis = startTime,
                    entryFee = state.newMatchEntryFee,
                    perKillReward = state.newMatchPerKill,
                    totalSlots = safeSlots,
                    roomId = state.newMatchRoomId.trim().ifBlank { existing.roomId },
                    roomPassword = state.newMatchPassword.trim().ifBlank { existing.roomPassword },
                    isSurvivalMode = state.newMatchIsSurvival,
                    survivalPrizePool = state.newMatchSurvivalPrizePool,
                    customRankPrizes = if (state.newMatchIsSurvival) state.newMatchRankPrizes else null
                )
                matchRepository.updateMatch(updatedMatch)
                _uiState.update {
                    it.copy(
                        isCreateMatchDialogOpen = false,
                        editingMatchId = null,
                        toastMessage = "Tournament '${updatedMatch.title}' updated successfully!"
                    )
                }
            }
        } else {
            val newId = "arena_${System.currentTimeMillis().toString().takeLast(6)}"
            val newMatch = TournamentMatch(
                id = newId,
                title = state.newMatchTitle.trim().ifBlank { if (state.newMatchIsSurvival) "Survival Arena (Top 10)" else "Custom Battle Arena" },
                map = "Bermuda",
                matchType = state.newMatchMatchType,
                perspective = "TPP",
                startTimeMillis = startTime,
                entryFee = state.newMatchEntryFee,
                perKillReward = state.newMatchPerKill,
                totalSlots = safeSlots,
                joinedPlayersCount = 1,
                joinedPlayerUids = emptyList(),
                enrolledPlayers = emptyList(),
                roomId = state.newMatchRoomId.trim().ifBlank { "FF-ROOM-${(100..999).random()}" },
                roomPassword = state.newMatchPassword.trim().ifBlank { "pgb123" },
                status = MatchStatus.UPCOMING,
                isSurvivalMode = state.newMatchIsSurvival,
                survivalPrizePool = state.newMatchSurvivalPrizePool,
                customRankPrizes = if (state.newMatchIsSurvival) state.newMatchRankPrizes else null
            )

            matchRepository.createMatch(newMatch)
            _uiState.update {
                it.copy(
                    isCreateMatchDialogOpen = false,
                    editingMatchId = null,
                    toastMessage = "Custom room '${newMatch.title}' (${newMatch.totalSlots} Slots • ${if (newMatch.isSurvivalMode) "Survival Top 10" else newMatch.matchType.label}) scheduled for ${state.newMatchDateFormatted} at ${state.newMatchTimeFormatted}!"
                )
            }
        }
    }

    // --- CREDENTIAL BROADCASTING ---

    fun openBroadcastCredentialsDialog(match: TournamentMatch) {
        _uiState.update {
            it.copy(
                isBroadcastCredentialsDialogOpen = true,
                broadcastMatchId = match.id,
                broadcastRoomIdInput = match.roomId,
                broadcastPasswordInput = match.roomPassword
            )
        }
    }

    fun closeBroadcastCredentialsDialog() {
        _uiState.update { it.copy(isBroadcastCredentialsDialogOpen = false) }
    }

    fun onBroadcastRoomIdChanged(value: String) {
        _uiState.update { it.copy(broadcastRoomIdInput = value) }
    }

    fun onBroadcastPasswordChanged(value: String) {
        _uiState.update { it.copy(broadcastPasswordInput = value) }
    }

    fun broadcastRoomCredentials() {
        val state = _uiState.value
        val matchId = state.broadcastMatchId ?: return
        if (state.broadcastRoomIdInput.isBlank() || state.broadcastPasswordInput.isBlank()) {
            _uiState.update { it.copy(toastMessage = "Enter valid Room ID and Room Password") }
            return
        }

        matchRepository.updateRoomCredentials(
            matchId = matchId,
            roomId = state.broadcastRoomIdInput.trim(),
            roomPassword = state.broadcastPasswordInput.trim()
        )

        _uiState.update {
            it.copy(
                isBroadcastCredentialsDialogOpen = false,
                toastMessage = "Credentials broadcasted live to all registered players!"
            )
        }
    }

    // --- MATCH STATUS OPERATOR ---

    fun startMatch(matchId: String) {
        matchRepository.updateMatchStatus(matchId, MatchStatus.ONGOING)
        _uiState.update { it.copy(toastMessage = "Match status updated to ONGOING (Combat Active)!") }
    }

    fun deleteMatch(matchId: String) {
        matchRepository.deleteMatch(matchId)
        _uiState.update { it.copy(toastMessage = "Match deleted from system.") }
    }

    // --- RESULTS & KILL INPUT ---

    fun prepareResultDeclaration(match: TournamentMatch) {
        val roster = match.defaultEnteredParticipants
        val defaultWinner = roster.firstOrNull()?.name ?: ""
        val killMap = roster.associate { it.name to it.kills }
        val rankMap = roster.mapIndexed { index, participant ->
            participant.name to (index + 1)
        }.toMap()

        _uiState.update {
            it.copy(
                selectedTab = AdminTab.RESULTS,
                resultSelectedMatchId = match.id,
                resultBooyahWinnerName = defaultWinner,
                resultPlayerKills = killMap,
                resultPlayerRanks = rankMap
            )
        }
    }

    fun setBooyahWinner(name: String) {
        _uiState.update { it.copy(resultBooyahWinnerName = name) }
    }

    fun setPlayerKills(playerName: String, kills: Int) {
        _uiState.update { state ->
            val updated = state.resultPlayerKills.toMutableMap()
            updated[playerName] = kills.coerceAtLeast(0)
            state.copy(resultPlayerKills = updated)
        }
    }

    fun setPlayerRank(playerName: String, rank: Int) {
        _uiState.update { state ->
            val updated = state.resultPlayerRanks.toMutableMap()
            updated[playerName] = rank.coerceIn(1, 48)
            state.copy(resultPlayerRanks = updated)
        }
    }

    fun finalizeMatchResults() {
        val state = _uiState.value
        val matchId = state.resultSelectedMatchId ?: return
        val currentMatch = matches.value.firstOrNull { it.id == matchId } ?: return

        val roster = currentMatch.defaultEnteredParticipants
        val winner = state.resultBooyahWinnerName.ifBlank { roster.firstOrNull()?.name ?: "Winner" }

        // Sort participants based on rank in Survival mode, or kills in Per Kill mode
        val participantsWithResults = roster.mapIndexed { index, participant ->
            val kills = state.resultPlayerKills[participant.name] ?: participant.kills
            participant.copy(kills = kills)
        }.sortedWith(
            if (currentMatch.isSurvivalMode) {
                compareBy { state.resultPlayerRanks[it.name] ?: 99 }
            } else {
                compareByDescending<ParticipantRecord> { it.name == winner }
                    .thenByDescending { it.kills }
            }
        )

        matchRepository.declareMatchResults(
            matchId = matchId,
            winnerName = winner,
            participants = participantsWithResults
        )

        // Distribute winnings to registered player wallets & record in transaction history
        var totalDistributed = 0.0
        participantsWithResults.forEachIndexed { index, participant ->
            val rankNumber = if (currentMatch.isSurvivalMode) {
                state.resultPlayerRanks[participant.name] ?: (index + 1)
            } else {
                index + 1
            }

            val rewardEarned = if (currentMatch.isSurvivalMode) {
                currentMatch.getSurvivalPrizeForRank(rankNumber)
            } else {
                val kills = participant.kills
                val rankBonus = when (rankNumber) {
                    1 -> currentMatch.perKillReward * 5.0
                    2 -> currentMatch.perKillReward * 2.5
                    3 -> currentMatch.perKillReward * 1.5
                    else -> 0.0
                }
                (kills * currentMatch.perKillReward) + rankBonus
            }

            if (rewardEarned > 0.0) {
                totalDistributed += rewardEarned
                userRegistry.creditTournamentWinning(participant.name, rewardEarned)
                userRegistry.creditTournamentWinning(participant.uidNumber, rewardEarned)

                // Find player's registered mobile number so transaction history updates live
                val matchedEnrolled = currentMatch.enrolledPlayers.firstOrNull {
                    (participant.uid.isNotBlank() && it.uid == participant.uid) ||
                    it.ign.equals(participant.name, ignoreCase = true) ||
                    (participant.uidNumber.isNotBlank() && it.uidNumber == participant.uidNumber)
                }
                val matchedUser = userRegistry.allUsers.value.firstOrNull {
                    (participant.uid.isNotBlank() && it.uid == participant.uid) ||
                    it.ffIgn.equals(participant.name, ignoreCase = true) ||
                    (participant.uidNumber.isNotBlank() && it.ffUidNumber == participant.uidNumber) ||
                    it.mobileNumber == participant.name
                }
                val targetMobile = matchedEnrolled?.mobile?.takeIf { it.isNotBlank() }
                    ?: matchedUser?.mobileNumber?.takeIf { it.isNotBlank() }
                    ?: ""

                if (targetMobile.isNotBlank()) {
                    walletRepository.recordWinnings(
                        matchTitle = currentMatch.title,
                        amount = rewardEarned,
                        mobile = targetMobile,
                        kills = participant.kills,
                        rank = rankNumber,
                        matchId = currentMatch.id
                    )
                }
            }
        }

        val modeLabel = if (currentMatch.isSurvivalMode) "Survival Prize Pool" else "Kill Bounties"
        _uiState.update {
            it.copy(
                toastMessage = "🏆 Results Announced! ₹${totalDistributed.toInt()} $modeLabel credited to winning wallets.",
                selectedTab = AdminTab.TOURNAMENTS
            )
        }
    }

    // --- ADD CASH DEPOSITS VERIFICATION ---

    fun approveDeposit(txId: String, adminNote: String = "Screenshot verified on WhatsApp") {
        val approved = walletRepository.approveDeposit(txId, adminNote)
        if (approved != null) {
            if (approved.mobile.isNotBlank()) {
                userRegistry.adjustBalance(
                    mobileNumber = approved.mobile,
                    depositDelta = approved.amount,
                    winningDelta = 0.0,
                    bonusDelta = 0.0
                )
                userRegistry.recordDepositAdded(approved.mobile, approved.amount)
            }
            _uiState.update {
                it.copy(
                    toastMessage = "✅ ₹${approved.amount.toInt()} approved & added to ${approved.playerName}'s wallet!"
                )
            }
        } else {
            _uiState.update { it.copy(toastMessage = "Unable to approve: request not pending or missing.") }
        }
    }

    fun rejectDeposit(txId: String, reason: String = "Payment screenshot not verified") {
        val cleanReason = reason.trim().ifBlank { "Payment screenshot not verified" }
        val rejected = walletRepository.rejectDeposit(txId, cleanReason)
        if (rejected != null) {
            _uiState.update {
                it.copy(toastMessage = "❌ Deposit rejected: ${rejected.rejectReason}")
            }
        } else {
            _uiState.update { it.copy(toastMessage = "Unable to reject: request not pending or missing.") }
        }
    }

    // --- WITHDRAWALS MANAGEMENT ---

    fun approveWithdrawal(txId: String, utrNumber: String = "") {
        walletRepository.approveWithdrawal(txId, utrNumber)
        _uiState.update { it.copy(toastMessage = "Withdrawal request approved! Payout completed.") }
    }

    fun rejectWithdrawal(txId: String, reason: String = "Invalid UPI details") {
        val target = withdrawals.value.firstOrNull { it.id == txId }
        walletRepository.rejectWithdrawal(txId, reason)
        if (target != null && target.mobile.isNotBlank()) {
            userRegistry.adjustBalance(target.mobile, depositDelta = 0.0, winningDelta = target.amount, bonusDelta = 0.0)
        }
        _uiState.update { it.copy(toastMessage = "Withdrawal request rejected. ₹${target?.amount?.toInt() ?: 0} refunded to player wallet.") }
    }

    // --- APP CONTROLS & REMOTE CONFIG ---

    fun onToggleMaintenanceMode(enabled: Boolean) {
        _uiState.update { it.copy(maintenanceModeEnabled = enabled) }
        val msg = _uiState.value.maintenanceMessageInput.trim().ifBlank { "Server maintenance is currently underway. Matches will resume shortly!" }
        remoteConfigManager.updateConfig(maintenanceMode = enabled, announcementMessage = msg)
        _uiState.update {
            it.copy(
                toastMessage = if (enabled) "⚠️ Maintenance Mode ENABLED! Players are in maintenance screen." else "🟢 Maintenance Mode DISABLED! App is LIVE for players."
            )
        }
    }

    fun onMaintenanceMessageChanged(text: String) {
        _uiState.update { it.copy(maintenanceMessageInput = text) }
    }

    fun onSaveMaintenanceSettings() {
        val isMaint = _uiState.value.maintenanceModeEnabled
        val msg = _uiState.value.maintenanceMessageInput.trim()
        remoteConfigManager.updateConfig(maintenanceMode = isMaint, announcementMessage = msg)
        _uiState.update { it.copy(toastMessage = "✅ Maintenance announcement saved & synced!") }
    }

    fun onAnnouncementMessageChanged(text: String) {
        _uiState.update { it.copy(announcementMessageInput = text) }
    }

    fun onBroadcastAnnouncement() {
        val msg = _uiState.value.announcementMessageInput.trim()
        remoteConfigManager.updateConfig(announcementMessage = msg)
        _uiState.update {
            it.copy(toastMessage = if (msg.isNotBlank()) "📢 Broadcast announcement pushed to all players!" else "Announcement cleared.")
        }
    }

    fun onMinVersionChanged(text: String) {
        _uiState.update { it.copy(minVersionInput = text) }
    }

    fun onUpdateUrlChanged(url: String) {
        _uiState.update { it.copy(updateUrlInput = url) }
    }

    fun onSaveUpdateSettings() {
        val ver = _uiState.value.minVersionInput.trim()
        val url = _uiState.value.updateUrlInput.trim()
        val verCode = ver.toLongOrNull() ?: 1L
        remoteConfigManager.updateConfig(minVersionStr = ver, minVersionCode = verCode, updateUrl = url)
        _uiState.update { it.copy(toastMessage = "🚀 Update Link & Min Version (v$ver) Published!") }
    }

    fun onWhatsappLinkChanged(link: String) {
        _uiState.update { it.copy(whatsappLinkInput = link) }
    }

    fun onTelegramLinkChanged(link: String) {
        _uiState.update { it.copy(telegramLinkInput = link) }
    }

    fun onSaveSocialLinks() {
        val wa = _uiState.value.whatsappLinkInput.trim()
        val tg = _uiState.value.telegramLinkInput.trim()
        remoteConfigManager.updateConfig(whatsappLink = wa, telegramLink = tg)
        _uiState.update { it.copy(toastMessage = "✅ Support links updated (WhatsApp & Telegram)!") }
    }

    fun onRefreshRemoteConfig() {
        remoteConfigManager.fetchNow()
        _uiState.update { it.copy(toastMessage = "🔄 Syncing live with Firebase Remote Config...") }
    }
}
