package com.softvision.unittestingstudygroup.exercise1


import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.Before
import org.junit.Test

class CalculatorTest {
    private lateinit var calculator: Calculator

    @Before
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun `given the number 0, when computing the average, then the result will be 0`() {
        // when
        val result = calculator.mean(0.0)

        // then
        assertThat(result, equalTo(0.0))
    }

    @Test
    fun `given the same natural number multiple times, when computing the average, then the result will be that exact number`() {
        // when
        val result = calculator.mean(1.0, 1.0, 1.0)

        // then
        assertThat(result, equalTo(1.0))
    }

    @Test
    fun `given different natural numbers, when computing the average, then the result will be their average`() {
        // when
        val result = calculator.mean(1.0, 2.0, 3.0)

        // then
        assertThat(result, equalTo(2.0))
    }

    @Test
    fun `given the same irrational number multiple times, when computing the average, then the result will be that exact number`() {
        // when
        val result = calculator.mean(0.1, 0.1, 0.1)

        // then
        assertThat(result, closeTo(0.1, 0.0001))
    }

    @Test
    fun `given different irrational numbers, when computing the average, then the result will be their average`() {
        // when
        val result = calculator.mean(0.1, 0.2, 0.3)

        // then
        assertThat(result, closeTo(0.2, 0.0001))
    }

    @Test
    fun `given different negative numbers, when computing the average, then the result will be their average`() {
        // when
        val result = calculator.mean(-1.0, -2.0, -3.0)

        // then
        assertThat(result, equalTo(-2.0))
    }

    @Test
    fun `given a series of numbers with a mean of 0, when computing the average, then the result will be 0`() {
        // when
        val result = calculator.mean(-1.0, 1.0)

        // then
        assertThat(result, equalTo(0.0))
    }

    @Test
    fun `given no numbers, when computing the average, then the result will be Not a Number`() {
        // when
        val result = calculator.mean()

        // then
        assertThat(result, equalTo(Double.NaN))
    }
}