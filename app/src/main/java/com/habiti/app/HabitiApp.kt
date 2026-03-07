package com.habiti.app

import android.app.Application
import com.habiti.habits.impl.di.habitsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HabitiApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HabitiApp)
            modules(habitsModule)
        }
    }
}