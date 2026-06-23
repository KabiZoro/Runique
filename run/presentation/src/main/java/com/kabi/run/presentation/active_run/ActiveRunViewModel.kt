package com.kabi.run.presentation.active_run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabi.core.connectivity.domain.messaging.MessagingAction
import com.kabi.core.domain.location.Location
import com.kabi.core.domain.run.Run
import com.kabi.core.domain.run.RunRepository
import com.kabi.core.domain.util.Result
import com.kabi.core.notification.ActiveRunService
import com.kabi.core.presentation.ui.asUiText
import com.kabi.run.domain.LocationDataCalculator
import com.kabi.run.domain.RunningTracker
import com.kabi.run.domain.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class ActiveRunViewModel(
    private val runningTracker: RunningTracker,
    private val runRepository: RunRepository,
    private val watchConnector: WatchConnector,
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(
        ActiveRunState(
            shouldTrack = ActiveRunService.isServiceActive.value && runningTracker.isTracking.value,
            hasStartedRunning = ActiveRunService.isServiceActive.value
        )
    )
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ActiveRunState()
        )

    private val eventChannel = Channel<ActiveRunEvent>()
    val events = eventChannel.receiveAsFlow()

    private val shouldTrack = _state
        .map { it.shouldTrack }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            _state.value.shouldTrack
        )
    private val hasLocationPermission = MutableStateFlow(false)

    private val isTracking = combine(
        shouldTrack,
        hasLocationPermission
    ) { shouldTrack, hasPermission ->
        shouldTrack && hasPermission
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    init {
        watchConnector
            .connectedDevice
            .filterNotNull()
            .onEach {
                Timber.d("New device detected: ${it.displayName}")
            }
            .launchIn(viewModelScope)

        hasLocationPermission
            .onEach { hasPermission ->
                if (hasPermission) {
                    runningTracker.startObservingLocation()
                } else {
                    runningTracker.stopObservingLocation()
                }
            }
            .launchIn(viewModelScope)

        isTracking
            .onEach { isTracking ->
                runningTracker.setIsTracking(isTracking)
            }
            .launchIn(viewModelScope)

        runningTracker
            .currentLocation
            .onEach { locationWithAltitude ->
                _state.update { it.copy(currentLocation = locationWithAltitude?.location) }
            }
            .launchIn(viewModelScope)

        runningTracker
            .runData
            .onEach { runData ->
                _state.update { it.copy(runData = runData) }
            }
            .launchIn(viewModelScope)

        runningTracker
            .elapsedTime
            .onEach { elapsedTime ->
                _state.update { it.copy(elapsedTime = elapsedTime) }
            }
            .launchIn(viewModelScope)

        listenToWatchActions()
    }

    fun onAction(action: ActiveRunAction, triggeredOnWatch: Boolean = false) {
        if (!triggeredOnWatch) {
            val messagingAction = when (action) {
                ActiveRunAction.OnFinishRunClick -> MessagingAction.Finish
                ActiveRunAction.OnResumeRunClick -> MessagingAction.StartOrResume
                ActiveRunAction.OnToggleRunClick -> {
                    if (_state.value.hasStartedRunning) {
                        MessagingAction.Pause
                    } else {
                        MessagingAction.StartOrResume
                    }
                }

                else -> null
            }

            messagingAction?.let {
                viewModelScope.launch {
                    watchConnector.sendActionToWatch(it)
                }
            }
        }
        when (action) {
            ActiveRunAction.OnBackClick -> {
                _state.update {
                    it.copy(
                        shouldTrack = false
                    )
                }
            }

            ActiveRunAction.OnFinishRunClick -> {
                _state.update {
                    it.copy(
                        isRunFinished = true,
                        isSavingRun = true
                    )
                }
            }

            ActiveRunAction.OnResumeRunClick -> {
                _state.update {
                    it.copy(
                        shouldTrack = true
                    )
                }
            }

            ActiveRunAction.OnToggleRunClick -> {
                _state.update {
                    it.copy(
                        hasStartedRunning = true,
                        shouldTrack = !_state.value.shouldTrack
                    )
                }
            }

            is ActiveRunAction.SubmitLocationPermissionInfo -> {
                hasLocationPermission.value = action.acceptedLocationPermission
                _state.update {
                    it.copy(
                        showLocationRationale = action.showLocationPermissionRationale
                    )
                }
            }

            is ActiveRunAction.SubmitNotificationPermissionInfo -> {
                _state.update {
                    it.copy(
                        showNotificationRationale = action.showNotificationPermissionRationale
                    )
                }
            }

            ActiveRunAction.DismissRationaleDialog -> {
                _state.update {
                    it.copy(
                        showLocationRationale = false,
                        showNotificationRationale = false
                    )
                }
            }

            is ActiveRunAction.OnRunProcessed -> {
                finishRun(action.mapPictureBytes)
            }
        }
    }

    private fun finishRun(mapPictureBytes: ByteArray) {
        val locations = _state.value.runData.locations
        if (locations.isEmpty() || locations.first().size <= 1) {
            _state.update { it.copy(isSavingRun = false) }
            return
        }

        viewModelScope.launch {
            val s = _state.value
            val run = Run(
                id = null,
                duration = s.elapsedTime,
                dateTimeUtc = ZonedDateTime.now()
                    .withZoneSameInstant(ZoneId.of("UTC")),
                distanceMeters = s.runData.distanceMeters,
                location = s.currentLocation ?: Location(0.0, 0.0),
                maxSpeedKmh = LocationDataCalculator.getMaxSpeedKmh(locations),
                totalElevationMeters = LocationDataCalculator.getTotalElevationMeters(locations),
                mapPictureUrl = null,
                avgHeartRate = if (_state.value.runData.heartRates.isEmpty()) {
                    null
                } else {
                    _state.value.runData.heartRates.average().roundToInt()
                },
                maxHeartRate = if (_state.value.runData.heartRates.isEmpty()) {
                    null
                } else {
                    _state.value.runData.heartRates.max()
                }
            )

            runningTracker.finishRun()

            when (val result = runRepository.upsertRun(run, mapPictureBytes)) {
                is Result.Error -> {
                    eventChannel.send(ActiveRunEvent.Error(result.error.asUiText()))
                }

                is Result.Success -> {
                    eventChannel.send(ActiveRunEvent.RunSaved)
                }
            }

            _state.update { it.copy(isSavingRun = false) }
        }
    }

    private fun listenToWatchActions() {
        watchConnector
            .messagingActions
            .onEach { action ->
                when (action) {
                    MessagingAction.ConnectionRequest -> {
                        if (isTracking.value) {
                            watchConnector.sendActionToWatch(MessagingAction.StartOrResume)
                        }
                    }

                    MessagingAction.Finish -> {
                        onAction(
                            action = ActiveRunAction.OnFinishRunClick,
                            triggeredOnWatch = true
                        )
                    }

                    MessagingAction.Pause -> {
                        if (isTracking.value) {
                            onAction(
                                action = ActiveRunAction.OnToggleRunClick,
                                triggeredOnWatch = true
                            )
                        }
                    }

                    MessagingAction.StartOrResume -> {
                        if (!isTracking.value) {
                            if (_state.value.hasStartedRunning) {
                                onAction(
                                    action = ActiveRunAction.OnResumeRunClick,
                                    triggeredOnWatch = true
                                )
                            } else {
                                onAction(
                                    action = ActiveRunAction.OnToggleRunClick,
                                    triggeredOnWatch = true
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive.value) {
            applicationScope.launch {
                watchConnector.sendActionToWatch(MessagingAction.UnTrackable)
            }
            runningTracker.stopObservingLocation()
        }
    }
}