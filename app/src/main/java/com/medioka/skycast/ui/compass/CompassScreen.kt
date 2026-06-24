package com.medioka.skycast.ui.compass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medioka.skycast.ui.theme.GradientEnd
import com.medioka.skycast.ui.theme.GradientStart
import com.medioka.skycast.ui.theme.Primary
import com.medioka.skycast.ui.theme.PrimaryContainer
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompassViewModel = koinViewModel()
) {
    val azimuth by viewModel.azimuth.collectAsState()

    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        label = "AzimuthRotation"
    )

    val directionText = remember(azimuth) {
        getCardinalDirection(azimuth)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Compass Sensor",
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${azimuth.toInt()}°",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Primary
                        )
                        Text(
                            text = directionText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color.White.copy(alpha = 0.04f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(6.dp, 24.dp)
                            .background(Color(0xFFE53935), RoundedCornerShape(50))
                    )

                    Canvas(
                        modifier = Modifier
                            .size(260.dp)
                            .graphicsLayer {
                                rotationZ = -animatedAzimuth
                            }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        for (angle in 0 until 360 step 5) {
                            val angleRad = Math.toRadians(angle.toDouble())
                            val tickLength = if (angle % 30 == 0) 18.dp.toPx() else 8.dp.toPx()
                            val strokeWidth = if (angle % 30 == 0) 2.dp.toPx() else 1.dp.toPx()
                            val color = if (angle % 90 == 0) Primary else Color.White.copy(alpha = 0.4f)

                            val startX = center.x + (radius - tickLength) * sin(angleRad).toFloat()
                            val startY = center.y - (radius - tickLength) * cos(angleRad).toFloat()
                            val endX = center.x + radius * sin(angleRad).toFloat()
                            val endY = center.y - radius * cos(angleRad).toFloat()

                            drawLine(
                                color = color,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeWidth
                            )
                        }

                        val paintN = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#E53935")
                            textSize = 24.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val paintOthers = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 20.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        val offsetText = 24.dp.toPx()
                        drawContext.canvas.nativeCanvas.drawText("N", center.x, center.y - radius + offsetText + 12.dp.toPx(), paintN)
                        drawContext.canvas.nativeCanvas.drawText("S", center.x, center.y + radius - offsetText, paintOthers)
                        drawContext.canvas.nativeCanvas.drawText("E", center.x + radius - offsetText, center.y + 8.dp.toPx(), paintOthers)
                        drawContext.canvas.nativeCanvas.drawText("W", center.x - radius + offsetText, center.y + 8.dp.toPx(), paintOthers)

                        val northPath = Path().apply {
                            moveTo(center.x, center.y - 80.dp.toPx())
                            lineTo(center.x - 12.dp.toPx(), center.y)
                            lineTo(center.x + 12.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(
                            path = northPath,
                            color = Color(0xFFE53935)
                        )

                        val southPath = Path().apply {
                            moveTo(center.x, center.y + 80.dp.toPx())
                            lineTo(center.x - 12.dp.toPx(), center.y)
                            lineTo(center.x + 12.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(
                            path = southPath,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = center
                        )
                    }
                }
            }
        }
    }
}

private fun getCardinalDirection(azimuth: Float): String {
    return when (azimuth) {
        in 337.5f..360f, in 0f..22.5f -> "NORTH"
        in 22.5f..67.5f -> "NORTH EAST"
        in 67.5f..112.5f -> "EAST"
        in 112.5f..157.5f -> "SOUTH EAST"
        in 157.5f..202.5f -> "SOUTH"
        in 202.5f..247.5f -> "SOUTH WEST"
        in 247.5f..292.5f -> "WEST"
        in 292.5f..337.5f -> "NORTH WEST"
        else -> "N"
    }
}
