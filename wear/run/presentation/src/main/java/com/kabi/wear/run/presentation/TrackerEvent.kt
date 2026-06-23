package com.kabi.wear.run.presentation

import com.kabi.core.presentation.ui.UiText

sealed interface TrackerEvent {
    data object RunFinished : TrackerEvent
    data class Error(val message: UiText): TrackerEvent
}