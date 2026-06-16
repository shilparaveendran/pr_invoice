package com.app.printf

import android.app.Application
import com.app.printf.di.AppContainer

class PrintfApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
