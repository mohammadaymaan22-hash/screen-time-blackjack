package com.placeholder.screentimeblackjack.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.Card
import com.placeholder.screentimeblackjack.engine.GameState
import com.placeholder.screentimeblackjack.engine.Hand
import com.placeholder.screentimeblackjack.ui.components.*
import com.placeholder.screentimeblackjack.ui.theme.*
import com.placeholder.screentimeblackjack.util.SoundManager

/**
 * Minimalist Noir screen for Screen Time Blackjack.
 * Strict Black, White, and Crimson Red aesthetic with zero visual clutter.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val timeBalance by viewModel.timeBalance.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val isDealingActive by viewModel.isDealingActive.collectAsState()
    val displayedPlayerCards by viewModel.displayedPlayerCards.collectAsState()
    val displayedDealerCards by viewModel.displayedDealerCards.collectAsState()
    val isDealerHoleCardHidden by viewModel.isDealerHoleCardHidden.collectAsState()
    val timeBalanceSeconds by viewModel.timeBalanceSeconds.collectAsState()
    val isActivelyTracking by viewModel.isActivelyTracking.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val selectedAppPackage by viewModel.selectedAppPackage.collectAsState()
    val appSecondsMap by viewModel.appSecondsMap.collectAsState()
    val activeMonitoredPackage by viewModel.activeMonitoredPackage.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Wager strictly starts at 0
    var selectedWager by remember { mutableIntStateOf(0) }
    var lastWager by remember { mutableIntStateOf(0) }

    val safeguardBlockReason by viewModel.safeguardBlockReason.collectAsState()

    // Clamp selected wager when balance decreases, never forcing to non-zero
    LaunchedEffect(timeBalance) {
        if (timeBalance > 0 && selectedWager > timeBalance) {
            selectedWager = timeBalance
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = {
                showSettingsDialog = false
                viewModel.refreshPermissions()
            }
        )
    }

    if (!isReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CasinoBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CasinoRed, strokeWidth = 3.dp)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TableFeltBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBarContent(
                    onOpenSettings = {
                        SoundManager.playTap()
                        showSettingsDialog = true
                    }
                )
            },
            bottomBar = {
                BottomControlsDock(
                    gameState = gameState,
                    isDealingActive = isDealingActive,
                    timeBalance = timeBalance,
                    selectedWager = selectedWager,
                    lastWager = lastWager,
                    onWagerChange = { selectedWager = it },
                    onPlaceBet = { amount ->
                        lastWager = amount
                        viewModel.placeBet(amount)
                    },
                    onHit = { viewModel.hit() },
                    onStand = { viewModel.stand() },
                    onNewHand = {
                        viewModel.startNewHand()
                        selectedWager = 0
                    },
                    onEmergencyGrant = {
                        SoundManager.playTap()
                        viewModel.addTime(15)
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Hub: App Vault Selector + Time Bank HUD
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeBlockedApps = blockedApps.filter { it.isBlocked }
                    if (activeBlockedApps.isNotEmpty()) {
                        AppVaultSelector(
                            blockedApps = activeBlockedApps,
                            selectedAppPackage = selectedAppPackage,
                            appSecondsMap = appSecondsMap,
                            onSelectApp = { pkg ->
                                SoundManager.playTap()
                                viewModel.selectApp(pkg)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val selectedApp = blockedApps.find { it.packageName == selectedAppPackage }

                    TimeBankHud(
                        balanceMinutes = timeBalance,
                        balanceSeconds = timeBalanceSeconds,
                        isActivelyTracking = isActivelyTracking && activeMonitoredPackage == selectedAppPackage,
                        selectedAppName = selectedApp?.appName,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Safeguard Block Alert (Only when mandatory cooldown or limits hit)
                if (safeguardBlockReason != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CasinoRedBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertBorderRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = safeguardBlockReason!!,
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    SoundManager.playTap()
                                    viewModel.clearSafeguardBlock()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = PureWhite)
                            }
                        }
                    }
                }

                // Dealer Area
                DealerSection(
                    gameState = gameState,
                    displayedDealerCards = displayedDealerCards,
                    isDealingActive = isDealingActive,
                    isDealerHoleCardHidden = isDealerHoleCardHidden
                )

                // Center Table Area: Outcome announcement or dealing spinner
                CenterFeltSection(
                    gameState = gameState,
                    isDealingActive = isDealingActive
                )

                // Player Area
                PlayerSection(
                    gameState = gameState,
                    displayedPlayerCards = displayedPlayerCards,
                    isDealingActive = isDealingActive
                )

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/**
 * Minimalist Noir Top Bar with single Settings action.
 */
@Composable
private fun TopAppBarContent(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(CasinoRed)
                    .border(width = 1.dp, color = PureWhite, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♠",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "SCREEN TIME BLACKJACK",
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = PureWhite,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DealerSection(
    gameState: GameState,
    displayedDealerCards: List<Card>,
    isDealingActive: Boolean,
    isDealerHoleCardHidden: Boolean
) {
    val cards = if (isDealingActive || displayedDealerCards.isNotEmpty()) {
        displayedDealerCards
    } else when (gameState) {
        is GameState.PlayerTurn -> gameState.dealerHand.cards
        is GameState.DealerTurn -> gameState.dealerHand.cards
        is GameState.HandResolved -> gameState.dealerHand.cards
        is GameState.Betting -> emptyList()
    }

    val handValue = if (isDealerHoleCardHidden || cards.isEmpty()) {
        null
    } else {
        Hand(cards).value
    }

    val isBust = if (!isDealerHoleCardHidden && cards.isNotEmpty()) Hand(cards).isBust() else false
    val isBlackjack = if (!isDealerHoleCardHidden && cards.size == 2) Hand(cards).isBlackjack() else false
    val isSoft = if (!isDealerHoleCardHidden && cards.isNotEmpty()) Hand(cards).isSoft else false

    HandView(
        title = "Dealer",
        cards = cards,
        hideFirstCard = isDealerHoleCardHidden,
        handValue = handValue,
        isSoft = isSoft,
        isBlackjack = isBlackjack,
        isBust = isBust
    )
}

@Composable
private fun PlayerSection(
    gameState: GameState,
    displayedPlayerCards: List<Card>,
    isDealingActive: Boolean
) {
    val cards = if (isDealingActive || displayedPlayerCards.isNotEmpty()) {
        displayedPlayerCards
    } else when (gameState) {
        is GameState.PlayerTurn -> gameState.playerHand.cards
        is GameState.DealerTurn -> gameState.playerHand.cards
        is GameState.HandResolved -> gameState.playerHand.cards
        is GameState.Betting -> emptyList()
    }

    val handValue = if (cards.isEmpty()) null else Hand(cards).value
    val isBust = if (cards.isNotEmpty()) Hand(cards).isBust() else false
    val isBlackjack = if (cards.size == 2) Hand(cards).isBlackjack() else false
    val isSoft = if (cards.isNotEmpty()) Hand(cards).isSoft else false

    HandView(
        title = "You",
        cards = cards,
        handValue = handValue,
        isSoft = isSoft,
        isBlackjack = isBlackjack,
        isBust = isBust
    )
}

@Composable
private fun CenterFeltSection(
    gameState: GameState,
    isDealingActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isDealingActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CasinoSurfaceElevated)
                    .border(width = 1.dp, color = BorderDark, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = CasinoRed,
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "DEALING...",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else if (gameState is GameState.HandResolved) {
            OutcomeBanner(
                outcome = gameState.outcome,
                payout = gameState.payout,
                bet = gameState.bet
            )
        }
    }
}

@Composable
private fun BottomControlsDock(
    gameState: GameState,
    isDealingActive: Boolean,
    timeBalance: Int,
    selectedWager: Int,
    lastWager: Int,
    onWagerChange: (Int) -> Unit,
    onPlaceBet: (Int) -> Unit,
    onHit: () -> Unit,
    onStand: () -> Unit,
    onNewHand: () -> Unit,
    onEmergencyGrant: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = CasinoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                gameState is GameState.Betting -> {
                    BettingDockContent(
                        timeBalance = timeBalance,
                        isDealingActive = isDealingActive,
                        selectedWager = selectedWager,
                        onWagerChange = onWagerChange,
                        onPlaceBet = onPlaceBet,
                        onEmergencyGrant = onEmergencyGrant
                    )
                }
                gameState is GameState.PlayerTurn -> {
                    PlayerTurnDockContent(
                        isDealingActive = isDealingActive,
                        onHit = onHit,
                        onStand = onStand
                    )
                }
                gameState is GameState.DealerTurn -> {
                    Box(Modifier.height(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CasinoRed, modifier = Modifier.size(24.dp))
                    }
                }
                gameState is GameState.HandResolved -> {
                    HandResolvedDockContent(
                        timeBalance = timeBalance,
                        isDealingActive = isDealingActive,
                        lastWager = lastWager,
                        onNewHand = onNewHand,
                        onRebet = { onPlaceBet(it) },
                        onEmergencyGrant = onEmergencyGrant
                    )
                }
            }
        }
    }
}

@Composable
private fun BettingDockContent(
    timeBalance: Int,
    isDealingActive: Boolean,
    selectedWager: Int,
    onWagerChange: (Int) -> Unit,
    onPlaceBet: (Int) -> Unit,
    onEmergencyGrant: () -> Unit
) {
    if (timeBalance <= 0) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SCREEN TIME DEPLETED",
                color = AlertBorderRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Button(
                onClick = onEmergencyGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CasinoRed,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Emergency Grant (+15 min)", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Wager Header: Amount & Reset to 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WAGER",
            color = WhiteMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CasinoSurfaceElevated)
                    .border(width = 1.dp, color = if (selectedWager > 0) CasinoRed else BorderDark, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$selectedWager min",
                    color = if (selectedWager > 0) PureWhite else WhiteSubtle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (selectedWager > 0) {
                TextButton(
                    onClick = {
                        SoundManager.playTap()
                        onWagerChange(0) // Reset back to 0
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Clear", color = CasinoRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Tactile Chips Row (Black, White, and Red)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CasinoChip(
            label = "1m",
            baseColor = ChipWhite,
            stripeColor = CasinoBlack,
            textColor = CasinoBlack,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 1)) },
            enabled = !isDealingActive && selectedWager + 1 <= timeBalance
        )
        CasinoChip(
            label = "5m",
            baseColor = CasinoRed,
            stripeColor = PureWhite,
            textColor = PureWhite,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 5)) },
            enabled = !isDealingActive && selectedWager + 5 <= timeBalance
        )
        CasinoChip(
            label = "10m",
            baseColor = CasinoSurfaceElevated,
            stripeColor = CasinoRed,
            textColor = PureWhite,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 10)) },
            enabled = !isDealingActive && selectedWager + 10 <= timeBalance
        )
        CasinoChip(
            label = "25m",
            baseColor = CasinoRedDark,
            stripeColor = PureWhite,
            textColor = PureWhite,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 25)) },
            enabled = !isDealingActive && selectedWager + 25 <= timeBalance
        )
        CasinoChip(
            label = "MAX",
            baseColor = CasinoBlack,
            stripeColor = CasinoRedBright,
            textColor = CasinoRedBright,
            onClick = { onWagerChange(timeBalance) },
            enabled = !isDealingActive && timeBalance > 0
        )
    }

    // Deal Action Button (Disabled when wager is 0)
    Button(
        onClick = {
            SoundManager.playTap()
            onPlaceBet(selectedWager)
        },
        enabled = !isDealingActive && selectedWager in 1..timeBalance,
        colors = ButtonDefaults.buttonColors(
            containerColor = CasinoRed,
            contentColor = PureWhite,
            disabledContainerColor = CasinoSurfaceElevated,
            disabledContentColor = WhiteSubtle
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(elevation = if (selectedWager > 0) 4.dp else 0.dp, shape = RoundedCornerShape(12.dp))
    ) {
        Text(
            text = if (selectedWager > 0) "DEAL HAND ($selectedWager min)" else "SELECT WAGER (0 min)",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun PlayerTurnDockContent(
    isDealingActive: Boolean,
    onHit: () -> Unit,
    onStand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HIT Button (Pure White on Black)
        Button(
            onClick = {
                SoundManager.playTap()
                onHit()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = CasinoBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = CasinoBlack)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "HIT",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // STAND Button (Vivid Crimson Red)
        Button(
            onClick = {
                SoundManager.playTap()
                onStand()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = CasinoRed,
                contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "STAND",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun HandResolvedDockContent(
    timeBalance: Int,
    isDealingActive: Boolean,
    lastWager: Int,
    onNewHand: () -> Unit,
    onRebet: (Int) -> Unit,
    onEmergencyGrant: () -> Unit
) {
    if (timeBalance <= 0) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "OUT OF SCREEN TIME",
                color = AlertBorderRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Button(
                onClick = onEmergencyGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CasinoRed,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Emergency Grant (+15 min)", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lastWager in 1..timeBalance) {
            OutlinedButton(
                onClick = {
                    SoundManager.playTap()
                    onNewHand()
                    onRebet(lastWager)
                },
                enabled = !isDealingActive,
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoRed),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(
                    text = "REBET ($lastWager m)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Deal Next Hand (Pure White on Black)
        Button(
            onClick = {
                SoundManager.playTap()
                onNewHand()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = CasinoBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(if (lastWager in 1..timeBalance) 1.2f else 1f)
                .height(50.dp)
        ) {
            Text(
                text = "NEXT HAND",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Clean Noir App Vault Selector: Horizontal chips in Black, White, and Red.
 */
@Composable
fun AppVaultSelector(
    blockedApps: List<com.placeholder.screentimeblackjack.data.BlockedApp>,
    selectedAppPackage: String?,
    appSecondsMap: Map<String, Int>,
    onSelectApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(blockedApps, key = { it.packageName }) { app ->
            val isSelected = app.packageName == selectedAppPackage
            val sec = appSecondsMap[app.packageName] ?: app.timeBalanceSeconds
            val min = (sec + 59) / 60
            val isDepleted = sec <= 0

            val borderColor = when {
                isSelected -> CasinoRed
                isDepleted -> AlertBorderRed.copy(alpha = 0.6f)
                else -> BorderDark
            }
            val containerColor = when {
                isSelected -> CasinoSurfaceElevated
                isDepleted -> CasinoRedBg
                else -> CasinoSurface
            }

            Surface(
                onClick = { onSelectApp(app.packageName) },
                shape = RoundedCornerShape(10.dp),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSelected) {
                        Text(
                            text = "♠",
                            color = CasinoRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = app.appName,
                        color = if (isSelected) PureWhite else WhiteMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = when {
                            isDepleted -> CasinoRed
                            isSelected -> CasinoRedDark
                            else -> CasinoBlack
                        }
                    ) {
                        Text(
                            text = if (isDepleted) "0m" else "${min}m",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
