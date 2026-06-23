package com.kabi.auth.presentation.register

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabi.auth.domain.AuthRepository
import com.kabi.auth.domain.UserDataValidator
import com.kabi.core.domain.util.DataError
import com.kabi.core.domain.util.Result
import com.kabi.core.presentation.ui.R
import com.kabi.core.presentation.ui.UiText
import com.kabi.core.presentation.ui.asUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userDataValidator: UserDataValidator,
    private val repository: AuthRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                credentialsValidation()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterState()
        )

    private val _eventChannel = Channel<RegisterEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnToggleOnPasswordVisibilityClick -> {
                _state.update {
                    it.copy(
                        isPasswordVisible = !it.isPasswordVisible
                    )
                }
            }
            RegisterAction.OnLoginClick -> {}
        }
    }

    private fun register(){
        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true) }
            val result = repository.register(
                email = _state.value.email.text.toString().trim(),
                password = _state.value.password.text.toString()
            )
            _state.update { it.copy(isRegistering = false) }

            when(result){
                is Result.Error -> {
                    if (result.error == DataError.Network.CONFLICT){
                        _eventChannel.send(RegisterEvent.Error(
                            UiText.StringResource(R.string.error_email_exists)
                        ))
                    }else {
                        _eventChannel.send(RegisterEvent.Error(result.error.asUiText()))
                    }
                }
                is Result.Success -> {
                    _eventChannel.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }

    private fun credentialsValidation() {
        snapshotFlow { _state.value.email.text }
            .onEach { email ->
                val isValidEmail = userDataValidator.isValidEMAIL(email.toString())
                _state.update {
                    it.copy(
                        isEmailValid = isValidEmail,
                        canRegister = isValidEmail && it.passwordValidationState.isValidPassword
                                && !it.isRegistering
                    )
                }
            }
            .launchIn(viewModelScope)

        snapshotFlow { _state.value.password.text }
            .onEach { password ->
                val passwordValidationState = userDataValidator.validatePassword(password.toString())
                _state.update {
                    it.copy(
                        passwordValidationState = passwordValidationState,
                        canRegister = it.isEmailValid && passwordValidationState.isValidPassword
                                && !it.isRegistering
                    )
                }
            }
            .launchIn(viewModelScope)
    }

}
