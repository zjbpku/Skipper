package com.fidroid.skipper.cmt.log

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.DebugTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority == Log.INFO || priority == Log.WARN || priority == Log.ERROR || priority == Log.ASSERT
    }
}