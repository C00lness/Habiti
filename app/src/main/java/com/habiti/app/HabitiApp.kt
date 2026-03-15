package com.habiti.app

import android.app.Application
import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.di.habitsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class HabitiApp: Application() {
    private lateinit var koin: org.koin.core.Koin
    override fun onCreate() {
        super.onCreate()
        koin = startKoin {
            androidContext(this@HabitiApp)
            modules(habitsModule)
        }.koin

        CoroutineScope(Dispatchers.IO).launch {
            val repository = koin.get<HabitRepository>()
            repository.rescheduleAllReminders()
        }
    }
}