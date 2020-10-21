package io.getstream.chat.sample.application

import android.os.StrictMode
import timber.log.Timber

class DebugMetricsHelper {
    internal fun init() {
        StrictMode.ThreadPolicy.Builder().detectAll()
            .permitDiskReads()
            .permitDiskWrites()
            .penaltyLog()
            .build()
            .apply {
                StrictMode.setThreadPolicy(this)
            }

        Timber.plant(Timber.DebugTree())
    }
}
