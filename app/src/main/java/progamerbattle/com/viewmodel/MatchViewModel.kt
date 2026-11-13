package progamerbattle.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.model.MatchPlayer
import progamerbattle.com.model.MatchStatus
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.TournamentMatch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class JoinMatchResult {
    data class Success(val match: TournamentMatch) : JoinMatchResult()
    data class InsufficientBalance(val match: TournamentMatch, val requiredAmount: Double, val currentBalance: Double) : JoinMatchResult()
    data class AlreadyJoined(val match: TournamentMatch) : JoinMatchResult()
    object MatchFull : JoinMatchResult()
    object RegistrationClosed : JoinMatchResult()
    object NotLoggedIn : JoinMatchResult()
}

class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<MatchType?>(null) // null means ALL
    val selectedFilter: StateFlow<MatchType?> = _selectedFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<MatchStatus?>(null) // null means ALL
    val selectedStatusFilter: StateFlow<MatchStatus?> = _selectedStatusFilter.asStateFlow()

    private val _survivalFilterOnly = MutableStateFlow<Boolean?>(null) // null = all, true = survival, false = per kill
    val survivalFilterOnly: StateFlow<Boolean?> = _survivalFilterOnly.asStateFlow()

    private val _selectedTierFilter = MutableStateFlow<Double?>(null)
    val selectedTierFilter: StateFlow<Double?> = _selectedTierFilter.asStateFlow()

    // Test override toggle so reviewer can test room unlock on any match anytime
    private val _forceUnlockCredentials = MutableStateFlow(false)
    val forceUnlockCredentials: StateFlow<Boolean> = _forceUnlockCredentials.asStateFlow()

    private val _joinEvents = MutableSharedFlow<JoinMatchResult>()
    val joinEvents: SharedFlow<JoinMatchResult> = _joinEvents.asSharedFlow()

    // Filtered matches for Home Tab (All custom rooms with identical conditions)
    val filteredMatches: StateFlow<List<TournamentMatch>> = combine(
        matchRepository.matches,
        _selectedStatusFilter,
        _survivalFilterOnly
    ) { matches, statusFilter, survivalOnly ->
        matches.filter { match ->
            val matchesStatus = (statusFilter == null || match.status == statusFilter)
            val matchesSurvival = when (survivalOnly) {
                null -> true
                true -> match.isSurvivalMode
                false -> !match.isSurvivalMode
            }
            matchesStatus && matchesSurvival
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All matches for My Matches & history
    val allMatches: StateFlow<List<TournamentMatch>> = matchRepository.matches

    fun setStatusFilter(status: MatchStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setSurvivalFilter(survivalOnly: Boolean?) {
        _survivalFilterOnly.value = survivalOnly
    }

    fun setTierFilter(tier: Double?) {
        _selectedTierFilter.value = tier
    }

    fun setFilter(filter: MatchType?) {
        _selectedFilter.value = filter
    }

    fun toggleForceUnlock() {
        _forceUnlockCredentials.update { !it }
    }

    fun joinMatch(match: TournamentMatch) {
        viewModelScope.launch {
            val user = authRepository.currentUser.value
            if (user == null) {
                _joinEvents.emit(JoinMatchResult.NotLoggedIn)
                return@launch
            }

            if (user.joinedMatchIds.contains(match.id) || match.joinedPlayerUids.contains(user.uid)) {
                _joinEvents.emit(JoinMatchResult.AlreadyJoined(match))
                return@launch
            }

            if (match.isRegistrationClosed) {
                _joinEvents.emit(JoinMatchResult.RegistrationClosed)
                return@launch
            }

            if (match.remainingSlots <= 0) {
                _joinEvents.emit(JoinMatchResult.MatchFull)
                return@launch
            }

            if (user.walletBalance < match.entryFee) {
                val needed = match.entryFee - user.walletBalance
                _joinEvents.emit(
                    JoinMatchResult.InsufficientBalance(
                        match = match,
                        requiredAmount = needed,
                        currentBalance = user.walletBalance
                    )
                )
                return@launch
            }

            // Deduct balance and record transaction
            val deducted = authRepository.deductBalance(match.entryFee)
            if (deducted) {
                val player = MatchPlayer(
                    uid = user.uid,
                    ign = user.inGameName,
                    uidNumber = user.inGameUid,
                    mobile = user.mobileNumber
                )
                authRepository.markMatchJoined(match.id)
                matchRepository.joinMatch(match.id, player)
                walletRepository.recordMatchDeduction(match.title, match.entryFee)
                _joinEvents.emit(JoinMatchResult.Success(match))
            }
        }
    }
}
