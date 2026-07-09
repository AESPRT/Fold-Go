package com.aesprt.foldgo

import android.app.Application
import com.aesprt.foldgo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FoldGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FoldGoApplication)
            modules(appModule)
        }
    }
}
