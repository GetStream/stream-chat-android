package io.getstream.chat.ui.sample.application

import android.os.StrictMode

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
    }
}
