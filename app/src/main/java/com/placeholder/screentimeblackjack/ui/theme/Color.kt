package com.placeholder.screentimeblackjack.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// PURE MINIMAL 2-COLOR PALETTE: PURE OLED BLACK & CRISP WHITE
// =========================================================================

val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

// Subtle structural levels using strict opacities of white
val WhiteHigh = Color(0xFFFFFFFF)              // 100% white for active text & high contrast
val WhiteMedium = Color(0xCCFFFFFF)            // 80% white for secondary content
val WhiteMuted = Color(0x80FFFFFF)             // 50% white for tertiary labels & subtext
val WhiteBorder = Color(0x33FFFFFF)            // 20% white for geometric borders
val WhiteSubtle = Color(0x1AFFFFFF)            // 10% white for soft surface fills

// Base background & surfaces
val CasinoGreenDeep = PureBlack
val CasinoGreenDark = PureBlack
val CasinoGreenFelt = PureBlack
val CasinoGreenLight = PureBlack
val CasinoGreenHighlight = PureBlack
val CasinoFeltRim = PureBlack

// Accents (mapped to crisp white / high contrast)
val CasinoGold = PureWhite
val CasinoGoldLight = PureWhite
val CasinoGoldDark = WhiteMuted
val CasinoGoldMuted = WhiteBorder

// Card themes (pure high-contrast monochrome)
val CardFaceBg = PureWhite
val CardBorderColor = PureWhite
val CardBackDark = PureBlack
val CardBackGold = PureWhite
val SuitRed = PureBlack
val SuitBlack = PureBlack

// Time currency indicators
val TimeBankCyan = PureWhite
val TimeBankMint = PureWhite
val TimeBankAmber = WhiteMedium
val TimeBankDanger = WhiteMuted
val TimeBankGlow = Color(0x26FFFFFF)

// Chips (minimal monochromatic rings)
val ChipWhite = PureWhite
val ChipBlue = PureWhite
val ChipGreen = PureWhite
val ChipRed = PureWhite
val ChipBlack = PureBlack

// UI Surfaces & Text
val SurfaceTable = PureBlack
val SurfaceCard = PureBlack
val SurfaceCardElevated = PureBlack
val TextPrimary = PureWhite
val TextSecondary = WhiteMuted
val TextTertiary = WhiteBorder
val TextDark = PureBlack
val BorderSubtle = WhiteBorder
val BorderGold = WhiteBorder

// Aliases
val CasinoSurfaceDark = PureBlack
val CasinoGreenCardBg = PureBlack
val FeltRed = PureWhite
