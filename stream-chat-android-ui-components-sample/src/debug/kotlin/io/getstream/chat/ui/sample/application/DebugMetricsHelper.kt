package io.getstream.chat.ui.sample.application

import android.os.StrictMode
import timber.log.Timber

object DebugMetricsHelper {
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
