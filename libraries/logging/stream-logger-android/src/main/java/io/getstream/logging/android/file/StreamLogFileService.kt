/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.logging.android.file

import android.app.Service
import android.content.Intent
import android.os.IBinder

private const val ACTION_SHARE = "io.getstream.logging.android.SHARE"
private const val ACTION_CLEAR = "io.getstream.logging.android.CLEAR"

/**
 * The service handles adb commands to share/clear the log file.
 */
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
