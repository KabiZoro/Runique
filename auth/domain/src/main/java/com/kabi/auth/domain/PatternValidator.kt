package com.kabi.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}