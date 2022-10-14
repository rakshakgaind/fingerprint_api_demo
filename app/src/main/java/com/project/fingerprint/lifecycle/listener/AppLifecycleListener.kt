package com.project.fingerprint.lifecycle.listener

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.project.fingerprint.activity.MainActivity

class AppLifecycleListener : DefaultLifecycleObserver {


    override fun onStart(owner: LifecycleOwner) {
        log("onStart")
        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        log("onStop")
        MainActivity.isAuthentic = false
        super.onStop(owner)
    }

    override fun onCreate(owner: LifecycleOwner) {
        log("onCreate")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        log("onDestroy")
        super.onDestroy(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        log("onPause")
        MainActivity.isAuthentic = false
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        log("onResume")
        super.onResume(owner)
    }

    private fun log(item: String) {
        Log.d("AppLifecycleListener", "$item ")

    }

}