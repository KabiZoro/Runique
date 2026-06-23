package com.kabi.auth.presentation.register

sealed interface RegisterAction {
    data object OnToggleOnPasswordVisibilityClick: RegisterAction
    data object OnLoginClick: RegisterAction
    data object OnRegisterClick: RegisterAction
}