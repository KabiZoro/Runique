package com.kabi.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                loginCredentialsValidation()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LoginState()
        )

    private val _eventChannel = Channel<LoginEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> {}
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update {
                    it.copy(
                        isPasswordVisible = !it.isPasswordVisible
                    )
                }
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true) }
            val result = authRepository.login(
                email = _state.value.email.text.toString().trim(),
                password = _state.value.password.text.toString()
            )
            _state.update { it.copy(isLoggingIn = false) }

            when(result){
                is Result.Error -> {
                    if (result.error == DataError.Network.UNAUTHORIZED){
                        _eventChannel.send(LoginEvent.Error(
                            UiText.StringResource(R.string.error_email_password_incorrect)
                        ))
                    } else {
                        _eventChannel.send(LoginEvent.Error(result.error.asUiText()))
                    }
                }
                is Result.Success -> {
                    _eventChannel.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }

    private fun loginCredentialsValidation() {
        combine(
            snapshotFlow { _state.value.email.text },
            snapshotFlow { _state.value.password.text }
        ) { email, password ->
            _state.update {
                it.copy(
                    canLogin = userDataValidator.isValidEMAIL(
                        email = email.toString().trim()
                    ) && password.isNotEmpty()
                )
            }
        }.launchIn(viewModelScope)
    }


}