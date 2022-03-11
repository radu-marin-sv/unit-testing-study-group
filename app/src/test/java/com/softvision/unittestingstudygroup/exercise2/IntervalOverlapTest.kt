package com.softvision.unittestingstudygroup.exercise2


import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class IntervalOverlapTest {
    @Test
    fun `given interval 1 was before interval 2, when computing the overlap, then the result will be false`() {
        // given
        val interval1 = Interval(0, 2)
        val interval2 = Interval(3, 5)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `given interval 1 overlapped over the start of interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(0, 4)
        val interval2 = Interval(3, 5)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 was contained in interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(4, 6)
        val interval2 = Interval(3, 8)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 contained interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(3, 8)
        val interval2 = Interval(4, 6)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 overlapped over the end of interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(4, 7)
        val interval2 = Interval(3, 5)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 was after interval 2, when computing the overlap, then the result will be false`() {
        // given
        val interval1 = Interval(6, 9)
        val interval2 = Interval(3, 5)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `given interval 1 was before and adjacent to interval 2, when computing the overlap, then the result will be false`() {
        // given
        val interval1 = Interval(1, 3)
        val interval2 = Interval(3, 5)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `given interval 1 overlapped and was adjacent to the start of interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(3, 6)
        val interval2 = Interval(3, 9)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 overlapped and adjacent to the end of interval 2, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(5, 8)
        val interval2 = Interval(3, 8)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `given interval 1 was after and adjacent to the end of interval 2, when computing the overlap, then the result will be false`() {
        // given
        val interval1 = Interval(9, 12)
        val interval2 = Interval(3, 9)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `given two identical intervals, when computing the overlap, then the result will be true`() {
        // given
        val interval1 = Interval(3, 9)
        val interval2 = Interval(3, 9)

        // when
        val result = interval1.overlaps(interval2)

        // then
        assertThat(result, equalTo(true))
    }
}