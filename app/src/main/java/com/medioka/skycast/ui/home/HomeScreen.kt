package com.medioka.skycast.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medioka.skycast.domain.model.WeatherInfo
import com.medioka.skycast.ui.common.DashboardErrorView
import com.medioka.skycast.ui.common.DashboardSkeleton
import com.medioka.skycast.ui.common.WeatherIllustration
import com.medioka.skycast.ui.theme.Background
import com.medioka.skycast.ui.theme.GradientEnd
import com.medioka.skycast.ui.theme.GradientStart
import com.medioka.skycast.ui.theme.HeadlineLgMobileTextStyle
import com.medioka.skycast.ui.theme.HeroTempTextStyle
import com.medioka.skycast.ui.theme.LabelSmTextStyle
import com.medioka.skycast.ui.theme.Primary
import com.medioka.skycast.ui.theme.PrimaryContainer
import com.medioka.skycast.ui.theme.Secondary
import com.medioka.skycast.ui.theme.Tertiary
import org.koin.androidx.compose.koinViewModel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.medioka.skycast.ui.common.LocationPermissionDeniedView
import com.medioka.skycast.ui.common.LocationDisabledView

private fun isLocationPermissionGranted(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

private fun isLocationServicesEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
           locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(isLocationPermissionGranted(context)) }
    var isGpsEnabled by remember { mutableStateOf(isLocationServicesEnabled(context)) }
    var bypassLocationError by remember { mutableStateOf(false) }

    val hasSavedLocation = remember { viewModel.hasSavedLocation() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = isLocationPermissionGranted(context)
                isGpsEnabled = isLocationServicesEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasPermission = fineGranted || coarseGranted
        isGpsEnabled = isLocationServicesEnabled(context)
    }

    
    LaunchedEffect(Unit) {
        val coords = viewModel.getDefaultCoordinates()
        viewModel.fetchWeather(coords.first, coords.second)
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
                            text = "SkyCast",
                            fontWeight = FontWeight.Bold,
                            style = HeadlineLgMobileTextStyle,
                            color = Primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToMap,
                    containerColor = PrimaryContainer,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Open Map Selection",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!hasPermission && !bypassLocationError) {
                    LocationPermissionDeniedView(
                        onRequestPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onUseSavedLocation = if (hasSavedLocation) {
                            { bypassLocationError = true }
                        } else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!isGpsEnabled && !bypassLocationError) {
                    LocationDisabledView(
                        onEnableLocation = {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        },
                        onUseSavedLocation = if (hasSavedLocation) {
                            { bypassLocationError = true }
                        } else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            val state = uiState
                            if (state is HomeUiState.Success) {
                                viewModel.fetchWeather(
                                    state.weatherInfo.coordinate.latitude,
                                    state.weatherInfo.coordinate.longitude
                                )
                            } else {
                                val coords = viewModel.getDefaultCoordinates()
                                viewModel.fetchWeather(coords.first, coords.second)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (val state = uiState) {
                            is HomeUiState.Loading -> {
                                DashboardSkeleton()
                            }
                            is HomeUiState.Success -> {
                                DashboardContent(
                                    weatherInfo = state.weatherInfo,
                                    isOffline = state.isOffline,
                                    onRefresh = {
                                        viewModel.fetchWeather(
                                            state.weatherInfo.coordinate.latitude,
                                            state.weatherInfo.coordinate.longitude
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is HomeUiState.Error -> {
                                DashboardErrorView(
                                    message = state.message,
                                    onRetry = {
                                        val coords = viewModel.getDefaultCoordinates()
                                        viewModel.fetchWeather(coords.first, coords.second)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardContent(
    weatherInfo: WeatherInfo,
    isOffline: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        
        if (isOffline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF93000A).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Running in Offline Mode. Showing last cached data.",
                    color = Color(0xFFFFB4AB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = weatherInfo.cityName,
                            color = Color.White.copy(alpha = 0.6f),
                            style = LabelSmTextStyle
                        )
                    }

                    Text(
                        text = "${weatherInfo.temperature.toInt()}°C",
                        style = HeroTempTextStyle,
                        color = Primary
                    )

                    Text(
                        text = weatherInfo.condition,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "H: ${weatherInfo.maxTemp.toInt()}° L: ${weatherInfo.minTemp.toInt()}°",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    
                    WeatherIllustration(
                        conditionCode = weatherInfo.conditionCode,
                        modifier = Modifier.size(110.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS Pin",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${"%.4f".format(weatherInfo.coordinate.latitude)}, ${"%.4f".format(weatherInfo.coordinate.longitude)}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }

        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FEELS LIKE",
                        color = Color.White.copy(alpha = 0.6f),
                        style = LabelSmTextStyle
                    )
                    Text(
                        text = "${weatherInfo.feelsLike.toInt()}°",
                        color = Tertiary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LAST SYNC",
                        color = Color.White.copy(alpha = 0.6f),
                        style = LabelSmTextStyle
                    )
                    val formattedTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(weatherInfo.lastUpdated))
                    Text(
                        text = formattedTime,
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        
        Text(
            text = "5-Day Forecast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )

        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weatherInfo.forecast) { forecastItem ->
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = forecastItem.dayName.uppercase(),
                            color = Color.White.copy(alpha = 0.6f),
                            style = LabelSmTextStyle
                        )

                        WeatherIllustration(
                            conditionCode = forecastItem.conditionCode,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = forecastItem.condition,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${forecastItem.temperature.toInt()}°",
                            color = Primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
