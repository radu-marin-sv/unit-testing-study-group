package com.softvision.unittestingstudygroup.exercise5

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {
    val io: Executor = Executors.newFixedThreadPool(3)
    val main: Executor = AndroidExecutor()

    class AndroidExecutor : Executor {
        val handler = Handler(Looper.getMainLooper())

        override fun execute(runnable: Runnable) {
            handler.post(runnable)
        }
    }
}