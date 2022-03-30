package io.getstream.logging.android.file

import android.app.Service
import android.content.Intent
import android.os.IBinder

private const val ACTION_SHARE = "io.getstream.logging.android.SHARE"
private const val ACTION_CLEAR = "io.getstream.logging.android.CLEAR"

public class StreamLogFileService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHARE -> StreamLogFileManager.share()
            ACTION_CLEAR -> StreamLogFileManager.clear()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
