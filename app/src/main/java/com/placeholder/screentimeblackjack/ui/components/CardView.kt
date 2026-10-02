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
 * 2-color minimal playing card component in pure black & crisp white.
 */
@Composable
fun PlayingCardView(
    card: Card?,
    isFaceUp: Boolean = true,
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 98.dp,
    elevation: Dp = 0.dp
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    val isShowingBack = rotation > 90f

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(8.dp))
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
 * Minimalist Card Front: Pure white card face, jet black typography & suit glyph.
 */
@Composable
private fun CardFaceView(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(PureWhite)
            .border(width = 1.dp, color = PureWhite, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp, vertical = 4.dp)
    ) {
        // Top-left index
        CardCornerIndex(
            rank = card.rank.display,
            suitSymbol = card.suit.symbol,
            color = PureBlack,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center suit glyph
        Text(
            text = card.suit.symbol,
            color = PureBlack,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom-right inverted index
        CardCornerIndex(
            rank = card.rank.display,
            suitSymbol = card.suit.symbol,
            color = PureBlack,
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
            fontFamily = FontFamily.Monospace,
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
 * Minimalist Card Back: Pure black with crisp white geometric grid.
 */
@Composable
fun CardBackView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(PureBlack)
            .border(width = 1.5.dp, color = PureWhite, shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = WhiteBorder, shape = RoundedCornerShape(4.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 10.dp.toPx()
                var x = 0f
                while (x < size.width + size.height) {
                    drawLine(
                        color = WhiteSubtle,
                        start = Offset(x, 0f),
                        end = Offset(x - size.height, size.height),
                        strokeWidth = 1f
                    )
                    x += step
                }
            }
        }
    }
}
