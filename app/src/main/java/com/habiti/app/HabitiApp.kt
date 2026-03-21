package com.habiti.app

import android.app.Application
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresExtension
import com.habiti.ti.di.tiModule
import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.di.habitsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class HabitiApp: Application() {
    private lateinit var koin: org.koin.core.Koin
    @RequiresExtension(extension = SdkExtensions.AD_SERVICES, version = 6)
    override fun onCreate() {
        super.onCreate()
        koin = startKoin {
            androidContext(this@HabitiApp)
            modules(habitsModule, tiModule)
        }.koin

        CoroutineScope(Dispatchers.IO).launch {
            val repository = koin.get<HabitRepository>()
            repository.rescheduleAllReminders()
        }
    }
}