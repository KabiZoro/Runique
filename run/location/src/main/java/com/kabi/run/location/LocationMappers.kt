package com.kabi.run.location

import android.location.Location
import com.kabi.core.domain.location.LocationWithAltitude

fun Location.toLocationWithAltitude(): LocationWithAltitude{
    return LocationWithAltitude(
        location = com.kabi.core.domain.location.Location(
            lat = latitude,
            long = longitude
        ),
        altitude = altitude
    )
}