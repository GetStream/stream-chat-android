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
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.internal.file.ShareableUriProvider
import java.io.File

/**
 * Activity result contract for capturing a photo or a video with a camera app.
 *
 * The caller lists the camera apps available for the [Mode] with [targets], lets the user pick one
 * when there are several, and launches the contract with the picked [Target].
 * Capture intents are launched directly rather than through `Intent.createChooser`: the Android 16
 * share sheet crashes the system process when a target without a MIME type is picked.
 *
 * Files are created in external storage directories:
 * - Photos: `{externalFilesDir}/Pictures/`
 * - Videos: `{externalFilesDir}/Movies/`
 * With fallback to cache directories if external storage is unavailable.
 *
 * After [createIntent] the caller should persist [pictureFile] and [videoFile] paths
 * so they can be restored after process death via the constructor.
 *
 * @param mode The capture mode determining what media types can be captured.
 * @param fileManager Manager for creating temporary files in external storage.
 * @param pictureFile Previously created photo file to restore after process death.
 * @param videoFile Previously created video file to restore after process death.
 * @param uriProvider Provider of the content URI the camera app writes the capture to.
 */
@InternalStreamChatApi
public class CaptureMediaContract(
    private val mode: Mode,
    private val fileManager: StreamFileManager = StreamFileManager(),
    pictureFile: File? = null,
    videoFile: File? = null,
    private val uriProvider: ShareableUriProvider = ShareableUriProvider(),
) : ActivityResultContract<CaptureMediaContract.Target, File?>() {

    /**
     * The photo destination file created by [createIntent], or restored via constructor.
     */
    @InternalStreamChatApi
    public var pictureFile: File? = pictureFile
        private set

    /**
     * The video destination file created by [createIntent], or restored via constructor.
     */
    @InternalStreamChatApi
    public var videoFile: File? = videoFile
        private set

    /**
     * Lists the camera apps that can capture the media allowed by the [Mode], photo targets first.
     */
    @InternalStreamChatApi
    public fun targets(context: Context): List<Target> = when (mode) {
        Mode.PHOTO -> queryTargets(context, Target.Kind.PHOTO)
        Mode.VIDEO -> queryTargets(context, Target.Kind.VIDEO)
        Mode.PHOTO_AND_VIDEO -> queryTargets(context, Target.Kind.PHOTO) + queryTargets(context, Target.Kind.VIDEO)
    }

    /**
     * The title to show above the list of [targets].
     */
    @InternalStreamChatApi
    public fun title(context: Context): String = context.getString(
        when (mode) {
            Mode.PHOTO -> R.string.stream_ui_message_composer_capture_media_take_photo
            Mode.VIDEO -> R.string.stream_ui_message_composer_capture_media_video
            Mode.PHOTO_AND_VIDEO -> R.string.stream_ui_message_composer_capture_media
        },
    )

    override fun createIntent(context: Context, input: Target): Intent {
        val destinationFile = when (input.kind) {
            Target.Kind.PHOTO -> fileManager.createPhotoInExternalDir(context).getOrNull()
                .also {
                    pictureFile = it
                    videoFile = null
                }
            Target.Kind.VIDEO -> fileManager.createVideoInExternalDir(context).getOrNull()
                .also {
                    videoFile = it
                    pictureFile = null
                }
        }
        return Intent(input.kind.action).apply {
            component = ComponentName(input.activityInfo.packageName, input.activityInfo.name)
            if (destinationFile != null) {
                putExtra(MediaStore.EXTRA_OUTPUT, uriProvider.getUriForFile(context, destinationFile))
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): File? =
        (pictureFile.takeIfCaptured() ?: videoFile.takeIfCaptured())
            .takeIf { resultCode == Activity.RESULT_OK }

    private fun queryTargets(context: Context, kind: Target.Kind): List<Target> =
        context.packageManager
            .queryIntentActivities(Intent(kind.action), PackageManager.MATCH_DEFAULT_ONLY)
            .map { Target(kind, it.activityInfo) }

    @InternalStreamChatApi
    public enum class Mode {
        PHOTO,
        VIDEO,
        PHOTO_AND_VIDEO,
    }

    /**
     * A camera app activity that can capture one kind of media.
     *
     * @param kind The kind of media the activity captures.
     * @param activityInfo The camera activity, also used to load its label and icon.
     */
    @InternalStreamChatApi
    public data class Target(
        val kind: Kind,
        val activityInfo: ActivityInfo,
    ) {
        @InternalStreamChatApi
        public enum class Kind(internal val action: String) {
            PHOTO(MediaStore.ACTION_IMAGE_CAPTURE),
            VIDEO(MediaStore.ACTION_VIDEO_CAPTURE),
        }
    }
}

private fun File?.takeIfCaptured(): File? = this?.takeIf { it.exists() && it.length() > 0 }
