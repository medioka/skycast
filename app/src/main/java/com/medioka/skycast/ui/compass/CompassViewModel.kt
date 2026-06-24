package com.medioka.skycast.ui.compass

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CompassViewModel(context: Context) : ViewModel() {
    private val compassSensorManager = CompassSensorManager(context)

    val azimuth: StateFlow<Float> = compassSensorManager.getAzimuthFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0f
        )
}
