package progamerbattle.com

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.model.User
import progamerbattle.com.viewmodel.MIN_WITHDRAWAL_AMOUNT

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMobileToInternalEmailMapping() {
        val repo = AuthRepository()
        val email = repo.mapMobileToInternalEmail("9876543210")
        assertEquals("9876543210@progamerbattle.internal", email)

        val emailWithCountryCode = repo.mapMobileToInternalEmail("+91 98765 43210")
        assertEquals("9876543210@progamerbattle.internal", emailWithCountryCode)
    }

    @Test
    fun testTournamentMatchSlotsCalculation() {
        val match = TournamentMatch(
            id = "test_match",
            title = "Bermuda Solo Test",
            totalSlots = 48,
            joinedPlayersCount = 40,
            matchType = MatchType.SOLO
        )
        assertEquals(8, match.remainingSlots)
        assertEquals(40f / 48f, match.progressFraction, 0.001f)
    }

    @Test
    fun testUserModelBalances() {
        val user = User(
            uid = "test-uid",
            fullName = "Gamer Test",
            mobileNumber = "9998887770",
            ffIgn = "ProShooter",
            ffUid = "12345678",
            secretPin = "654321",
            depositBalance = 200.0,
            winningBalance = 750.0,
            bonusBalance = 150.0
        )
        assertEquals(1100.0, user.walletBalance, 0.01)
        assertEquals("ProShooter", user.inGameName)
        assertEquals("12345678", user.inGameUid)
    }

    @Test
    fun testWithdrawalThresholds() {
        assertEquals(100.0, MIN_WITHDRAWAL_AMOUNT, 0.001)

        val user = User(
            winningBalance = 650.0,
            depositBalance = 100.0,
            bonusBalance = 10.0
        )

        // Below 100 should be rejected
        val requestBelowMin = 50.0
        assertTrue(requestBelowMin < MIN_WITHDRAWAL_AMOUNT)

        // Valid withdrawal: between 100 and user's winningBalance
        val validRequest = 100.0
        assertTrue(validRequest >= MIN_WITHDRAWAL_AMOUNT && validRequest <= user.winningBalance)

        // Exceeding winning balance should be rejected
        val requestExceedingWinnings = 700.0
        assertTrue(requestExceedingWinnings > user.winningBalance)
    }

    @Test
    fun testUserStatsDefaultToZero() {
        val newUser = User(
            uid = "new-user",
            mobileNumber = "9988776655"
        )
        assertEquals(0, newUser.matchesPlayed)
        assertEquals(0, newUser.matchesWon)
        assertEquals(0, newUser.totalKills)
    }

    @Test
    fun testCleanStartAndDynamicRankingWithRealPlayers() {
        // Production install starts with 0 demo matches
        val repo = progamerbattle.com.data.MatchRepository()
        assertTrue("MatchRepository must start empty with zero demo matches", repo.matches.value.isEmpty())

        // Match with zero enrolled players has zero demo participants
        val emptyMatch = TournamentMatch(
            id = "real_room_1",
            title = "Bermuda Solo Real #1",
            entryFee = 30.0,
            perKillReward = 20.0,
            totalSlots = 48
        )
        assertTrue(emptyMatch.defaultEnteredParticipants.isEmpty())
        assertTrue(emptyMatch.getParticipantRankings().isEmpty())

        // Create real match with real enrolled players
        val realPlayers = listOf(
            progamerbattle.com.model.MatchPlayer(uid = "u1", ign = "PlayerAlpha", uidNumber = "1001"),
            progamerbattle.com.model.MatchPlayer(uid = "u2", ign = "PlayerBravo", uidNumber = "1002")
        )
        val upcomingMatch = emptyMatch.copy(
            joinedPlayersCount = 2,
            joinedPlayerUids = listOf("u1", "u2"),
            enrolledPlayers = realPlayers,
            status = progamerbattle.com.model.MatchStatus.UPCOMING
        )
        repo.createMatch(upcomingMatch)

        val preMatchRankings = upcomingMatch.getParticipantRankings()
        assertEquals(2, preMatchRankings.size)
        preMatchRankings.forEach { ranking ->
            assertEquals("Rank 1", ranking.rank)
            assertEquals(1, ranking.rankNumber)
            assertEquals(0, ranking.kills)
            assertEquals(0.0, ranking.earnedAmount, 0.001)
        }

        // Completed match with declared kills ranks descending by kills
        repo.declareMatchResults(
            matchId = upcomingMatch.id,
            winnerName = "PlayerAlpha",
            participants = listOf(
                progamerbattle.com.model.ParticipantRecord(uid = "u1", name = "PlayerAlpha", uidNumber = "1001", kills = 5),
                progamerbattle.com.model.ParticipantRecord(uid = "u2", name = "PlayerBravo", uidNumber = "1002", kills = 2)
            )
        )
        val completedMatch = repo.matches.value.first()
        val postMatchRankings = completedMatch.getParticipantRankings()
        assertEquals(2, postMatchRankings.size)
        assertEquals("PlayerAlpha", postMatchRankings[0].name)
        assertEquals(5, postMatchRankings[0].kills)
        assertTrue(postMatchRankings[0].earnedAmount > postMatchRankings[1].earnedAmount)
    }

    @Test
    fun testRegistrationLockLogic() {
        // Match in future with slots available -> Open
        val openMatch = TournamentMatch(
            id = "open_match",
            startTimeMillis = System.currentTimeMillis() + 600000L,
            totalSlots = 48,
            joinedPlayersCount = 20
        )
        assertFalse(openMatch.isRegistrationClosed)

        // Match past start time -> Locked
        val expiredMatch = TournamentMatch(
            id = "expired_match",
            startTimeMillis = System.currentTimeMillis() - 5000L,
            totalSlots = 48,
            joinedPlayersCount = 20
        )
        assertTrue(expiredMatch.isRegistrationClosed)

        // Match with 0 slots remaining -> Locked
        val fullMatch = TournamentMatch(
            id = "full_match",
            startTimeMillis = System.currentTimeMillis() + 600000L,
            totalSlots = 48,
            joinedPlayersCount = 48
        )
        assertTrue(fullMatch.isRegistrationClosed)
    }

    @Test
    fun testSevenMinuteActivePhaseThreshold() {
        // 15 minutes before start -> Locked phase (waiting for entry room)
        val lockedPhaseMatch = TournamentMatch(
            id = "locked_phase",
            startTimeMillis = System.currentTimeMillis() + 15 * 60 * 1000L
        )
        assertFalse(lockedPhaseMatch.isSevenMinuteActivePhase)

        // 5 minutes before start -> Active phase (play & accept room request)
        val activePhaseMatch = TournamentMatch(
            id = "active_phase",
            startTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000L
        )
        assertTrue(activePhaseMatch.isSevenMinuteActivePhase)
    }

    @Test
    fun testUpcomingTournamentScheduleAndJoin() {
        val repo = progamerbattle.com.data.MatchRepository()
        assertTrue(repo.matches.value.isEmpty())

        val createdMatch = TournamentMatch(
            id = "live_room_101",
            title = "Bermuda Custom Room #1",
            startTimeMillis = System.currentTimeMillis() + 30 * 60 * 1000L,
            entryFee = 30.0,
            perKillReward = 20.0,
            totalSlots = 48,
            joinedPlayersCount = 0
        )
        repo.createMatch(createdMatch)

        val player = progamerbattle.com.model.MatchPlayer(
            uid = "test-player-1",
            ign = "HeroGamer",
            uidNumber = "12345678",
            mobile = "9876543210"
        )
        val joined = repo.joinMatch(createdMatch.id, player)
        assertTrue(joined)

        val updated = repo.matches.value.first { it.id == createdMatch.id }
        assertEquals(1, updated.joinedPlayersCount)
        assertTrue(updated.joinedPlayerUids.contains("test-player-1"))
    }
}
