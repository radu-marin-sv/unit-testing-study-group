package com.softvision.unittestingstudygroup.exercise3

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class IsInThePastTest {
    private lateinit var isInThePast: IsInThePast

    private lateinit var timeReference: Date

    @Before
    fun setUp() {
        isInThePast = IsInThePast()

        // Time ref is 2020/7/15
        val calendar = Calendar.getInstance()
        calendar.set(2020, 7, 15)
        timeReference = calendar.time
    }

    @Test
    fun `given the date was in the past, when checking if the date is in the past, then it will report true`() {
        // given
        val calendar = Calendar.getInstance()
        calendar.set(2019, 7, 15)
        val date = calendar.time

        // when
        val result = isInThePast(date, now = timeReference)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given the date was in the future, when checking if the date is in the past, then it will report false`() {
        // given
        val calendar = Calendar.getInstance()
        calendar.set(2028, 7, 15)
        val date = calendar.time

        // when
        val result = isInThePast(date, now = timeReference)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `given the date was in the present moment, when checking if the date is in the past, then it will report false`() {
        // given
        val date = timeReference

        // when
        val result = isInThePast(date, now = timeReference)

        // then
        assertThat(result, equalTo(false))
    }
}