package com.medioka.weatherapp.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medioka.weatherapp.ui.theme.HeadlineLgTextStyle
import com.medioka.weatherapp.ui.theme.LabelSmTextStyle
import com.medioka.weatherapp.ui.theme.Primary
import com.medioka.weatherapp.ui.theme.PrimaryContainer
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapScreen(
    onLocationSelected: (Double, Double) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Setup OSMDroid user agent configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. AndroidView wrapping OSM MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    val startPoint = GeoPoint(uiState.latitude, uiState.longitude)
                    controller.setCenter(startPoint)

                    // Track scroll zoom stops to update center coordinates in viewmodel
                    addMapListener(object : org.osmdroid.events.MapListener {
                        override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                            val center = mapCenter
                            viewModel.updateLocation(center.latitude, center.longitude)
                            return true
                        }

                        override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                            val center = mapCenter
                            viewModel.updateLocation(center.latitude, center.longitude)
                            return true
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Center Locator Pin (Hovering over the map)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp), // offset for visual pin balance
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Target Selector Pin",
                    tint = PrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp, 6.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                )
            }
        }

        // 3. Floating Search and Back Bar (Top Layer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glass Back Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button",
                        tint = Primary
                    )
                }
            }

            // Glass Search Bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Selector",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Move map to choose point",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Location Details Bottom Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(32.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Drag Handle Indicator
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = uiState.cityName,
                            style = HeadlineLgTextStyle,
                            color = Color.White
                        )
                        val latStr = "%.4f".format(uiState.latitude)
                        val lonStr = "%.4f".format(uiState.longitude)
                        Text(
                            text = "$latStr° N, $lonStr° W",
                            style = LabelSmTextStyle,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    // Selected Temperature Circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = uiState.temperature,
                                color = Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // select location button
                Button(
                    onClick = {
                        onLocationSelected(uiState.latitude, uiState.longitude)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "SELECT LOCATION",
                        fontWeight = FontWeight.Bold,
                        style = LabelSmTextStyle,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
