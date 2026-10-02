package com.placeholder.screentimeblackjack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.placeholder.screentimeblackjack.data.AppDatabase
import com.placeholder.screentimeblackjack.data.PlayerState
import com.placeholder.screentimeblackjack.engine.BlackjackEngine
import com.placeholder.screentimeblackjack.engine.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel bridging the BlackjackEngine and Room persistence to the UI.
 *
 * Exposes [gameState] and [timeBalance] as StateFlows for the Compose screen
 * to observe. Persists balance changes to Room on every mutation.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).playerStateDao()

    private var engine: BlackjackEngine? = null

    private val _gameState = MutableStateFlow<GameState>(GameState.Betting(0))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timeBalance = MutableStateFlow(0)
    val timeBalance: StateFlow<Int> = _timeBalance.asStateFlow()

    /** True once the initial balance has been loaded from Room. */
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = dao.get()
            val balance = saved?.timeBalance ?: 60

            engine = BlackjackEngine(initialBalance = balance)
            _timeBalance.value = balance
            _gameState.value = GameState.Betting(balance)
            _isReady.value = true
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
            val existing = dao.get()
            if (existing != null) {
                dao.updateBalance(balance)
            } else {
                dao.upsert(PlayerState(timeBalance = balance))
            }
        }
    }
}
