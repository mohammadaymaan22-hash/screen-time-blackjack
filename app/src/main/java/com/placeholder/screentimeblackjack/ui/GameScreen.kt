package com.placeholder.screentimeblackjack.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
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
 * Main game screen for Screen Time Blackjack with full luxury casino styling,
 * animated card deals, audio feedback, and tactile controls.
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

    var showRulesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var selectedWager by remember { mutableIntStateOf(5) }
    var lastWager by remember { mutableIntStateOf(5) }

    val blockedAppAlert by viewModel.blockedAppAlert.collectAsState()
    val safeguardBlockReason by viewModel.safeguardBlockReason.collectAsState()

    // Clamp selected wager when balance changes
    LaunchedEffect(timeBalance) {
        if (timeBalance > 0 && selectedWager > timeBalance) {
            selectedWager = timeBalance
        } else if (selectedWager == 0 && timeBalance > 0) {
            selectedWager = minOf(5, timeBalance)
        }
    }

    if (showRulesDialog) {
        RulesDialog(onDismiss = { showRulesDialog = false })
    }

    if (showHistoryDialog) {
        HandHistoryDialog(
            viewModel = viewModel,
            onDismiss = { showHistoryDialog = false }
        )
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
                .background(CasinoGreenDeep),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = CasinoGold)
                Text(
                    text = "Opening Casino Floor...",
                    color = CasinoGoldLight,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Luxury Green Felt Canvas Background
        TableFeltBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBarContent(
                    timeBalance = timeBalance,
                    onOpenRules = {
                        SoundManager.playTap()
                        showRulesDialog = true
                    },
                    onOpenSettings = {
                        SoundManager.playTap()
                        showSettingsDialog = true
                    },
                    onOpenHistory = {
                        SoundManager.playTap()
                        showHistoryDialog = true
                    },
                    onAddEmergencyTime = {
                        SoundManager.playTap()
                        viewModel.addTime(15)
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
                    onNewHand = { viewModel.startNewHand() },
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
                // Time Bank Currency HUD
                TimeBankHud(
                    balanceMinutes = timeBalance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp)
                )

                // Blocked App Interception Alert Banner (with high contrast dark-wine styling)
                if (blockedAppAlert != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = FeltRed),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertBorderRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AlertBorderRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "ACCESS INTERCEPTED",
                                        color = AlertBorderRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Out of screen time! Win hands at the table to unlock monitored apps.",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    SoundManager.playTap()
                                    viewModel.clearBlockedAppAlert()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Safeguard Block Alert Banner (Anti-compulsion limits)
                if (safeguardBlockReason != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CasinoGold)
                                Text(
                                    text = safeguardBlockReason!!,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    SoundManager.playTap()
                                    viewModel.clearSafeguardBlock()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
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

                // Center Table Felt: Outcomes or Bet Status
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

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun TopAppBarContent(
    timeBalance: Int,
    onOpenRules: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddEmergencyTime: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CasinoGold.copy(alpha = 0.2f))
                    .border(width = 1.dp, color = CasinoGold, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♠",
                    color = CasinoGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    text = "SCREEN TIME CASINO",
                    color = CasinoGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "BLACKJACK TABLE 1",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (timeBalance <= 0) {
                IconButton(
                    onClick = onAddEmergencyTime,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Time",
                        tint = TimeBankCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Hand History",
                    tint = CasinoGoldLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "App Gatekeeper Settings",
                    tint = CasinoGoldLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenRules,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "House Rules",
                    tint = CasinoGold,
                    modifier = Modifier.size(22.dp)
                )
            }
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
        title = "Dealer (Stands on 17)",
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
        title = "Your Hand",
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
            .height(68.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isDealingActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCardElevated.copy(alpha = 0.9f))
                    .border(width = 1.dp, color = BorderGold, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = CasinoGold,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "DEALING...",
                        color = CasinoGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        } else when (gameState) {
            is GameState.HandResolved -> {
                OutcomeBanner(
                    outcome = gameState.outcome,
                    payout = gameState.payout,
                    bet = gameState.bet
                )
            }
            is GameState.PlayerTurn -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCardElevated.copy(alpha = 0.9f))
                        .border(width = 1.dp, color = BorderGold, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CURRENT WAGER:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${gameState.bet} min",
                            color = CasinoGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            is GameState.Betting -> {
                Text(
                    text = "♠ BLACKJACK PAYS 3 TO 2 ♠",
                    color = CasinoGoldLight.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Serif
                )
            }
            is GameState.DealerTurn -> {
                Text(
                    text = "Dealer playing...",
                    color = CasinoGoldLight,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif
                )
            }
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
        color = SurfaceTable.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        CircularProgressIndicator(color = CasinoGold, modifier = Modifier.size(28.dp))
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
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Your digital curfew is active. Reload minutes to wager more screen time.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onEmergencyGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TimeBankCyan,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Emergency Grant (+15 min)", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Wager Header with Clear & Wager Count
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SELECT WAGER",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCardElevated)
                    .border(width = 1.dp, color = BorderGold, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$selectedWager min",
                    color = CasinoGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            TextButton(
                onClick = {
                    SoundManager.playTap()
                    onWagerChange(1)
                },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Reset", color = TextTertiary, fontSize = 11.sp)
            }
        }
    }

    // Tactile Chips Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CasinoChip(
            label = "1m",
            baseColor = ChipWhite,
            stripeColor = Color(0xFF455A64),
            textColor = TextDark,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 1)) },
            enabled = !isDealingActive && selectedWager + 1 <= timeBalance
        )
        CasinoChip(
            label = "5m",
            baseColor = ChipBlue,
            stripeColor = Color.White,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 5)) },
            enabled = !isDealingActive && selectedWager + 5 <= timeBalance
        )
        CasinoChip(
            label = "10m",
            baseColor = ChipGreen,
            stripeColor = Color.White,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 10)) },
            enabled = !isDealingActive && selectedWager + 10 <= timeBalance
        )
        CasinoChip(
            label = "25m",
            baseColor = ChipRed,
            stripeColor = CasinoGold,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 25)) },
            enabled = !isDealingActive && selectedWager + 25 <= timeBalance
        )
        CasinoChip(
            label = "MAX",
            baseColor = ChipBlack,
            stripeColor = CasinoGold,
            textColor = CasinoGold,
            onClick = { onWagerChange(timeBalance) },
            enabled = !isDealingActive && timeBalance > 0
        )
    }

    // Deal Action Button (Lustrous Gold Gradient)
    Button(
        onClick = {
            SoundManager.playTap()
            onPlaceBet(selectedWager)
        },
        enabled = !isDealingActive && selectedWager in 1..timeBalance,
        colors = ButtonDefaults.buttonColors(
            containerColor = CasinoGold,
            contentColor = TextDark,
            disabledContainerColor = SurfaceCardElevated,
            disabledContentColor = TextTertiary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
    ) {
        Text(
            text = "DEAL HAND ($selectedWager min)",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.2.sp
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HIT Button (Vibrant Emerald)
        Button(
            onClick = {
                SoundManager.playTap()
                onHit()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = CasinoGreenLight,
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TimeBankMint),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = TimeBankMint)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "HIT",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // STAND Button (Rich Burgundy)
        Button(
            onClick = {
                SoundManager.playTap()
                onStand()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = ChipRed,
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = CasinoGold)
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
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onEmergencyGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TimeBankCyan,
                    contentColor = TextDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "REBET ($lastWager m)",
                    color = CasinoGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Deal Next Hand
        Button(
            onClick = {
                SoundManager.playTap()
                onNewHand()
            },
            enabled = !isDealingActive,
            colors = ButtonDefaults.buttonColors(
                containerColor = CasinoGold,
                contentColor = TextDark
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(if (lastWager in 1..timeBalance) 1.2f else 1f)
                .height(52.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
        ) {
            Text(
                text = "NEXT HAND",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 1.sp
            )
        }
    }
}
