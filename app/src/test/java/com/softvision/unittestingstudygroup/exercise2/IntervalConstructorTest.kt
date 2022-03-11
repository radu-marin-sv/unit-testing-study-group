package com.softvision.unittestingstudygroup.exercise2

import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class IntervalConstructorTest {
    @Test
    fun `given a start smaller than an end, when creating an interval, then it will succeed`() {
        // given
        val start = 10
        val end = 15

        // when
        try {
            val result = Interval(start, end)
        } catch (exception: Exception) {
            // then
            fail()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a start greater than an end, when creating an interval, then it will fail with an Exception`() {
        // given
        val start = 15
        val end = 10

        // when
        val result = Interval(start, end)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given a start equal to an end, when creating an interval, then it will fail with an Exception`() {
        // given
        val start = 15
        val end = 15

        // when
        val result = Interval(start, end)
    }
}