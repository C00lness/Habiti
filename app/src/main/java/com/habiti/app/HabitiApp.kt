package com.habiti.app

import android.app.Application

class HabitiApp: Application() {
    override fun onCreate() {
        super.onCreate()
//        startKoin {
//            androidContext(this@HabitiApp)
//            modules(coreModule, HabitsModule)
//        }
    }
}