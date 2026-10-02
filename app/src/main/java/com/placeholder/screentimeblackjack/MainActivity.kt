package com.placeholder.screentimeblackjack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.placeholder.screentimeblackjack.ui.GameScreen
import com.placeholder.screentimeblackjack.ui.GameViewModel
import com.placeholder.screentimeblackjack.ui.theme.CasinoGreenDeep
import com.placeholder.screentimeblackjack.ui.theme.ScreenTimeBlackjackTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleBlockedAppIntent(intent)
        setContent {
            ScreenTimeBlackjackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CasinoGreenDeep
                ) {
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
        viewModel.refreshBalanceFromDb()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBlockedAppIntent(intent)
        viewModel.refreshBalanceFromDb()
    }

    private fun handleBlockedAppIntent(intent: android.content.Intent?) {
        val blockedPkg = intent?.getStringExtra(com.placeholder.screentimeblackjack.service.AppBlockerAccessibilityService.EXTRA_BLOCKED_APP_TRIGGERED)
        if (blockedPkg != null) {
            viewModel.setBlockedAppAlert(blockedPkg)
        }
    }
}
