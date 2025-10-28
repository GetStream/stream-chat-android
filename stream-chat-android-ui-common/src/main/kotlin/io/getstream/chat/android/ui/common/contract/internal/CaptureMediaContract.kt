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

package io.getstream.chat.android.ui.common.contract.internal

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

@InternalStreamChatApi
public class CaptureMediaContract(private val mode: Mode) : ActivityResultContract<Unit, File?>() {

    private var pictureFile: File? = null
    private var videoFile: File? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        val intents: List<Intent> = mode.intents(context)
        val initialIntent = intents.lastOrNull() ?: Intent()
        return Intent.createChooser(initialIntent, mode.label(context))
            .apply {
                putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    (intents - initialIntent).toTypedArray(),
                )
            }
    }

    private fun getRecordVideoIntents(context: Context): List<Intent> = File(
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.cacheDir,
        createFileName("STREAM_VID", "mp4"),
    ).let {
        videoFile = it
        createIntentList(context, MediaStore.ACTION_VIDEO_CAPTURE, it)
    }

    private fun getTakePictureIntents(context: Context): List<Intent> = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
        createFileName("STREAM_IMG", "jpg"),
    ).let {
        pictureFile = it
        createIntentList(context, MediaStore.ACTION_IMAGE_CAPTURE, it)
    }

    private fun createFileName(prefix: String, extension: String) = "${prefix}_${dateFormat.format(Date().time)}.$extension"

    private fun createIntentList(
        context: Context,
        action: String,
        destinationFile: File,
    ): List<Intent> {
        val destinationUri = StreamFileUtil.getUriForFile(
            context,
            destinationFile,
        )
        val actionIntent = Intent(action)
        return context.packageManager.queryIntentActivities(
            actionIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        ).map {
            Intent(actionIntent).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, destinationUri)
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
                `package` = it.activityInfo.packageName
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): File? = (pictureFile.takeIfCaptured() ?: videoFile.takeIfCaptured())
        .takeIf { resultCode == Activity.RESULT_OK }

    @InternalStreamChatApi
    public enum class Mode {
        PHOTO,
        VIDEO,
        PHOTO_AND_VIDEO,
    }

    private fun Mode.intents(context: Context): List<Intent> = when (this) {
        Mode.PHOTO -> getTakePictureIntents(context)
        Mode.VIDEO -> getRecordVideoIntents(context)
        Mode.PHOTO_AND_VIDEO -> getTakePictureIntents(context) + getRecordVideoIntents(context)
    }

    private fun Mode.label(context: Context): String = context.getString(
        when (this) {
            Mode.PHOTO -> R.string.stream_ui_message_composer_capture_media_take_photo
            Mode.VIDEO -> R.string.stream_ui_message_composer_capture_media_video
            Mode.PHOTO_AND_VIDEO -> R.string.stream_ui_message_composer_capture_media
        },
    )
}

private fun File?.takeIfCaptured(): File? = this?.takeIf { it.exists() && it.length() > 0 }
