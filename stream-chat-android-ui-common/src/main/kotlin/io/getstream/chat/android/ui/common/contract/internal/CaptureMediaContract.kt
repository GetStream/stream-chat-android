/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.internal.file.ShareableUriProvider
import java.io.File

/**
 * Activity result contract for capturing media (photos and/or videos) using the device camera.
 *
 * Files are created in external storage directories:
 * - Photos: `{externalFilesDir}/Pictures/`
 * - Videos: `{externalFilesDir}/Movies/`
 * With fallback to cache directories if external storage is unavailable.
 *
 * [pictureFile] and [videoFile] are set during [createIntent] and used by [parseResult] to resolve
 * the captured file. After process death these references are lost; callers can restore them via
 * [restoreFilePaths] before the pending result is dispatched.
 *
 * @param mode The capture mode determining what media types can be captured.
 * @param fileManager Manager for creating temporary files in external storage.
 * @param onFilesCreated Optional callback invoked inside [createIntent] after destination files
 * are created, giving the caller an opportunity to persist the file references before the
 * external activity starts.
 */
@InternalStreamChatApi
public class CaptureMediaContract(
    private val mode: Mode,
    private val fileManager: StreamFileManager = StreamFileManager(),
    private val onFilesCreated: ((pictureFile: File?, videoFile: File?) -> Unit)? = null,
) : ActivityResultContract<Unit, File?>() {

    /**
     * The destination file for a captured photo.
     * Set by [createIntent]; can be restored externally after process death.
     */
    private var pictureFile: File? = null

    /**
     * The destination file for a captured video.
     * Set by [createIntent]; can be restored externally after process death.
     */
    private var videoFile: File? = null

    override fun createIntent(context: Context, input: Unit): Intent {
        val intents: List<Intent> = mode.intents(context)
        onFilesCreated?.invoke(pictureFile, videoFile)
        val initialIntent = intents.lastOrNull() ?: Intent()
        return Intent.createChooser(initialIntent, mode.label(context))
            .apply {
                putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    (intents - initialIntent).toTypedArray(),
                )
            }
    }

    /**
     * Restores [pictureFile] and [videoFile] from previously persisted absolute paths.
     *
     * After process death the in-memory file references are lost, so the pending
     * [ActivityResultContract] callback would be unable to locate the captured media.
     * Call this method **before** the result is dispatched (e.g. in `onCreate`) to
     * reconstruct the destination files so that [parseResult] can return the correct file.
     *
     * Passing `null` for either parameter leaves the corresponding file reference unchanged.
     *
     * @param picturePath Absolute path of the photo destination file, or `null`.
     * @param videoPath Absolute path of the video destination file, or `null`.
     */
    @InternalStreamChatApi
    public fun restoreFilePaths(picturePath: String?, videoPath: String?) {
        picturePath?.let { pictureFile = File(it) }
        videoPath?.let { videoFile = File(it) }
    }

    private fun getRecordVideoIntents(context: Context): List<Intent> {
        videoFile = fileManager.createVideoInExternalDir(context).getOrNull()
        return videoFile
            ?.let { createIntentList(context, MediaStore.ACTION_VIDEO_CAPTURE, it) }
            ?: emptyList()
    }

    private fun getTakePictureIntents(context: Context): List<Intent> {
        pictureFile = fileManager.createPhotoInExternalDir(context).getOrNull()
        return pictureFile?.let { createIntentList(context, MediaStore.ACTION_IMAGE_CAPTURE, it) }
            ?: emptyList()
    }

    private fun createIntentList(
        context: Context,
        action: String,
        destinationFile: File,
    ): List<Intent> {
        val destinationUri = ShareableUriProvider().getUriForFile(context, destinationFile)
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

    override fun parseResult(resultCode: Int, intent: Intent?): File? =
        (pictureFile.takeIfCaptured() ?: videoFile.takeIfCaptured())
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
