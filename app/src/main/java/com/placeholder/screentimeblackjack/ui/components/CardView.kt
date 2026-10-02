package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.Card
import com.placeholder.screentimeblackjack.engine.Suit
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Authentic casino playing card component with realistic ivory cardstock,
 * crisp suit colors, ornate card back, 3D flip rotation, and tactile elevation.
 */
@Composable
fun PlayingCardView(
    card: Card?,
    isFaceUp: Boolean = true,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 98.dp,
    elevation: Dp = 6.dp,
    tiltAngle: Float = 0f
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    val isShowingBack = rotation > 90f

    Box(
        modifier = modifier
            .rotate(tiltAngle)
            .size(width = width, height = height)
            .shadow(elevation = elevation, shape = RoundedCornerShape(9.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(9.dp))
    ) {
        if (isShowingBack || card == null) {
            CardBackView(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            )
        } else {
            CardFaceView(
                card = card,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Card Front: Fine ivory cardstock, bold corner indices, and center pip.
 */
@Composable
private fun CardFaceView(
    card: Card,
    modifier: Modifier = Modifier
) {
    val suitColor = when (card.suit) {
        Suit.HEARTS, Suit.DIAMONDS -> SuitRed
        Suit.CLUBS, Suit.SPADES -> SuitBlack
    }

    Box(
        modifier = modifier
            .background(CardFaceBg)
            .border(width = 1.dp, color = CardBorderColor, shape = RoundedCornerShape(9.dp))
            .padding(horizontal = 5.dp, vertical = 4.dp)
    ) {
        // Top-left index
        CardCornerIndex(
            rank = card.rank.display,
            suitSymbol = card.suit.symbol,
            color = suitColor,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center suit glyph or court graphic
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom-right inverted index
        CardCornerIndex(
            rank = card.rank.display,
            suitSymbol = card.suit.symbol,
            color = suitColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .rotate(180f)
        )
    }
}

@Composable
private fun CardCornerIndex(
    rank: String,
    suitSymbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = rank,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            lineHeight = 13.sp
        )
        Text(
            text = suitSymbol,
            color = color,
            fontSize = 12.sp,
            lineHeight = 12.sp
        )
    }
}

/**
 * Luxury Casino Card Back: Deep royal navy with gold ornate lattice & double border.
 */
@Composable
fun CardBackView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CardBackDark)
            .border(width = 1.5.dp, color = CardBackGold, shape = RoundedCornerShape(9.dp))
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = CardBackGold.copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 8.dp.toPx()
                var x = 0f
                while (x < size.width + size.height) {
                    // Forward diagonal lines
                    drawLine(
                        color = CardBackGold.copy(alpha = 0.25f),
                        start = Offset(x, 0f),
                        end = Offset(x - size.height, size.height),
                        strokeWidth = 1f
                    )
                    // Backward diagonal lines
                    drawLine(
                        color = CardBackGold.copy(alpha = 0.25f),
                        start = Offset(x - size.height, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }

            // Center spade watermark emblem on card back
            Text(
                text = "♠",
                color = CardBackGold.copy(alpha = 0.7f),
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
