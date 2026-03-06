package com.habiti.app

import android.app.Application
import org.koin.core.context.startKoin

class HabitiApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
        }
    }
}