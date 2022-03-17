package com.softvision.unittestingstudygroup.exercise4

import android.util.Patterns

// This is open for testing only!!! DO NOT ABUSE ME!!!
open class ValidateEmailAddress {
    enum class Result {
        SUCCESS,
        EMPTY,
        INVALID_EMAIL
    }

    open fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    operator fun invoke(email: String): Result {
        return when {
            email.isBlank() -> Result.EMPTY
            isValidEmail(email) -> Result.SUCCESS
            else -> Result.INVALID_EMAIL
        }
    }
}