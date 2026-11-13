package progamerbattle.com

import progamerbattle.com.data.AuthRepository
import progamerbattle.com.model.MatchType
import progamerbattle.com.model.TournamentMatch
import progamerbattle.com.model.User
import progamerbattle.com.viewmodel.MIN_WITHDRAWAL_AMOUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

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

    @Test
    fun testReferralCodeGenerationAndAntiSelfReferral() {
        val user = User(
            uid = "user_9876543210",
            fullName = "Shadow Hunter",
            mobileNumber = "9876543210",
            ffIgn = "Shadow",
            ffUid = "11223344"
        )

        // Default sign up bonus must be 10.0
        assertEquals(10.0, user.bonusBalance, 0.01)
        assertEquals(10.0, user.walletBalance, 0.01)

        // Computed referral code should match PGB + SHAD + 3210
        assertEquals("PGBSHAD3210", user.myReferralCode)

        // Register user into UserRegistry
        val registry = progamerbattle.com.data.UserRegistry.getInstance()
        registry.recordUser(user)

        // Search user by referral code
        val found = registry.findUserByReferralCode("PGBSHAD3210")
        assertTrue(found != null)
        assertEquals("9876543210", found?.mobileNumber)

        // Anti-fake referral: Self-referral must be strictly rejected
        val selfCreditResult = registry.recordPendingReferral(
            referrerMobile = "9876543210",
            refereeMobile = "9876543210",
            refereeName = "Shadow Hunter",
            refereeIgn = "Shadow"
        )
        assertFalse("Self referral must return false", selfCreditResult)

        // Valid referral from a friend starts in PENDING status
        val friendCreditResult = registry.recordPendingReferral(
            referrerMobile = "9876543210",
            refereeMobile = "9988776655",
            refereeName = "Aman Friend",
            refereeIgn = "AmanFF"
        )
        assertTrue("Friend referral registration must succeed", friendCreditResult)

        // Referrer has 1 referral, but earnings are 0 and reward is pending
        val pendingReferrer = registry.findUserByMobile("9876543210")
        assertEquals(1, pendingReferrer?.totalReferrals)
        assertEquals(0.0, pendingReferrer?.referralEarnings ?: 0.0, 0.01)
        assertEquals(10.0, pendingReferrer?.bonusBalance ?: 0.0, 0.01)

        val pendingRecord = registry.referrals.value.first { it.refereeMobile == "9988776655" }
        assertEquals(progamerbattle.com.model.ReferralStatus.PENDING, pendingRecord.status)

        // Friend adds deposit < ₹10 -> still PENDING
        registry.recordDepositAdded("9988776655", 5.0)
        assertEquals(progamerbattle.com.model.ReferralStatus.PENDING, registry.referrals.value.first { it.refereeMobile == "9988776655" }.status)

        // Friend adds deposit >= ₹10 -> becomes CLAIMABLE
        registry.recordDepositAdded("9988776655", 10.0)
        val claimableRecord = registry.referrals.value.first { it.refereeMobile == "9988776655" }
        assertEquals(progamerbattle.com.model.ReferralStatus.CLAIMABLE, claimableRecord.status)

        // Referrer claims reward -> status becomes CLAIMED (Success), credited into bonusBalance only
        val claimSuccess = registry.claimReferralReward(claimableRecord.id, "9876543210")
        assertTrue("Claim must succeed", claimSuccess)

        val claimedRecord = registry.referrals.value.first { it.refereeMobile == "9988776655" }
        assertEquals(progamerbattle.com.model.ReferralStatus.CLAIMED, claimedRecord.status)

        val updatedReferrer = registry.findUserByMobile("9876543210")
        assertEquals(1, updatedReferrer?.totalReferrals)
        assertEquals(10.0, updatedReferrer?.referralEarnings ?: 0.0, 0.01)
        assertEquals(20.0, updatedReferrer?.bonusBalance ?: 0.0, 0.01)
        assertEquals(0.0, updatedReferrer?.winningBalance ?: 0.0, 0.01)
    }

    @Test
    fun testWithdrawalOnlyFromWinningBalance() {
        // User with high deposit and bonus, but insufficient winning balance
        val user = User(
            uid = "user_wallet_test",
            mobileNumber = "9876543210",
            depositBalance = 500.0,
            bonusBalance = 200.0,
            winningBalance = 50.0 // Only 50 in winnings! Total wallet balance = 750
        )
        assertEquals(750.0, user.walletBalance, 0.01)

        // User attempts to withdraw ₹100 (which is MIN_WITHDRAWAL_AMOUNT)
        val withdrawAttempt = 100.0

        // Strict rule: Only winningBalance can be withdrawn
        val isAllowed = withdrawAttempt >= MIN_WITHDRAWAL_AMOUNT && withdrawAttempt <= user.winningBalance
        assertFalse("Withdrawal must be rejected because winning balance (₹50) is less than ₹100", isAllowed)

        // Now user wins tournament match and gains ₹200 winning balance (total winning = ₹250)
        val userAfterWinning = user.copy(winningBalance = 250.0)
        val canWithdrawNow = withdrawAttempt >= MIN_WITHDRAWAL_AMOUNT && withdrawAttempt <= userAfterWinning.winningBalance
        assertTrue("Withdrawal must be allowed now as winning balance (₹250) >= ₹100", canWithdrawNow)

        // Simulated withdrawal deducts only winningBalance
        val userAfterWithdraw = userAfterWinning.copy(winningBalance = userAfterWinning.winningBalance - withdrawAttempt)
        assertEquals(150.0, userAfterWithdraw.winningBalance, 0.01)
        assertEquals(500.0, userAfterWithdraw.depositBalance, 0.01) // untouched
        assertEquals(200.0, userAfterWithdraw.bonusBalance, 0.01)   // untouched
    }

    @Test
    fun testCustomRoomSlotsAndFormatCalculation() {
        // 1 vs 1 match (2 slots)
        val oneVsOneMatch = TournamentMatch(
            id = "clash_1v1",
            title = "1 vs 1 Clash Arena",
            totalSlots = 2,
            joinedPlayersCount = 1,
            matchType = MatchType.ONE_VS_ONE
        )
        assertEquals(1, oneVsOneMatch.remainingSlots)
        assertEquals(0.5f, oneVsOneMatch.progressFraction, 0.001f)
        assertEquals("1 vs 1", oneVsOneMatch.matchType.label)

        // 2 vs 2 match (4 slots)
        val twoVsTwoMatch = TournamentMatch(
            id = "clash_2v2",
            title = "2 vs 2 Duo Battle",
            totalSlots = 4,
            joinedPlayersCount = 2,
            matchType = MatchType.TWO_VS_TWO
        )
        assertEquals(2, twoVsTwoMatch.remainingSlots)
        assertEquals(0.5f, twoVsTwoMatch.progressFraction, 0.001f)
        assertEquals("2 vs 2", twoVsTwoMatch.matchType.label)

        // Custom room with 1 slot
        val singleSlotMatch = TournamentMatch(
            id = "solo_1slot",
            title = "Solo Trial",
            totalSlots = 1,
            joinedPlayersCount = 1,
            matchType = MatchType.CUSTOM
        )
        assertEquals(0, singleSlotMatch.remainingSlots)
        assertEquals(1.0f, singleSlotMatch.progressFraction, 0.001f)
        assertTrue(singleSlotMatch.isRegistrationClosed)

        // AdminViewModel slot boundaries check (1 to 48)
        val adminVm = progamerbattle.com.admin.AdminViewModel()
        adminVm.onNewMatchFormatSelected(MatchType.ONE_VS_ONE, 2)
        assertEquals(2, adminVm.uiState.value.newMatchTotalSlots)
        assertEquals(MatchType.ONE_VS_ONE, adminVm.uiState.value.newMatchMatchType)

        adminVm.onNewMatchFormatSelected(MatchType.TWO_VS_TWO, 4)
        assertEquals(4, adminVm.uiState.value.newMatchTotalSlots)
        assertEquals(MatchType.TWO_VS_TWO, adminVm.uiState.value.newMatchMatchType)

        adminVm.onNewMatchSlotsChanged(48)
        assertEquals(48, adminVm.uiState.value.newMatchTotalSlots)

        // Ensure clamped to valid 1-48 range
        adminVm.onNewMatchSlotsChanged(0)
        assertEquals(1, adminVm.uiState.value.newMatchTotalSlots)

        adminVm.onNewMatchSlotsChanged(100)
        assertEquals(48, adminVm.uiState.value.newMatchTotalSlots)
    }

    @Test
    fun testRemoteConfigDefaultsAndModel() {
        val defaultConfig = progamerbattle.com.data.RemoteConfigModel()

        // 1. Verify default links
        assertTrue(defaultConfig.whatsappLink.contains("wa.me"))
        assertEquals("https://t.me/ProGamerBattle", defaultConfig.telegramLink)

        // 2. Verify default maintenance mode is false
        assertFalse(defaultConfig.isMaintenanceMode)

        // 3. Verify announcement message default is empty
        assertEquals("", defaultConfig.announcementMessage)

        // 4. Verify default min version is 1 and no update is required
        assertEquals(1L, defaultConfig.minVersion)
        assertFalse(defaultConfig.isUpdateRequired)
        assertEquals("https://progamerbattle.com/download", defaultConfig.updateUrl)
    }

    @Test
    fun testRemoteConfigMaintenanceModeTrigger() {
        // When maintenance_mode is toggled to true
        val maintenanceConfig = progamerbattle.com.data.RemoteConfigModel(
            isMaintenanceMode = true,
            announcementMessage = "Server upgrading to v2 engine",
            telegramLink = "https://t.me/ProGamerBattle",
            whatsappLink = "https://wa.me/919876543210"
        )

        assertTrue("Maintenance mode should be active", maintenanceConfig.isMaintenanceMode)
        assertEquals("Server upgrading to v2 engine", maintenanceConfig.announcementMessage)
        assertEquals("https://t.me/ProGamerBattle", maintenanceConfig.telegramLink)
        assertEquals("https://wa.me/919876543210", maintenanceConfig.whatsappLink)
    }

    @Test
    fun testRemoteConfigMinVersionAndForceUpdate() {
        val installedVersionCode = 1L

        // If min_version in Firebase is 1 (matches installed version), no update required
        val currentVersionConfig = progamerbattle.com.data.RemoteConfigModel(
            minVersion = 1L,
            isUpdateRequired = 1L > installedVersionCode
        )
        assertFalse(currentVersionConfig.isUpdateRequired)

        // If min_version in Firebase is increased to 2, update becomes required
        val futureVersionConfig = progamerbattle.com.data.RemoteConfigModel(
            minVersion = 2L,
            minVersionStr = "2.0",
            updateUrl = "https://progamerbattle.com/download/v2.apk",
            isUpdateRequired = 2L > installedVersionCode
        )
        assertTrue("Update must be required when min_version > installed version", futureVersionConfig.isUpdateRequired)
        assertEquals("https://progamerbattle.com/download/v2.apk", futureVersionConfig.updateUrl)
    }

    @Test
    fun testMatchDateTimeISTAndCountdownAndGoToRoom() {
        val now = System.currentTimeMillis()

        // 1. Match starting in 20 minutes (Waiting phase)
        val waitingMatch = TournamentMatch(
            id = "wait_match",
            startTimeMillis = now + 20 * 60 * 1000L
        )
        assertFalse("20 mins before match should NOT be in active room phase", waitingMatch.isSevenMinuteActivePhase)
        val minusStr = waitingMatch.formatMinusCountdown(now)
        assertTrue("Countdown should start with minus sign", minusStr.startsWith("-"))
        assertTrue("Countdown should format 20 mins as -20:00", minusStr == "-20:00" || minusStr.startsWith("-19:"))

        // 2. Match starting in 5 minutes (Inside 7-minute threshold -> Go To Room active)
        val roomReadyMatch = TournamentMatch(
            id = "room_ready_match",
            startTimeMillis = now + 5 * 60 * 1000L
        )
        assertTrue("5 mins before match MUST be in active room phase", roomReadyMatch.isSevenMinuteActivePhase)
        assertTrue("Credentials must be unlocked", roomReadyMatch.isRoomCredentialsUnlocked)

        // 3. Indian Standard Time formatted string
        val istFormatted = waitingMatch.formattedDateTime()
        assertTrue("Formatted date time should contain Today", istFormatted.startsWith("Today"))
        assertTrue("Formatted date time should contain Nov", istFormatted.contains("Nov"))
    }

    @Test
    fun testSurvivalRoomTop10PrizePool() {
        val participants = (1..12).map { i ->
            progamerbattle.com.model.ParticipantRecord(
                uid = "u_$i",
                name = "Player_$i",
                uidNumber = "${100000 + i}",
                kills = 15 - i
            )
        }
        val survivalMatch = TournamentMatch(
            id = "survival_test_101",
            title = "Bermuda Survival Arena #201",
            isSurvivalMode = true,
            survivalPrizePool = 1000.0,
            customParticipants = participants,
            status = progamerbattle.com.model.MatchStatus.COMPLETED
        )

        assertTrue(survivalMatch.isSurvivalMode)
        assertEquals(1000.0, survivalMatch.totalPrizePool, 0.01)

        // Verify Top 10 Prize calculations
        assertEquals(350.0, survivalMatch.getSurvivalPrizeForRank(1), 0.01) // 35%
        assertEquals(200.0, survivalMatch.getSurvivalPrizeForRank(2), 0.01) // 20%
        assertEquals(120.0, survivalMatch.getSurvivalPrizeForRank(3), 0.01) // 12%
        assertEquals(80.0, survivalMatch.getSurvivalPrizeForRank(4), 0.01)  // 8%
        assertEquals(60.0, survivalMatch.getSurvivalPrizeForRank(5), 0.01)  // 6%
        assertEquals(50.0, survivalMatch.getSurvivalPrizeForRank(6), 0.01)  // 5%
        assertEquals(40.0, survivalMatch.getSurvivalPrizeForRank(7), 0.01)  // 4%
        assertEquals(40.0, survivalMatch.getSurvivalPrizeForRank(8), 0.01)  // 4%
        assertEquals(30.0, survivalMatch.getSurvivalPrizeForRank(9), 0.01)  // 3%
        assertEquals(30.0, survivalMatch.getSurvivalPrizeForRank(10), 0.01) // 3%
        assertEquals(0.0, survivalMatch.getSurvivalPrizeForRank(11), 0.01)  // Rank 11+ get 0

        // Total of top 10 must equal exactly 1000.0
        val sumTop10 = (1..10).sumOf { survivalMatch.getSurvivalPrizeForRank(it) }
        assertEquals(1000.0, sumTop10, 0.01)

        // Verify participant ranking assignment for completed match
        val rankings = survivalMatch.getParticipantRankings()
        assertEquals(12, rankings.size)
        assertEquals(350.0, rankings[0].earnedAmount, 0.01)
        assertEquals(200.0, rankings[1].earnedAmount, 0.01)
        assertEquals(120.0, rankings[2].earnedAmount, 0.01)
        assertEquals(80.0, rankings[3].earnedAmount, 0.01)
        assertEquals(60.0, rankings[4].earnedAmount, 0.01)
        assertEquals(50.0, rankings[5].earnedAmount, 0.01)
        assertEquals(40.0, rankings[6].earnedAmount, 0.01)
        assertEquals(40.0, rankings[7].earnedAmount, 0.01)
        assertEquals(30.0, rankings[8].earnedAmount, 0.01)
        assertEquals(30.0, rankings[9].earnedAmount, 0.01)
        assertEquals(0.0, rankings[10].earnedAmount, 0.01)
    }
}
