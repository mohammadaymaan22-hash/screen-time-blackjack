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
 * Standard playing card component with realistic casino proportions,
 * crisp suit emblems, and smooth 3D flip animation support.
 */
@Composable
fun PlayingCardView(
    card: Card?,
    isFaceUp: Boolean = true,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 98.dp,
    elevation: Dp = 6.dp
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    val isShowingBack = rotation > 90f

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .shadow(elevation = elevation, shape = RoundedCornerShape(8.dp), clip = false)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(8.dp))
    ) {
        if (isShowingBack || card == null) {
            // Flip the back face so it's not mirrored
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
 * Card Front: Crisp ivory card paper, dual corner indices, and center suit symbol.
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
            .border(width = 1.dp, color = CardBorderColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Top-left index
        CardCornerIndex(
            rank = card.rank.display,
            suitSymbol = card.suit.symbol,
            color = suitColor,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center large suit emblem
        Text(
            text = card.suit.symbol,
            color = suitColor.copy(alpha = 0.9f),
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom-right inverted index (rotated 180°)
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
            fontFamily = FontFamily.SansSerif,
            lineHeight = 13.sp
        )
        Text(
            text = suitSymbol,
            color = color,
            fontSize = 11.sp,
            lineHeight = 11.sp
        )
    }
}

/**
 * Card Back: Luxurious geometric casino pattern in deep emerald & gold.
 */
@Composable
fun CardBackView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CardBackDark)
            .border(width = 2.dp, color = CardBackGold, shape = RoundedCornerShape(8.dp))
            .padding(3.dp)
            .border(width = 1.dp, color = CardBackGold.copy(alpha = 0.6f), shape = RoundedCornerShape(5.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
            val step = 10.dp.toPx()
            val strokeWidth = 1.dp.toPx()

            // Diagonal lattice grid
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = CardBackGold.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x + size.height, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = CardBackGold.copy(alpha = 0.35f),
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = strokeWidth
                )
                x += step
            }

            // Center gold diamond medallion
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = 10.dp.toPx()
            drawCircle(
                color = CardBackDark,
                radius = radius + 2.dp.toPx(),
                center = center
            )
            drawCircle(
                color = CardBackGold,
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
