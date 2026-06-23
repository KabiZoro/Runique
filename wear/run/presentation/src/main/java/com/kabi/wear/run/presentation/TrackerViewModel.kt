@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.kabi.wear.run.presentation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabi.core.connectivity.domain.messaging.MessagingAction
import com.kabi.core.domain.util.Result
import com.kabi.core.notification.ActiveRunService
import com.kabi.wear.run.domain.ExerciseTracker
import com.kabi.wear.run.domain.PhoneConnector
import com.kabi.wear.run.domain.RunningTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TrackerViewModel(
    private val exerciseTracker: ExerciseTracker,
    private val phoneConnector: PhoneConnector,
    private val runningTracker: RunningTracker
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val hasBodySensorPermission = MutableStateFlow(false)

    private val _eventChannel = Channel<TrackerEvent>()
    val events = _eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(
        TrackerState(
            hasStartedRunning = ActiveRunService.isServiceActive.value,
            isRunActive = ActiveRunService.isServiceActive.value && runningTracker.isTracking.value,
            isTrackable = ActiveRunService.isServiceActive.value
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
            initialValue = TrackerState()
        )

    private val isTracking = snapshotFlow {
        _state.value.isRunActive && _state.value.isTrackable && _state.value.isConnectedPhoneNearby
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    init {
        phoneConnector
            .connectedNode
            .filterNotNull()
            .onEach { connectedNode ->
                _state.update {
                    it.copy(
                        isConnectedPhoneNearby = connectedNode.isNearBy
                    )
                }
            }
            .combine(isTracking) { _, isTracking ->
                if (!isTracking) {
                    phoneConnector.sendActionToPhone(MessagingAction.ConnectionRequest)
                }
            }
            .launchIn(viewModelScope)

        runningTracker
            .isTrackable
            .onEach { isTrackable ->
                _state.update {
                    it.copy(isTrackable = isTrackable)
                }
            }
            .launchIn(viewModelScope)

        isTracking
            .onEach { isTracking ->
                val result = when {
                    isTracking && !_state.value.hasStartedRunning -> {
                        exerciseTracker.startExercise()
                    }

                    isTracking && _state.value.hasStartedRunning -> {
                        exerciseTracker.resumeExercise()
                    }

                    !isTracking && _state.value.hasStartedRunning -> {
                        exerciseTracker.pauseExercise()
                    }

                    else -> Result.Success(Unit)
                }

                if (result is Result.Error) {
                    result.error.toUiText()?.let {
                        _eventChannel.send(TrackerEvent.Error(it))
                    }
                }

                if (isTracking) {
                    _state.update {
                        it.copy(hasStartedRunning = true)
                    }
                }
                runningTracker.setIsTracking(isTracking)
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val isHeartRateTrackingSupported = exerciseTracker.isHeartRateTrackingSupported()
            _state.update {
                it.copy(canTrackHeartRate = isHeartRateTrackingSupported)
            }
        }

        val isAmbientMode = snapshotFlow { _state.value.isAmbientMode }

        isAmbientMode
            .flatMapLatest { isAmbientMode ->
                if (isAmbientMode){
                    runningTracker
                        .heartRate
                        .sample(10.seconds)
                } else {
                    runningTracker.heartRate
                }
            }
            .onEach { heartRate ->
                _state.update {
                    it.copy(heartRate = heartRate)
                }
            }
            .launchIn(viewModelScope)

        isAmbientMode
            .flatMapLatest { isAmbientMode ->
                if (isAmbientMode){
                    runningTracker
                        .elapsedTime
                        .sample(10.seconds)
                } else {
                    runningTracker.elapsedTime
                }
            }
            .onEach { elapsedDuration ->
                _state.update {
                    it.copy(elapsedDuration = elapsedDuration)
                }
            }
            .launchIn(viewModelScope)

        runningTracker
            .distanceMeters
            .onEach { distanceMeters ->
                _state.update {
                    it.copy(distanceMeters = distanceMeters)
                }
            }
            .launchIn(viewModelScope)

        listenToPhoneActions()
    }

    fun onAction(action: TrackerAction, triggeredOnPhone: Boolean = false) {
        if (!triggeredOnPhone) {
            sendActionToPhone(action)
        }
        when (action) {
            is TrackerAction.OnBodySensorPermissionResult -> {
                hasBodySensorPermission.value = action.isGranted
                if (action.isGranted) {
                    viewModelScope.launch {
                        val isHeartRateTrackingSupported =
                            exerciseTracker.isHeartRateTrackingSupported()
                        _state.update {
                            it.copy(
                                canTrackHeartRate = isHeartRateTrackingSupported
                            )
                        }
                    }
                }
            }

            TrackerAction.OnFinishRunClick -> {
                viewModelScope.launch {
                    exerciseTracker.stopExercise()
                    _eventChannel.send(TrackerEvent.RunFinished)

                    _state.update {
                        it.copy(
                            elapsedDuration = Duration.ZERO,
                            distanceMeters = 0,
                            heartRate = 0,
                            hasStartedRunning = false,
                            isRunActive = false
                        )
                    }
                }
            }

            TrackerAction.OnToggleRunClick -> {
                if (_state.value.isTrackable) {
                    _state.update {
                        it.copy(
                            isRunActive = !_state.value.isRunActive
                        )
                    }
                }
            }

            is TrackerAction.OnEnterAmbientMode -> {
                _state.update {
                    it.copy(
                        isAmbientMode = true,
                        burnInProtectionRequired = action.burnInProtectionRequired
                    )
                }
            }

            TrackerAction.OnExitAmbientMode -> {
                _state.update {
                    it.copy(isAmbientMode = false)
                }
            }
        }
    }

    private fun sendActionToPhone(action: TrackerAction) {
        viewModelScope.launch {
            val messagingAction = when (action) {
                TrackerAction.OnFinishRunClick -> MessagingAction.Finish
                TrackerAction.OnToggleRunClick -> {
                    if (_state.value.isRunActive) {
                        MessagingAction.Pause
                    } else {
                        MessagingAction.StartOrResume
                    }
                }

                else -> null
            }

            messagingAction?.let {
                val result = phoneConnector.sendActionToPhone(it)
                if (result is Result.Error) {
                    println("Tracker error: ${result.error}")
                }
            }
        }
    }

    private fun listenToPhoneActions() {
        phoneConnector
            .messagingActions
            .onEach { action ->
                when (action) {
                    MessagingAction.Finish -> {
                        onAction(TrackerAction.OnFinishRunClick, triggeredOnPhone = true)
                    }

                    MessagingAction.Pause -> {
                        if (_state.value.isTrackable) {
                            _state.update {
                                it.copy(isRunActive = false)
                            }
                        }
                    }

                    MessagingAction.StartOrResume -> {
                        if (_state.value.isTrackable) {
                            _state.update {
                                it.copy(isRunActive = true)
                            }
                        }
                    }

                    MessagingAction.Trackable -> {
                        _state.update {
                            it.copy(isTrackable = true)
                        }
                    }

                    MessagingAction.UnTrackable -> {
                        _state.update {
                            it.copy(isTrackable = false)
                        }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

}