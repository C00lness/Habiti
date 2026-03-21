package com.habiti.ti.di
import org.koin.dsl.module
import com.habiti.ti.cat.CatNamePreferences
import com.habiti.ti.impl.TiMotivatorImpl
import com.habiti.core.ai.TiMotivator
import org.koin.android.ext.koin.androidContext

val tiModule = module {
    single { CatNamePreferences.getCatName(androidContext()) }
    single<TiMotivator> { TiMotivatorImpl(androidContext(), get()) }
}