package com.example.ti.di
import org.koin.dsl.module
import com.example.ti.TiMotivator
import org.koin.android.ext.koin.androidContext

val tiModule = module {
    single { TiMotivator.apply{
        init(androidContext())
    } }
}