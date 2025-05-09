package com.limsphere.pe

import android.app.Application
import android.content.ComponentCallbacks2
import android.os.Build
import android.os.StrictMode
import com.onesignal.OneSignal

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectFileUriExposure()
                    .build()
            )
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            System.gc()
            Runtime.getRuntime().gc()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
        Runtime.getRuntime().gc()
    }
} 