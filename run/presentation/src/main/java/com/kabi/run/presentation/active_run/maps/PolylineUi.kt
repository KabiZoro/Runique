package com.kabi.run.presentation.active_run.maps

import androidx.compose.ui.graphics.Color
import com.kabi.core.domain.location.Location

data class PolylineUi(
    val location1: Location,
    val location2: Location,
    val color: Color
)
