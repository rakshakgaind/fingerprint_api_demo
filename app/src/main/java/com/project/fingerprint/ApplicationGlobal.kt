package com.project.fingerprint

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.project.fingerprint.lifecycle.listener.AppLifecycleListener

open class ApplicationGlobal : Application() {

    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())
    }
}