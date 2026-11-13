package progamerbattle.com.model

enum class MatchType(val label: String) {
    SOLO("Solo"),
    DUO("Duo"),
    SQUAD("Squad"),
    ONE_VS_ONE("1 vs 1"),
    TWO_VS_TWO("2 vs 2"),
    FOUR_VS_FOUR("4 vs 4"),
    CUSTOM("Custom")
}

enum class MatchStatus(val label: String) {
    UPCOMING("Upcoming"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class MatchPlayer(
    val uid: String = "",
    val ign: String = "",
    val uidNumber: String = "",
    val mobile: String = "",
    val joinedAt: Long = System.currentTimeMillis()
)

data class ParticipantRecord(
    val uid: String = "",
    val name: String = "",
    val uidNumber: String = "",
    val kills: Int = 0
)

data class ParticipantRanking(
    val rank: String,
    val rankNumber: Int,
    val name: String,
    val kills: Int,
    val earnedAmount: Double
)

// Backwards compatibility alias for tests
typealias SoloRankStanding = ParticipantRanking
typealias SquadWinnerMember = ParticipantRanking

data class TournamentMatch(
    val id: String = "",
    val title: String = "",
    val map: String = "Bermuda",
    val matchType: MatchType = MatchType.SOLO,
    val perspective: String = "TPP",
    val startTimeMillis: Long = System.currentTimeMillis() + 30 * 60 * 1000L,
    val entryFee: Double = 30.0,
    val perKillReward: Double = 20.0,
    val totalSlots: Int = 48,
    val joinedPlayersCount: Int = 0,
    val joinedPlayerUids: List<String> = emptyList(),
    val enrolledPlayers: List<MatchPlayer> = emptyList(),
    val roomId: String = "",
    val roomPassword: String = "",
    val status: MatchStatus = MatchStatus.UPCOMING,
    val winnerName: String = "",
    val userKillCount: Int = 0,
    val userWinnings: Double = 0.0,
    val customPrizePool: Double? = null,
    val customParticipants: List<ParticipantRecord>? = null,
    val isSurvivalMode: Boolean = false,
    val survivalPrizePool: Double = 1000.0,
    val customRankPrizes: Map<Int, Double>? = null
) {
    val capacityCollection: Double
        get() = totalSlots * entryFee

    val actualCollection: Double
        get() = joinedPlayersCount * entryFee

    val effectiveCollection: Double
        get() = if (isRegistrationClosed) actualCollection else capacityCollection

    val rank1Reward: Double
        get() = kotlin.math.round(effectiveCollection * 0.40).coerceAtLeast(entryFee * 4.0)

    val rank2Reward: Double
        get() = kotlin.math.round(effectiveCollection * 0.20).coerceAtLeast(entryFee * 2.0)

    val rank3Reward: Double
        get() = kotlin.math.round(effectiveCollection * 0.10).coerceAtLeast(entryFee * 1.0)

    val booyahReward: Double
        get() = rank1Reward

    val highKillPool: Double
        get() = kotlin.math.round(effectiveCollection * 0.30)

    val hostMargin: Double
        get() = kotlin.math.round(effectiveCollection * 0.30)

    val dynamicPrizePool: Double
        get() = customPrizePool ?: (rank1Reward + rank2Reward + rank3Reward)

    val totalPrizePool: Double
        get() = if (isSurvivalMode) {
            if (customRankPrizes != null && customRankPrizes.isNotEmpty()) {
                customRankPrizes.values.sum()
            } else {
                survivalPrizePool
            }
        } else {
            dynamicPrizePool
        }

    /**
     * Survival Mode: Exact guaranteed prize distribution for Top 10 finishers.
     * Checks customRankPrizes configured by admin first; falls back to standard percentage distribution.
     */
    fun getSurvivalPrizeForRank(rankNumber: Int): Double {
        if (customRankPrizes != null) {
            return customRankPrizes[rankNumber] ?: 0.0
        }
        val pool = survivalPrizePool
        return when (rankNumber) {
            1 -> kotlin.math.round(pool * 0.35) // 35% (e.g. ₹350)
            2 -> kotlin.math.round(pool * 0.20) // 20% (e.g. ₹200)
            3 -> kotlin.math.round(pool * 0.12) // 12% (e.g. ₹120)
            4 -> kotlin.math.round(pool * 0.08) // 8%  (e.g. ₹80)
            5 -> kotlin.math.round(pool * 0.06) // 6%  (e.g. ₹60)
            6 -> kotlin.math.round(pool * 0.05) // 5%  (e.g. ₹50)
            7 -> kotlin.math.round(pool * 0.04) // 4%  (e.g. ₹40)
            8 -> kotlin.math.round(pool * 0.04) // 4%  (e.g. ₹40)
            9 -> kotlin.math.round(pool * 0.03) // 3%  (e.g. ₹30)
            10 -> kotlin.math.round(pool * 0.03) // 3%  (e.g. ₹30)
            else -> 0.0
        }
    }

    /**
     * Returns the configured ranking payout positions for display in UI.
     * If admin deleted or configured specific ranks, only those ranks are shown.
     */
    fun getSurvivalTop10PrizeTable(): List<Pair<Int, Double>> {
        if (customRankPrizes != null && customRankPrizes.isNotEmpty()) {
            return customRankPrizes.entries.sortedBy { it.key }.map { Pair(it.key, it.value) }
        }
        return (1..10).map { rank ->
            Pair(rank, getSurvivalPrizeForRank(rank))
        }
    }

    /**
     * Real entered participants roster for this custom room.
     * Contains only real joined players or admin-declared participant results (no demo/filler players).
     */
    val defaultEnteredParticipants: List<ParticipantRecord>
        get() {
            if (customParticipants != null && customParticipants.isNotEmpty()) {
                return customParticipants
            }

            return enrolledPlayers.map { player ->
                ParticipantRecord(
                    uid = player.uid,
                    name = player.ign.ifBlank { "Player" },
                    uidNumber = player.uidNumber,
                    kills = userKillCount
                )
            }
        }

    /**
     * Ranking generator:
     * - Before the match starts: lists all entered participants with 'Rank 1', their name, 0 kills, and 0 winnings.
     * - Upon match completion:
     *   * For Survival Mode: Top 10 survivors receive their share of the guaranteed Prize Pool.
     *   * For Per Kill Arena: dynamically ranks players by highest kills and kill bounties.
     */
    fun getParticipantRankings(): List<ParticipantRanking> {
        val allParticipants = defaultEnteredParticipants

        return if (status == MatchStatus.COMPLETED) {
            val sortedByKills = allParticipants.sortedWith(
                compareByDescending<ParticipantRecord> { it.kills }
                    .thenBy { it.name }
            )

            if (isSurvivalMode) {
                // Survival Mode: Top 10 finishers share the prize pool
                sortedByKills.mapIndexed { index, participant ->
                    val finalRankNumber = index + 1
                    val survivalPrize = getSurvivalPrizeForRank(finalRankNumber)

                    ParticipantRanking(
                        rank = "Rank $finalRankNumber",
                        rankNumber = finalRankNumber,
                        name = participant.name,
                        kills = participant.kills,
                        earnedAmount = survivalPrize
                    )
                }
            } else {
                // Per Kill Arena mode
                sortedByKills.mapIndexed { index, participant ->
                    val finalRankNumber = index + 1
                    val rankBonus = when (finalRankNumber) {
                        1 -> perKillReward * 5.0
                        2 -> perKillReward * 2.5
                        3 -> perKillReward * 1.5
                        else -> 0.0
                    }
                    val totalEarned = (participant.kills * perKillReward) + rankBonus

                    ParticipantRanking(
                        rank = "Rank $finalRankNumber",
                        rankNumber = finalRankNumber,
                        name = participant.name,
                        kills = participant.kills,
                        earnedAmount = totalEarned
                    )
                }
            }
        } else {
            // Before match start: all entered participants listed with 'Rank 1', their name, 0 kills, and 0 winnings
            allParticipants.map { participant ->
                ParticipantRanking(
                    rank = "Rank 1",
                    rankNumber = 1,
                    name = participant.name,
                    kills = 0,
                    earnedAmount = 0.0
                )
            }
        }
    }

    // Backwards compatibility helpers
    val rankStandings: List<ParticipantRanking>
        get() = getParticipantRankings()

    val winningSquadMembers: List<ParticipantRanking>
        get() = getParticipantRankings()

    val participatingPlayers: List<MatchPlayer>
        get() = defaultEnteredParticipants.map {
            MatchPlayer(uid = it.uid, ign = it.name, uidNumber = it.uidNumber)
        }

    val participatingSquadMembers: List<MatchPlayer>
        get() = participatingPlayers

    val isRegistrationClosed: Boolean
        get() = System.currentTimeMillis() >= startTimeMillis || status != MatchStatus.UPCOMING || remainingSlots <= 0

    val millisUntilStart: Long
        get() = startTimeMillis - System.currentTimeMillis()

    val isSevenMinuteActivePhase: Boolean
        get() = millisUntilStart <= 7 * 60 * 1000L || status == MatchStatus.ONGOING

    val isRoomCredentialsUnlocked: Boolean
        get() = isSevenMinuteActivePhase

    val remainingSlots: Int
        get() = (totalSlots - joinedPlayersCount).coerceAtLeast(0)

    val progressFraction: Float
        get() = if (totalSlots > 0) (joinedPlayersCount.toFloat() / totalSlots.toFloat()).coerceIn(0f, 1f) else 0f

    /**
     * Formats date & time strictly using Indian Standard Time (Asia/Kolkata).
     * Displays "Today (11 Nov), 06:30 PM", "Tomorrow (12 Nov), 06:30 PM", or "11 Nov 2026, 06:30 PM".
     */
    fun formattedDateTime(): String {
        val istZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
        val matchCal = java.util.Calendar.getInstance(istZone).apply { timeInMillis = startTimeMillis }
        val nowCal = java.util.Calendar.getInstance(istZone)

        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).apply {
            timeZone = istZone
        }
        val timeStr = timeFormat.format(java.util.Date(startTimeMillis))

        val isSameDay = matchCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                matchCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)

        val isTomorrow = matchCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                matchCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR) + 1

        return when {
            isSameDay -> {
                val dayOfMonth = matchCal.get(java.util.Calendar.DAY_OF_MONTH)
                val monthFormat = java.text.SimpleDateFormat("MMM", java.util.Locale.ENGLISH).apply {
                    timeZone = istZone
                }
                val monthStr = monthFormat.format(java.util.Date(startTimeMillis))
                "Today ($dayOfMonth $monthStr), $timeStr"
            }
            isTomorrow -> {
                val dayOfMonth = matchCal.get(java.util.Calendar.DAY_OF_MONTH)
                val monthFormat = java.text.SimpleDateFormat("MMM", java.util.Locale.ENGLISH).apply {
                    timeZone = istZone
                }
                val monthStr = monthFormat.format(java.util.Date(startTimeMillis))
                "Tomorrow ($dayOfMonth $monthStr), $timeStr"
            }
            else -> {
                val fullFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH).apply {
                    timeZone = istZone
                }
                fullFormat.format(java.util.Date(startTimeMillis))
            }
        }
    }

    /**
     * Formats remaining time in minus format: "-MM:SS" or "-HH:MM:SS"
     */
    fun formatMinusCountdown(currentTimeMs: Long = System.currentTimeMillis()): String {
        val remaining = (startTimeMillis - currentTimeMs).coerceAtLeast(0L)
        val totalSeconds = remaining / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("-%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("-%02d:%02d", minutes, seconds)
        }
    }
}
