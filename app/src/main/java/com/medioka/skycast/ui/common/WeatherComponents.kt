package com.medioka.skycast.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medioka.skycast.ui.theme.Primary
import com.medioka.skycast.ui.theme.PrimaryContainer
import com.medioka.skycast.ui.theme.Tertiary
import com.medioka.skycast.ui.theme.TertiaryContainer

// 1. Shimmer Animation Modifier
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    background(brush = brush)
}

// 2. Custom Canvas Weather Illustrations (Sun, Clouds, Rain, Thunderstorms)
@Composable
fun WeatherIllustration(
    conditionCode: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        when (conditionCode) {
            0 -> drawSun(center, width * 0.35f) // Clear Sky
            1, 2, 3 -> { // Partly Cloudy / Overcast
                drawSun(center - Offset(width * 0.15f, height * 0.15f), width * 0.25f)
                drawCloud(center + Offset(width * 0.05f, height * 0.08f), width * 0.5f)
            }
            45, 48 -> drawFog(center, width * 0.5f) // Foggy
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> { // Rainy / Drizzle
                drawCloud(center - Offset(0f, height * 0.1f), width * 0.45f)
                drawRaindrops(center + Offset(0f, height * 0.2f), width * 0.4f)
            }
            95, 96, 99 -> { // Thunderstorm
                drawCloud(center - Offset(0f, height * 0.1f), width * 0.45f)
                drawLightning(center + Offset(0f, height * 0.18f), width * 0.25f)
            }
            else -> drawCloud(center, width * 0.5f)
        }
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(TertiaryContainer, Tertiary, Color.Transparent),
            center = center,
            radius = radius * 1.3f
        ),
        center = center,
        radius = radius * 1.2f
    )
    drawCircle(
        color = Tertiary,
        center = center,
        radius = radius
    )
}

private fun DrawScope.drawCloud(center: Offset, width: Float) {
    val cloudColor = Color.White.copy(alpha = 0.85f)
    val shadowColor = Primary.copy(alpha = 0.2f)

    // Shadow glow
    drawRoundRect(
        color = shadowColor,
        topLeft = center - Offset(width / 2f, width * 0.25f),
        size = Size(width, width * 0.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.25f, width * 0.25f)
    )

    // Main cloud body
    drawRoundRect(
        color = cloudColor,
        topLeft = center - Offset(width / 2.2f, width * 0.2f),
        size = Size(width * 0.9f, width * 0.4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.2f, width * 0.2f)
    )
    // Cloud dome
    drawCircle(
        color = cloudColor,
        center = center - Offset(width * 0.1f, width * 0.12f),
        radius = width * 0.22f
    )
}

private fun DrawScope.drawFog(center: Offset, width: Float) {
    val fogColor = Color.White.copy(alpha = 0.6f)
    for (i in 0..2) {
        val yOffset = center.y - (width * 0.2f) + (i * width * 0.15f)
        drawRoundRect(
            color = fogColor,
            topLeft = Offset(center.x - (width * 0.4f), yOffset),
            size = Size(width * 0.8f, 8.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
    }
}

private fun DrawScope.drawRaindrops(center: Offset, width: Float) {
    val rainColor = Primary
    val startX = center.x - (width * 0.3f)
    for (i in 0..2) {
        val x = startX + (i * width * 0.3f)
        drawLine(
            color = rainColor,
            start = Offset(x, center.y - (width * 0.2f)),
            end = Offset(x - (width * 0.1f), center.y + (width * 0.2f)),
            strokeWidth = 3.dp.toPx()
        )
    }
}

private fun DrawScope.drawLightning(center: Offset, height: Float) {
    val lightningColor = Tertiary
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x + (height * 0.2f), center.y - (height * 0.5f))
        lineTo(center.x - (height * 0.2f), center.y)
        lineTo(center.x + (height * 0.1f), center.y)
        lineTo(center.x - (height * 0.1f), center.y + (height * 0.5f))
        lineTo(center.x + (height * 0.3f), center.y - (height * 0.05f))
        lineTo(center.x, center.y - (height * 0.05f))
        close()
    }
    drawPath(path = path, color = lightningColor)
}

// 3. Skeleton Loading Screen Composable
@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Top Space
        Spacer(modifier = Modifier.height(16.dp))

        // Large Card Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .shimmer()
        )

        // Bento Stats Grid Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .shimmer()
                )
            }
        }

        // Forecast Header Skeleton
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(4.dp))
                .shimmer()
        )

        // Forecast Cards List Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(140.dp)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .shimmer()
                )
            }
        }
    }
}

// 4. Custom Error Overlay with Recovery Support
@Composable
fun DashboardErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFFFB4AB),
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Oops! Something went wrong",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry Icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRY AGAIN",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
