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
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.ui.components.*
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Main game screen for Screen Time Blackjack with full casino styling,
 * responsive felt table layout, and tactile controls.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val timeBalance by viewModel.timeBalance.collectAsState()
    val isReady by viewModel.isReady.collectAsState()

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
                    onOpenRules = { showRulesDialog = true },
                    onOpenSettings = { showSettingsDialog = true },
                    onOpenHistory = { showHistoryDialog = true },
                    onAddEmergencyTime = { viewModel.addTime(15) }
                )
            },
            bottomBar = {
                BottomControlsDock(
                    gameState = gameState,
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
                    onEmergencyGrant = { viewModel.addTime(15) }
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
                        .padding(top = 4.dp, bottom = 4.dp)
                )

                // Blocked App Interception Alert Banner (when launched via Accessibility gating)
                if (blockedAppAlert != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = FeltRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                                Text(
                                    text = "Access Blocked: Out of time! Win blackjack hands to unlock.",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearBlockedAppAlert() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
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
                        colors = CardDefaults.cardColors(containerColor = CasinoGoldDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                                Text(
                                    text = safeguardBlockReason!!,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearSafeguardBlock() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                            }
                        }
                    }
                }

                // Dealer Area
                DealerSection(gameState = gameState)

                // Center Table Felt: Outcomes or Bet Status
                CenterFeltSection(gameState = gameState)

                // Player Area
                PlayerSection(gameState = gameState)

                Spacer(Modifier.height(8.dp))
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
private fun DealerSection(gameState: GameState) {
    val (dealerCards, hideHoleCard, handValue, isSoft, isBust, isBlackjack) = when (gameState) {
        is GameState.Betting -> Hex(emptyList<Card>(), false, null, false, false, false)
        is GameState.PlayerTurn -> Hex(
            gameState.dealerHand.cards,
            true,
            null,
            false,
            false,
            false
        )
        is GameState.DealerTurn -> Hex(
            gameState.dealerHand.cards,
            false,
            gameState.dealerHand.value,
            gameState.dealerHand.isSoft,
            gameState.dealerHand.isBust(),
            gameState.dealerHand.isBlackjack()
        )
        is GameState.HandResolved -> Hex(
            gameState.dealerHand.cards,
            false,
            gameState.dealerHand.value,
            gameState.dealerHand.isSoft,
            gameState.dealerHand.isBust(),
            gameState.dealerHand.isBlackjack()
        )
    }

    HandView(
        title = "Dealer (Stands on 17)",
        cards = dealerCards,
        hideFirstCard = hideHoleCard,
        handValue = handValue,
        isSoft = isSoft,
        isBlackjack = isBlackjack,
        isBust = isBust
    )
}

@Composable
private fun PlayerSection(gameState: GameState) {
    val (playerCards, handValue, isSoft, isBust, isBlackjack) = when (gameState) {
        is GameState.Betting -> Quint(emptyList<Card>(), null, false, false, false)
        is GameState.PlayerTurn -> Quint(
            gameState.playerHand.cards,
            gameState.playerHand.value,
            gameState.playerHand.isSoft,
            gameState.playerHand.isBust(),
            gameState.playerHand.isBlackjack()
        )
        is GameState.DealerTurn -> Quint(
            gameState.playerHand.cards,
            gameState.playerHand.value,
            gameState.playerHand.isSoft,
            gameState.playerHand.isBust(),
            gameState.playerHand.isBlackjack()
        )
        is GameState.HandResolved -> Quint(
            gameState.playerHand.cards,
            gameState.playerHand.value,
            gameState.playerHand.isSoft,
            gameState.playerHand.isBust(),
            gameState.playerHand.isBlackjack()
        )
    }

    HandView(
        title = "Your Hand",
        cards = playerCards,
        handValue = handValue,
        isSoft = isSoft,
        isBlackjack = isBlackjack,
        isBust = isBust
    )
}

@Composable
private fun CenterFeltSection(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        when (gameState) {
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
                            color = CasinoGoldLight,
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
                    color = CasinoGold.copy(alpha = 0.35f),
                    fontSize = 11.sp,
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
            when (gameState) {
                is GameState.Betting -> {
                    BettingDockContent(
                        timeBalance = timeBalance,
                        selectedWager = selectedWager,
                        onWagerChange = onWagerChange,
                        onPlaceBet = onPlaceBet,
                        onEmergencyGrant = onEmergencyGrant
                    )
                }
                is GameState.PlayerTurn -> {
                    PlayerTurnDockContent(
                        onHit = onHit,
                        onStand = onStand
                    )
                }
                is GameState.DealerTurn -> {
                    Box(Modifier.height(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CasinoGold, modifier = Modifier.size(28.dp))
                    }
                }
                is GameState.HandResolved -> {
                    HandResolvedDockContent(
                        timeBalance = timeBalance,
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
    selectedWager: Int,
    onWagerChange: (Int) -> Unit,
    onPlaceBet: (Int) -> Unit,
    onEmergencyGrant: () -> Unit
) {
    if (timeBalance <= 0) {
        // Out of screen time state
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SCREEN TIME DEPLETED",
                color = SuitRed,
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
                    color = CasinoGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            TextButton(
                onClick = { onWagerChange(1) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Clear", color = TextTertiary, fontSize = 11.sp)
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
            stripeColor = CasinoGold,
            textColor = TextDark,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 1)) },
            enabled = selectedWager + 1 <= timeBalance
        )
        CasinoChip(
            label = "5m",
            baseColor = ChipBlue,
            stripeColor = ChipWhite,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 5)) },
            enabled = selectedWager + 5 <= timeBalance
        )
        CasinoChip(
            label = "10m",
            baseColor = ChipGreen,
            stripeColor = ChipWhite,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 10)) },
            enabled = selectedWager + 10 <= timeBalance
        )
        CasinoChip(
            label = "25m",
            baseColor = ChipRed,
            stripeColor = CasinoGold,
            textColor = Color.White,
            onClick = { onWagerChange(minOf(timeBalance, selectedWager + 25)) },
            enabled = selectedWager + 25 <= timeBalance
        )
        CasinoChip(
            label = "MAX",
            baseColor = ChipBlack,
            stripeColor = CasinoGold,
            textColor = CasinoGoldLight,
            onClick = { onWagerChange(timeBalance) },
            enabled = timeBalance > 0
        )
    }

    // Deal Action Button
    Button(
        onClick = { onPlaceBet(selectedWager) },
        enabled = selectedWager in 1..timeBalance,
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
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun PlayerTurnDockContent(
    onHit: () -> Unit,
    onStand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HIT Button (Emerald)
        Button(
            onClick = onHit,
            colors = ButtonDefaults.buttonColors(
                containerColor = CasinoGreenLight,
                contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGreenHighlight),
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

        // STAND Button (Crimson / Slate)
        Button(
            onClick = onStand,
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceCardElevated,
                contentColor = CasinoGoldLight
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold),
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
                color = SuitRed,
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
        // Option to repeat previous bet directly if enough balance
        if (lastWager in 1..timeBalance) {
            OutlinedButton(
                onClick = {
                    onNewHand()
                    onRebet(lastWager)
                },
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
            onClick = onNewHand,
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
                letterSpacing = 1.sp
            )
        }
    }
}

private data class Hex<A, B, C, D, E, F>(
    val first: A, val second: B, val third: C,
    val fourth: D, val fifth: E, val sixth: F
)

private data class Quint<A, B, C, D, E>(
    val first: A, val second: B, val third: C, val fourth: D, val fifth: E
)
