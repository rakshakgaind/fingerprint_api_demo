package com.project.fingerprint

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.project.fingerprint.lifecycle.listener.AppLifecycleListener

open class ApplicationGlobal: Application(){

    var test:Int?=null

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener())

    }



}