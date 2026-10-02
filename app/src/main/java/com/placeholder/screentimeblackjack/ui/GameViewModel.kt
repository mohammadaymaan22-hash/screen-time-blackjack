package com.placeholder.screentimeblackjack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.placeholder.screentimeblackjack.data.AppDatabase
import com.placeholder.screentimeblackjack.data.PlayerState
import com.placeholder.screentimeblackjack.engine.BlackjackEngine
import com.placeholder.screentimeblackjack.engine.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.placeholder.screentimeblackjack.engine.Card
import com.placeholder.screentimeblackjack.engine.Hand
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.util.SoundManager

/**
 * ViewModel bridging the BlackjackEngine and Room persistence to the UI.
 *
 * Exposes [gameState] and [timeBalance] as StateFlows for the Compose screen
 * to observe. Persists balance changes to Room on every mutation.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val playerDao = AppDatabase.getInstance(application).playerStateDao()
    private val blockedAppDao = AppDatabase.getInstance(application).blockedAppDao()
    private val handHistoryDao = AppDatabase.getInstance(application).handHistoryDao()

    private var engine: BlackjackEngine? = null

    private val _gameState = MutableStateFlow<GameState>(GameState.Betting(0))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timeBalance = MutableStateFlow(0)
    val timeBalance: StateFlow<Int> = _timeBalance.asStateFlow()

    /** True once the initial balance has been loaded from Room. */
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /** Player state containing economy options and safeguard settings */
    val playerState: StateFlow<PlayerState> = playerDao.getFlow()
        .map { it ?: PlayerState() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlayerState()
        )

    /** Recent hand history stream */
    val recentHands: StateFlow<List<com.placeholder.screentimeblackjack.data.HandHistory>> =
        handHistoryDao.getRecentHandsFlow(50).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** Blocked apps list observed from database */
    val blockedApps: StateFlow<List<com.placeholder.screentimeblackjack.data.BlockedApp>> =
        blockedAppDao.getAllFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _blockedAppAlert = MutableStateFlow<String?>(null)
    val blockedAppAlert: StateFlow<String?> = _blockedAppAlert.asStateFlow()

    private val _safeguardBlockReason = MutableStateFlow<String?>(null)
    val safeguardBlockReason: StateFlow<String?> = _safeguardBlockReason.asStateFlow()

    private val _isDealingActive = MutableStateFlow(false)
    val isDealingActive: StateFlow<Boolean> = _isDealingActive.asStateFlow()

    private val _displayedPlayerCards = MutableStateFlow<List<Card>>(emptyList())
    val displayedPlayerCards: StateFlow<List<Card>> = _displayedPlayerCards.asStateFlow()

    private val _displayedDealerCards = MutableStateFlow<List<Card>>(emptyList())
    val displayedDealerCards: StateFlow<List<Card>> = _displayedDealerCards.asStateFlow()

    private val _isDealerHoleCardHidden = MutableStateFlow(true)
    val isDealerHoleCardHidden: StateFlow<Boolean> = _isDealerHoleCardHidden.asStateFlow()

    init {
        refreshPermissions()
        seedDefaultBlockedAppsIfEmpty()
        viewModelScope.launch {
            checkAndApplyDailyReset()
            val saved = playerDao.get()
            // Reset any leftover cooldowns or restrictions
            if (saved != null && (saved.cooldownUntilTimestamp > 0 || saved.maxHandsPerHour > 0 || saved.dailyLossCapMinutes > 0)) {
                playerDao.upsert(saved.copy(
                    cooldownUntilTimestamp = 0L,
                    consecutiveLossesCount = 0,
                    maxHandsPerHour = 0,
                    consecutiveLossThreshold = 0,
                    dailyLossCapMinutes = 0
                ))
            }
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

    fun clearSafeguardBlock() {
        _safeguardBlockReason.value = null
    }

    /** Daily reset vs rolling balance check */
    suspend fun checkAndApplyDailyReset() {
        val current = playerDao.get() ?: return
        if (!current.useDailyReset) return

        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        if (now - current.lastDailyResetTimestamp >= oneDayMillis) {
            // Apply daily reset
            val resetState = current.copy(
                timeBalance = current.dailyResetBalance,
                lastDailyResetTimestamp = now,
                currentDailyLossMinutes = 0,
                consecutiveLossesCount = 0,
                cooldownUntilTimestamp = 0L,
                lastUpdated = now
            )
            playerDao.upsert(resetState)
            engine?.resetBalance(current.dailyResetBalance)
            _timeBalance.value = current.dailyResetBalance
            _gameState.value = GameState.Betting(current.dailyResetBalance)
        }
    }

    fun updateEconomySettings(
        useDailyReset: Boolean,
        dailyResetBalance: Int,
        maxHandsPerHour: Int,
        consecutiveLossThreshold: Int,
        cooldownDurationMinutes: Int,
        dailyLossCapMinutes: Int
    ) {
        viewModelScope.launch {
            val current = playerDao.get() ?: PlayerState()
            val updated = current.copy(
                useDailyReset = useDailyReset,
                dailyResetBalance = dailyResetBalance,
                maxHandsPerHour = maxHandsPerHour,
                consecutiveLossThreshold = consecutiveLossThreshold,
                cooldownDurationMinutes = cooldownDurationMinutes,
                dailyLossCapMinutes = dailyLossCapMinutes
            )
            playerDao.upsert(updated)
        }
    }

    fun clearCooldownOverride() {
        viewModelScope.launch {
            val current = playerDao.get() ?: return@launch
            playerDao.upsert(current.copy(cooldownUntilTimestamp = 0L, consecutiveLossesCount = 0))
            _safeguardBlockReason.value = null
        }
    }

    fun placeBet(amount: Int) {
        val eng = engine ?: return

        // Anti-compulsion safeguard validation
        viewModelScope.launch {
            val pState = playerDao.get() ?: PlayerState()
            val now = System.currentTimeMillis()

            // 1. Mandatory Cooldown active check
            if (pState.cooldownUntilTimestamp > now) {
                val remMinutes = ((pState.cooldownUntilTimestamp - now) / 60_000L).coerceAtLeast(1)
                _safeguardBlockReason.value = "Cooldown Active: Take a break! Wait $remMinutes min (after ${pState.consecutiveLossThreshold} consecutive losses)."
                return@launch
            }

            // 2. Daily Loss Cap check
            if (pState.dailyLossCapMinutes > 0 && (pState.currentDailyLossMinutes + amount) > pState.dailyLossCapMinutes) {
                val remainingLossAllowance = (pState.dailyLossCapMinutes - pState.currentDailyLossMinutes).coerceAtLeast(0)
                _safeguardBlockReason.value = "Daily Loss Cap Reached: You have lost ${pState.currentDailyLossMinutes}/${pState.dailyLossCapMinutes} min today. Max bet allowed: $remainingLossAllowance min."
                return@launch
            }

            // 3. Max hands per hour check
            if (pState.maxHandsPerHour > 0) {
                val oneHourAgo = now - 3600_000L
                val handsInLastHour = handHistoryDao.getHandsCountSince(oneHourAgo)
                if (handsInLastHour >= pState.maxHandsPerHour) {
                    _safeguardBlockReason.value = "Hourly Speed Limit Reached: Max ${pState.maxHandsPerHour} hands per hour to prevent compulsion."
                    return@launch
                }
            }

            // Passed all safeguards — proceed with placing bet
            _safeguardBlockReason.value = null
            _isDealingActive.value = true
            _isDealerHoleCardHidden.value = true
            _displayedPlayerCards.value = emptyList()
            _displayedDealerCards.value = emptyList()

            eng.placeBet(amount)

            val targetPlayerCards = when (val s = eng.state) {
                is GameState.PlayerTurn -> s.playerHand.cards
                is GameState.HandResolved -> s.playerHand.cards
                else -> emptyList()
            }
            val targetDealerCards = when (val s = eng.state) {
                is GameState.PlayerTurn -> s.dealerHand.cards
                is GameState.HandResolved -> s.dealerHand.cards
                else -> emptyList()
            }

            _timeBalance.value = eng.timeBalance
            _gameState.value = GameState.PlayerTurn(Hand(emptyList()), Hand(emptyList()), amount, eng.timeBalance)

            // Step 1: Player Card 1
            delay(120)
            SoundManager.playCardDeal()
            _displayedPlayerCards.value = targetPlayerCards.take(1)

            // Step 2: Dealer Hole Card (face down)
            delay(220)
            SoundManager.playCardDeal()
            _displayedDealerCards.value = targetDealerCards.take(1)

            // Step 3: Player Card 2
            delay(220)
            SoundManager.playCardDeal()
            _displayedPlayerCards.value = targetPlayerCards.take(2)

            // Step 4: Dealer Upcard
            delay(220)
            SoundManager.playCardDeal()
            _displayedDealerCards.value = targetDealerCards.take(2)

            // Step 5: Check immediate natural blackjack
            if (eng.state is GameState.HandResolved) {
                val res = eng.state as GameState.HandResolved
                delay(350)
                _isDealerHoleCardHidden.value = false
                SoundManager.playCardDeal()
                delay(400)
                when (res.outcome) {
                    HandOutcome.PLAYER_BLACKJACK -> SoundManager.playBlackjack()
                    HandOutcome.PLAYER_WIN -> SoundManager.playWin()
                    HandOutcome.PUSH -> SoundManager.playPush()
                    HandOutcome.DEALER_WIN -> SoundManager.playBust()
                }
                syncState(eng)
            } else {
                syncState(eng)
            }
            _isDealingActive.value = false
        }
    }

    fun hit() {
        val eng = engine ?: return
        if (_isDealingActive.value) return
        viewModelScope.launch {
            _isDealingActive.value = true
            eng.hit()

            val targetPlayerCards = when (val s = eng.state) {
                is GameState.PlayerTurn -> s.playerHand.cards
                is GameState.HandResolved -> s.playerHand.cards
                else -> emptyList()
            }
            SoundManager.playCardDeal()
            _displayedPlayerCards.value = targetPlayerCards
            delay(320)

            if (eng.state is GameState.HandResolved) {
                val res = eng.state as GameState.HandResolved
                _isDealerHoleCardHidden.value = false
                SoundManager.playBust()
                delay(400)
                syncState(eng)
            } else {
                syncState(eng)
            }
            _isDealingActive.value = false
        }
    }

    fun stand() {
        val eng = engine ?: return
        if (_isDealingActive.value) return
        viewModelScope.launch {
            _isDealingActive.value = true
            eng.stand()
            val res = eng.state as? GameState.HandResolved ?: return@launch
            val finalDealerCards = res.dealerHand.cards

            // 1. Flip dealer hole card
            SoundManager.playCardDeal()
            _isDealerHoleCardHidden.value = false
            delay(450)

            // 2. Deal any dealer hit cards one-by-one with realistic casino cadence
            if (finalDealerCards.size > 2) {
                for (i in 2 until finalDealerCards.size) {
                    SoundManager.playCardDeal()
                    _displayedDealerCards.value = finalDealerCards.take(i + 1)
                    delay(450)
                }
            }

            delay(250)
            when (res.outcome) {
                HandOutcome.PLAYER_BLACKJACK -> SoundManager.playBlackjack()
                HandOutcome.PLAYER_WIN -> SoundManager.playWin()
                HandOutcome.PUSH -> SoundManager.playPush()
                HandOutcome.DEALER_WIN -> SoundManager.playBust()
            }
            syncState(eng)
            _isDealingActive.value = false
        }
    }

    fun startNewHand() {
        val eng = engine ?: return
        SoundManager.playTap()
        _isDealingActive.value = false
        _displayedPlayerCards.value = emptyList()
        _displayedDealerCards.value = emptyList()
        _isDealerHoleCardHidden.value = true
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
     * Sync the ViewModel's StateFlows from the engine, persist balance,
     * record HandHistory, and update safeguard metrics upon resolution.
     */
    private fun syncState(eng: BlackjackEngine) {
        val newState = eng.state
        _gameState.value = newState
        _timeBalance.value = eng.timeBalance

        viewModelScope.launch {
            val existing = playerDao.get() ?: PlayerState()
            var consecutiveLosses = existing.consecutiveLossesCount
            var dailyLoss = existing.currentDailyLossMinutes
            var cooldownUntil = existing.cooldownUntilTimestamp

            // If hand resolved, write HandHistory and evaluate safeguards
            if (newState is GameState.HandResolved) {
                val history = com.placeholder.screentimeblackjack.data.HandHistory(
                    bet = newState.bet,
                    payout = newState.payout,
                    outcome = newState.outcome,
                    playerCards = newState.playerHand.toString(),
                    playerValue = newState.playerHand.value,
                    dealerCards = newState.dealerHand.toString(),
                    dealerValue = newState.dealerHand.value,
                    balanceAfter = eng.timeBalance
                )
                handHistoryDao.insert(history)

                // Update loss streak and loss cap metrics
                if (newState.outcome == com.placeholder.screentimeblackjack.engine.HandOutcome.DEALER_WIN) {
                    consecutiveLosses += 1
                    dailyLoss += newState.bet

                    // Trigger cooldown if threshold reached
                    if (existing.consecutiveLossThreshold > 0 && consecutiveLosses >= existing.consecutiveLossThreshold) {
                        cooldownUntil = System.currentTimeMillis() + (existing.cooldownDurationMinutes * 60_000L)
                        _safeguardBlockReason.value = "Mandatory Cooldown Activated: ${existing.consecutiveLossThreshold} consecutive losses reached. Cooldown for ${existing.cooldownDurationMinutes} min."
                    }
                } else if (newState.outcome == com.placeholder.screentimeblackjack.engine.HandOutcome.PLAYER_WIN ||
                    newState.outcome == com.placeholder.screentimeblackjack.engine.HandOutcome.PLAYER_BLACKJACK
                ) {
                    // Reset consecutive loss streak on win
                    consecutiveLosses = 0
                }
            }

            val updatedPlayerState = existing.copy(
                timeBalance = eng.timeBalance,
                lastUpdated = System.currentTimeMillis(),
                consecutiveLossesCount = consecutiveLosses,
                currentDailyLossMinutes = dailyLoss,
                cooldownUntilTimestamp = cooldownUntil
            )
            playerDao.upsert(updatedPlayerState)
        }
    }
}
