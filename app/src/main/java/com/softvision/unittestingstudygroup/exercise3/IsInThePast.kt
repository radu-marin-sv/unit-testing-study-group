package com.softvision.unittestingstudygroup.exercise3

import java.util.*

class IsInThePast {
    operator fun invoke(arg: Date, now: Date = Date()): Boolean {
        return now.time - arg.time > 0
    }
}