package com.project.fingerprint.util

import android.content.Context
import com.project.fingerprint.ApplicationGlobal

class SharePrefs {
    private val sharedPreferences by lazy { ApplicationGlobal.instance.getSharedPreferences("security_pref", Context.MODE_PRIVATE) }

    fun saveData(bool: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.SharePrefs.NEED_AUTH, bool).apply()
    }

    fun getData(): Boolean {
        return sharedPreferences.getBoolean(Constants.SharePrefs.NEED_AUTH, false)
    }
}