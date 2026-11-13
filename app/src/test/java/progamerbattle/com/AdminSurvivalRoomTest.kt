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
import progamerbattle.com.admin.AdminViewModel
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.UserRegistry
import progamerbattle.com.data.WalletRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AdminSurvivalRoomTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var matchRepo: MatchRepository
    private lateinit var walletRepo: WalletRepository
    private lateinit var userRegistry: UserRegistry
    private lateinit var adminViewModel: AdminViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        matchRepo = MatchRepository()
        walletRepo = WalletRepository()
        userRegistry = UserRegistry.getInstance(null)
        adminViewModel = AdminViewModel(
            matchRepository = matchRepo,
            walletRepository = walletRepo,
            userRegistry = userRegistry
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
        assertEquals(350.0, state.newMatchRankPrizes[1] ?: 0.0, 0.01)
        assertEquals(200.0, state.newMatchRankPrizes[2] ?: 0.0, 0.01)
    }

    @Test
    fun testManualPrizePoolInputUpdatesTotalAndRanks() {
        adminViewModel.openCreateSurvivalMatchDialog()

        adminViewModel.onPrizePoolInputChanged("2500")
        var state = adminViewModel.uiState.value

        assertEquals(2500.0, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals("2500", state.newMatchPrizePoolInput)
        assertEquals(875.0, state.newMatchRankPrizes[1] ?: 0.0, 0.01)
        assertEquals(500.0, state.newMatchRankPrizes[2] ?: 0.0, 0.01)
        assertEquals(2500.0, state.newMatchRankPrizes.values.sum(), 0.01)

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

        val rank10Prize = adminViewModel.uiState.value.newMatchRankPrizes[10] ?: 0.0
        adminViewModel.onDeleteRankPrize(10)
        var state = adminViewModel.uiState.value

        assertFalse(state.newMatchRankPrizes.containsKey(10))
        assertEquals(9, state.newMatchRankPrizes.size)
        assertEquals(1000.0 - rank10Prize, state.newMatchSurvivalPrizePool, 0.01)
        assertEquals((1000.0 - rank10Prize).toInt().toString(), state.newMatchPrizePoolInput)

        adminViewModel.onAddRankPrize(10, 50.0)
        state = adminViewModel.uiState.value
        assertTrue(state.newMatchRankPrizes.containsKey(10))
        assertEquals(50.0, state.newMatchRankPrizes[10] ?: 0.0, 0.01)
    }

    @Test
    fun testSwitchingBetweenSurvivalAndPerKillMode() {
        adminViewModel.openCreateSurvivalMatchDialog()
        assertTrue(adminViewModel.uiState.value.newMatchIsSurvival)

        adminViewModel.onNewMatchIsSurvivalChanged(false)
        assertFalse(adminViewModel.uiState.value.newMatchIsSurvival)

        adminViewModel.onNewMatchIsSurvivalChanged(true)
        assertTrue(adminViewModel.uiState.value.newMatchIsSurvival)
    }
}
