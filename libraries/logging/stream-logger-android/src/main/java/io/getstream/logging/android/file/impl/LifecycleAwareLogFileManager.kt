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

package io.getstream.logging.android.file.impl

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import io.getstream.logging.android.file.StreamLogFileManager
import io.getstream.logging.file.FileStreamLogger
import java.io.File

internal class LifecycleAwareLogFileManager(
    private val fileLogger: FileStreamLogger,
) : ActivityLifecycleCallbacks(), StreamLogFileManager.ShareManager, StreamLogFileManager.ClearManager {

    private val handler = Handler(Looper.getMainLooper())
    private var foregroundActivity: Activity? = null

    override fun clear() {
        fileLogger.clear()
        val activity = foregroundActivity ?: return
        Toast.makeText(activity, "Logs cleared", Toast.LENGTH_SHORT).show()
    }

    override fun share() {
        fileLogger.share { file ->
            handler.post {
                val activity = foregroundActivity ?: return@post
                activity.shareLogFile(file)
            }
        }
    }

    private fun Activity.shareLogFile(file: File) = runCatching {
        val authority = "$packageName.streamlogfileprovider"
        val uri = FileProvider.getUriForFile(this, authority, file)
        val share = Intent(Intent.ACTION_SEND)
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.type = "*/*"
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, "Share Logs"))
    }

    override fun onActivityResumed(activity: Activity) {
        foregroundActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        foregroundActivity = null
    }
}
