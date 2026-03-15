package com.habiti.habits.impl.di

import com.habiti.habits.impl.data.HabitRepository
import com.habiti.habits.impl.data.HabitsDb
import com.habiti.habits.impl.presentation.HabitsScreen
import com.habiti.habits.impl.presentation.HabitsViewModel
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val habitsModule = module {
    single { HabitsDb.getInstance(androidContext()) }
    single { get<HabitsDb>().habitDao() }
    single { HabitRepository(get(), androidContext()) }
    viewModel { HabitsViewModel(get()) }
}