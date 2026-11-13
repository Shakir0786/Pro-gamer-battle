package progamerbattle.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import progamerbattle.com.admin.AdminViewModel
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.UserRegistry
import progamerbattle.com.data.WalletRepository

class AdminSurvivalRoomTest {

    private lateinit var matchRepo: MatchRepository
    private lateinit var walletRepo: WalletRepository
    private lateinit var userRegistry: UserRegistry
    private lateinit var adminViewModel: AdminViewModel

    @Before
    fun setUp() {
        matchRepo = MatchRepository()
        walletRepo = WalletRepository()
        userRegistry = UserRegistry.getInstance(null)
        adminViewModel = AdminViewModel(
            matchRepository = matchRepo,
            walletRepository = walletRepo,
            userRegistry = userRegistry
        )
    }

    @Test
    fun testOpenCreateSurvivalMatchDialogSetsSurvivalDefaults() {
        adminViewModel.openCreateSurvivalMatchDialog()
        val state = adminViewModel.uiState.value

        assertTrue(state.isCreateMatchDialogOpen)
        assertTrue(state.newMatchIsSurvival)
        assertEquals(1000.0, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals("1000", state.newMatchPrizePoolInput)
        assertEquals(10, state.newMatchRankPrizes.size)
        assertEquals(1000.0, state.newMatchRankPrizes.values.sum(), 0.01)
        assertEquals(350.0, state.newMatchRankPrizes[1] ?: 0.0, 0.01) // Booyah is 35%
        assertEquals(200.0, state.newMatchRankPrizes[2] ?: 0.0, 0.01) // Rank 2 is 20%
    }

    @Test
    fun testManualPrizePoolInputUpdatesTotalAndRanks() {
        adminViewModel.openCreateSurvivalMatchDialog()

        // Admin types custom prize pool 2500
        adminViewModel.onPrizePoolInputChanged("2500")
        var state = adminViewModel.uiState.value

        assertEquals(2500.0, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals("2500", state.newMatchPrizePoolInput)
        assertEquals(875.0, state.newMatchRankPrizes[1] ?: 0.0, 0.01) // Booyah 35% of 2500 = 875
        assertEquals(500.0, state.newMatchRankPrizes[2] ?: 0.0, 0.01)  // Rank 2 20% of 2500 = 500
        assertEquals(2500.0, state.newMatchRankPrizes.values.sum(), 0.01)

        // Admin types custom prize pool 750
        adminViewModel.onPrizePoolInputChanged("750")
        state = adminViewModel.uiState.value

        assertEquals(750.0, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals("750", state.newMatchPrizePoolInput)
        assertEquals(750.0, state.newMatchRankPrizes.values.sum(), 0.01)
    }

    @Test
    fun testEditingRankPrizeUpdatesTotalPrizePoolInput() {
        adminViewModel.openCreateSurvivalMatchDialog()
        adminViewModel.onPrizePoolInputChanged("1000")

        // Booyah prize starts at 350. Admin increases it to 500 (+150)
        adminViewModel.onUpdateRankPrize(1, 500.0)
        val state = adminViewModel.uiState.value

        assertEquals(1150.0, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals("1150", state.newMatchPrizePoolInput)
        assertEquals(500.0, state.newMatchRankPrizes[1] ?: 0.0, 0.01)
    }

    @Test
    fun testDeletingRankUpdatesTotalPrizePoolInputAndAllowsReAdd() {
        adminViewModel.openCreateSurvivalMatchDialog()
        adminViewModel.onPrizePoolInputChanged("1000")

        // Admin deletes Rank 10 (which had 30.0)
        val rank10Prize = adminViewModel.uiState.value.newMatchRankPrizes[10] ?: 0.0
        adminViewModel.onDeleteRankPrize(10)
        var state = adminViewModel.uiState.value

        assertFalse(state.newMatchRankPrizes.containsKey(10))
        assertEquals(9, state.newMatchRankPrizes.size)
        assertEquals(1000.0 - rank10Prize, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals((1000.0 - rank10Prize).toInt().toString(), state.newMatchPrizePoolInput)

        // Admin adds back Rank 10 with custom prize
        adminViewModel.onAddRankPrize(10, 50.0)
        state = adminViewModel.uiState.value
        assertTrue(state.newMatchRankPrizes.containsKey(10))
        assertEquals(50.0, state.newMatchRankPrizes[10] ?: 0.0, 0.01)
    }

    @Test
    fun testSwitchingBetweenSurvivalAndPerKillMode() {
        adminViewModel.openCreateSurvivalMatchDialog()
        assertTrue(adminViewModel.uiState.value.newMatchIsSurvival)

        // Admin clicks Per Kill Arena
        adminViewModel.onNewMatchIsSurvivalChanged(false)
        assertFalse(adminViewModel.uiState.value.newMatchIsSurvival)

        // Admin switches back to Survival
        adminViewModel.onNewMatchIsSurvivalChanged(true)
        assertTrue(adminViewModel.uiState.value.newMatchIsSurvival)
    }
}
