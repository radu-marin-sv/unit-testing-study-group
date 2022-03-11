package com.softvision.unittestingstudygroup.exercise2


data class Interval(val start: Int, val end: Int) {
    init {
        require(start < end) { "A proper interval has a start value smaller than its end value" }
    }

    fun overlaps(other: Interval): Boolean {
        return (end > other.start) and (start < other.end)
    }
}