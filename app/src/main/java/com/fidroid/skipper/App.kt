package com.fidroid.skipper

import android.app.Application
import com.fidroid.skipper.cmt.log.ReleaseTree
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) DebugTree() else ReleaseTree())
    }
}