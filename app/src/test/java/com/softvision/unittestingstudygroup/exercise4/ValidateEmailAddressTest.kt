package com.softvision.unittestingstudygroup.exercise4

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class ValidateEmailAddressTest {
    private lateinit var validateEmailAddress: ValidateEmailAddress

    @Test
    fun `given a valid email address, when validating the input, then it will succeed`() {
        // given
        validateEmailAddress = object : ValidateEmailAddress() {
            override fun isValidEmail(email: String): Boolean {
                return true
            }
        }
        val input = "radu.marin@softivison.com"

        // when
        val result = validateEmailAddress(input)

        // then
        assertThat(result, equalTo(ValidateEmailAddress.Result.SUCCESS))
    }

    @Test
    fun `given an invalid email address, when validating the input, then it will fail`() {
        // given
        validateEmailAddress = object : ValidateEmailAddress() {
            override fun isValidEmail(email: String): Boolean {
                return false
            }
        }
        val input = "0radu.marin@softivison.com"

        // when
        val result = validateEmailAddress(input)

        // then
        assertThat(result, equalTo(ValidateEmailAddress.Result.INVALID_EMAIL))
    }

    @Test
    fun `given an empty email, when validating the input, then it will fail`() {
        validateEmailAddress = ValidateEmailAddress()

        // given
        val input = ""

        // when
        val result = validateEmailAddress(input)

        // then
        assertThat(result, equalTo(ValidateEmailAddress.Result.EMPTY))
    }

    @Test
    fun `given an email containing only space, when validating the input, then it will fail`() {
        validateEmailAddress = ValidateEmailAddress()

        // given
        val input = " \n\t\r"

        // when
        val result = validateEmailAddress(input)

        // then
        assertThat(result, equalTo(ValidateEmailAddress.Result.EMPTY))
    }
}