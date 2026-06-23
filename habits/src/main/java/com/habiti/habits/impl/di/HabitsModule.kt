package com.habiti.habits.impl.di

import android.os.ext.SdkExtensions
import androidx.annotation.RequiresExtension
import com.habiti.habits.impl.data.HabitHistoryRepository
import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.data.HabitsDb
import com.habiti.habits.impl.presentation.HabitsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@RequiresExtension(extension = SdkExtensions.AD_SERVICES, version = 6)
val habitsModule = module {
    single { HabitsDb.getInstance(androidContext()) }
    single { get<HabitsDb>().habitDao() }
    single { HabitRepository(get(), androidContext()) }
    single { HabitHistoryRepository(androidContext()) }
    viewModel { HabitsViewModel(get(), get(), get(), get()) }
}