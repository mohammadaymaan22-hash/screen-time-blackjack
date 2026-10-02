package com.placeholder.screentimeblackjack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.placeholder.screentimeblackjack.data.AppDatabase
import com.placeholder.screentimeblackjack.data.PlayerState
import com.placeholder.screentimeblackjack.engine.BlackjackEngine
import com.placeholder.screentimeblackjack.engine.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel bridging the BlackjackEngine and Room persistence to the UI.
 *
 * Exposes [gameState] and [timeBalance] as StateFlows for the Compose screen
 * to observe. Persists balance changes to Room on every mutation.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getInstance(application).playerStateDao()
    private val blockedAppDao = AppDatabase.getInstance(application).blockedAppDao()

    private var engine: BlackjackEngine? = null

    private val _gameState = MutableStateFlow<GameState>(GameState.Betting(0))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timeBalance = MutableStateFlow(0)
    val timeBalance: StateFlow<Int> = _timeBalance.asStateFlow()

    /** True once the initial balance has been loaded from Room. */
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /** Blocked apps list observed from database */
    val blockedApps: StateFlow<List<com.placeholder.screentimeblackjack.data.BlockedApp>> =
        blockedAppDao.getAllFlow().stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _blockedAppAlert = MutableStateFlow<String?>(null)
    val blockedAppAlert: StateFlow<String?> = _blockedAppAlert.asStateFlow()

    init {
        refreshPermissions()
        seedDefaultBlockedAppsIfEmpty()
        viewModelScope.launch {
            val saved = playerDao.get()
            val balance = saved?.timeBalance ?: 60

            engine = BlackjackEngine(initialBalance = balance)
            _timeBalance.value = balance
            _gameState.value = GameState.Betting(balance)
            _isReady.value = true
        }
    }

    fun refreshPermissions() {
        val app = getApplication<Application>()
        _isAccessibilityEnabled.value = com.placeholder.screentimeblackjack.util.PermissionHelper.isAccessibilityServiceEnabled(app)
        _isBatteryOptimizationIgnored.value = com.placeholder.screentimeblackjack.util.PermissionHelper.isBatteryOptimizationIgnored(app)
    }

    fun setBlockedAppAlert(pkg: String?) {
        _blockedAppAlert.value = pkg
    }

    fun clearBlockedAppAlert() {
        _blockedAppAlert.value = null
    }

    private fun seedDefaultBlockedAppsIfEmpty() {
        viewModelScope.launch {
            val existing = blockedAppDao.getActiveBlockedApps()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    com.placeholder.screentimeblackjack.data.BlockedApp("com.instagram.android", "Instagram"),
                    com.placeholder.screentimeblackjack.data.BlockedApp("com.zhiliaoapp.musically", "TikTok"),
                    com.placeholder.screentimeblackjack.data.BlockedApp("com.twitter.android", "X (Twitter)"),
                    com.placeholder.screentimeblackjack.data.BlockedApp("com.google.android.youtube", "YouTube")
                )
                defaults.forEach { blockedAppDao.insertOrUpdate(it) }
            }
        }
    }

    fun addBlockedApp(packageName: String, appName: String) {
        viewModelScope.launch {
            blockedAppDao.insertOrUpdate(
                com.placeholder.screentimeblackjack.data.BlockedApp(
                    packageName = packageName.trim(),
                    appName = appName.trim().ifEmpty { packageName.trim() },
                    isBlocked = true
                )
            )
        }
    }

    fun toggleAppBlocked(app: com.placeholder.screentimeblackjack.data.BlockedApp) {
        viewModelScope.launch {
            blockedAppDao.insertOrUpdate(app.copy(isBlocked = !app.isBlocked))
        }
    }

    fun removeBlockedApp(packageName: String) {
        viewModelScope.launch {
            blockedAppDao.deleteByPackage(packageName)
        }
    }

    /** Refresh balance when returning to foreground in case AccessibilityService modified it */
    fun refreshBalanceFromDb() {
        viewModelScope.launch {
            val saved = playerDao.get()
            val current = saved?.timeBalance ?: 60
            if (current != _timeBalance.value) {
                engine?.let { eng ->
                    if (current < eng.timeBalance) {
                        // Time was consumed in background
                        val diff = eng.timeBalance - current
                        eng.consumeTime(diff)
                    } else if (current > eng.timeBalance) {
                        eng.addTime(current - eng.timeBalance)
                    }
                    _gameState.value = eng.state
                    _timeBalance.value = eng.timeBalance
                }
            }
        }
    }

    fun placeBet(amount: Int) {
        val eng = engine ?: return
        eng.placeBet(amount)
        syncState(eng)
    }

    fun hit() {
        val eng = engine ?: return
        eng.hit()
        syncState(eng)
    }

    fun stand() {
        val eng = engine ?: return
        eng.stand()
        syncState(eng)
    }

    fun startNewHand() {
        val eng = engine ?: return
        eng.startNewHand()
        syncState(eng)
    }

    fun addTime(minutes: Int) {
        val eng = engine ?: return
        eng.addTime(minutes)
        syncState(eng)
    }

    fun resetBalance(amount: Int = 60) {
        val eng = engine ?: return
        eng.resetBalance(amount)
        syncState(eng)
    }

    /**
     * Sync the ViewModel's StateFlows from the engine and persist balance.
     */
    private fun syncState(eng: BlackjackEngine) {
        _gameState.value = eng.state
        _timeBalance.value = eng.timeBalance
        persistBalance(eng.timeBalance)
    }

    private fun persistBalance(balance: Int) {
        viewModelScope.launch {
            val existing = playerDao.get()
            if (existing != null) {
                playerDao.updateBalance(balance)
            } else {
                playerDao.upsert(PlayerState(timeBalance = balance))
            }
        }
    }
}
