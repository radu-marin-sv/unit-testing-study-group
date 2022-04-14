package com.softvision.unittestingstudygroup.exercise5

import io.mockk.MockKAnnotations
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class OtherMockkTimeTest {
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        mockk<Repository>()
    }
}