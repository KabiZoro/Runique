package com.kabi.analytics.presentation

sealed interface AnalyticsAction {
    data object OnBackClick: AnalyticsAction
}