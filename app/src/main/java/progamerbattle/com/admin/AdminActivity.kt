package progamerbattle.com.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import progamerbattle.com.data.MatchRepository
import progamerbattle.com.data.WalletRepository
import progamerbattle.com.ui.theme.ProGamerBattleTheme

class AdminActivity : ComponentActivity() {

    private val adminViewModel by lazy {
        AdminViewModel(
            matchRepository = MatchRepository.getInstance(),
            walletRepository = WalletRepository.getInstance(),
            userRegistry = progamerbattle.com.data.UserRegistry.getInstance(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProGamerBattleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdminMainScreen(adminViewModel = adminViewModel)
                }
            }
        }
    }
}
