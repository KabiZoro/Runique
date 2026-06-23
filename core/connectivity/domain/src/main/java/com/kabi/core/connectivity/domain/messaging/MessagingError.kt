package com.kabi.core.connectivity.domain.messaging

import com.kabi.core.domain.util.Error

enum class MessagingError: Error {
    CONNECTION_IS_INTERRUPTED,
    DISCONNECTED,
    UNKNOWN
}