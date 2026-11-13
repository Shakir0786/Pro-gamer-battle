package progamerbattle.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.ui.screens.MainScreen
import progamerbattle.com.ui.theme.CarbonBackground
import progamerbattle.com.ui.theme.ProGamerBattleTheme
import progamerbattle.com.viewmodel.AuthViewModel
import progamerbattle.com.viewmodel.MatchViewModel
import progamerbattle.com.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { AuthRepository(applicationContext) }
    private val matchRepository by lazy { MatchRepository.getInstance() }
    private val walletRepository by lazy { WalletRepository.getInstance() }

    private val authViewModel by lazy { AuthViewModel(authRepository) }
    private val matchViewModel by lazy { MatchViewModel(matchRepository, authRepository, walletRepository) }
    private val walletViewModel by lazy { WalletViewModel(walletRepository, authRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progamerbattle.com.data.UserRegistry.getInstance(applicationContext)
        progamerbattle.com.data.RemoteConfigManager.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            ProGamerBattleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        authViewModel = authViewModel,
                        matchViewModel = matchViewModel,
                        walletViewModel = walletViewModel
                    )
                }
            }
        }
    }
}
