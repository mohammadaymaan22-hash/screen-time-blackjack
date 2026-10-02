package com.placeholder.screentimeblackjack.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// NOIR CASINO: BLACK, WHITE & CRIMSON RED PALETTE
// =========================================================================

// Pure Blacks & Dark Obsidian Surfaces
val CasinoBlack = Color(0xFF000000)               // Pure Void Black
val CasinoDarkBg = Color(0xFF0A0A0A)              // App canvas deep background
val CasinoSurface = Color(0xFF141414)             // Card containers & bottom dock
val CasinoSurfaceElevated = Color(0xFF1E1E1E)     // Elevated chips & modals
val BorderDark = Color(0xFF2E2E2E)                // Subtle dark hairline border

// Crisp Whites
val PureWhite = Color(0xFFFFFFFF)                 // Pure 100% White
val OffWhite = Color(0xFFF2F2F2)                  // Card face background
val WhiteMuted = Color(0xB3FFFFFF)                // 70% opacity white
val WhiteSubtle = Color(0x66FFFFFF)               // 40% opacity white
val WhiteBorder = Color(0x33FFFFFF)               // 20% white border

// Vivid Crimson Reds
val CasinoRed = Color(0xFFE50914)                 // Signature vibrant casino red
val CasinoRedBright = Color(0xFFFF222A)           // Alert / highlight red
val CasinoRedDark = Color(0xFF8B0000)             // Deep dark crimson
val CasinoRedBg = Color(0xFF200507)               // Subdued red background container
val AlertBorderRed = Color(0xFFFF2E36)            // Bright red warning border
val AlertTextRed = Color(0xFFFF8589)              // Light red warning text

// Backward-compatible semantic bindings (all strictly mapped to Black, White, Red)
val CasinoGreenDeep = CasinoDarkBg
val CasinoGreenDark = CasinoBlack
val CasinoGreenFelt = CasinoSurface
val CasinoGreenLight = CasinoRed
val CasinoGreenHighlight = CasinoRedBright
val CasinoFeltRim = CasinoBlack

// Gold aliases redirected to White or Red
val CasinoGold = CasinoRed
val CasinoGoldLight = PureWhite
val CasinoGoldDark = CasinoRedDark
val CasinoGoldMuted = Color(0x59E50914)

// Cards
val CardFaceBg = PureWhite
val CardBorderColor = Color(0xFFCCCCCC)
val CardBackDark = Color(0xFF0C0C0C)
val CardBackGold = CasinoRed
val SuitRed = CasinoRed
val SuitBlack = Color(0xFF0A0A0A)

// Screen Time Currency & Indicators
val TimeBankCyan = PureWhite
val TimeBankMint = PureWhite
val TimeBankAmber = CasinoRed
val TimeBankDanger = CasinoRedBright
val TimeBankGlow = Color(0x33E50914)

// Chips
val ChipWhite = PureWhite
val ChipBlue = Color(0xFF181818)                  // Obsidian Chip
val ChipGreen = Color(0xFF282828)                 // Charcoal Chip
val ChipRed = CasinoRed                           // Red Chip
val ChipBlack = CasinoBlack                       // Pure Black Chip

// Surfaces & Borders
val SurfaceTable = CasinoSurface
val SurfaceCard = CasinoSurface
val SurfaceCardElevated = CasinoSurfaceElevated
val BorderSubtle = BorderDark
val BorderGold = Color(0x80E50914)

// Text Hierarchy
val TextPrimary = PureWhite
val TextSecondary = WhiteMuted
val TextTertiary = WhiteSubtle
val TextDark = CasinoBlack

// Alerts
val FeltRed = CasinoRedBg
val CasinoSurfaceDark = CasinoSurface
val CasinoGreenCardBg = CasinoSurfaceElevated
val PureBlack = CasinoBlack
