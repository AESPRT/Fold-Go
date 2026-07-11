package com.aesprt.foldgo

import android.app.Application
import com.aesprt.foldgo.core.notification.NotificationHelper
import com.aesprt.foldgo.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FoldGoApplication : Application() {
    private val notificationHelper: NotificationHelper by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FoldGoApplication)
            modules(appModule)
        }

        notificationHelper.createNotificationChannels()
    }
}
