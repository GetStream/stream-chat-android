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

package io.getstream.chat.android.ui.common.feature.documents

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.internal.file.StreamShareFileManager
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.function.Consumer

/**
 * Helper class that wraps the coroutine-based share logic so it can be called from Java.
 *
 * @param lifecycleOwner The [LifecycleOwner] used to scope the share coroutine.
 */
public class AttachmentShareHelper(private val lifecycleOwner: LifecycleOwner) {

    private val shareFileManager = StreamShareFileManager()
    private var shareJob: Job? = null

    /**
     * Whether a share operation is currently in progress.
     */
    public val isInProgress: Boolean get() = shareJob?.isActive == true

    /**
     * Downloads the attachment described by the given parameters to a shareable file
     * and delivers the result via [onResult].
     *
     * @param context The Android [Context].
     * @param url The asset URL of the attachment.
     * @param mimeType The MIME type of the attachment.
     * @param name The file name of the attachment.
     * @param fileSize The file size in bytes.
     * @param onStarted Called immediately when the share operation starts.
     * @param onResult Called with the [Result] containing the shareable [Uri], or an error.
     */
    public fun share(
        context: Context,
        url: String,
        mimeType: String?,
        name: String?,
        fileSize: Int,
        onStarted: Runnable,
        onResult: Consumer<Result<Uri>>,
    ) {
        onStarted.run()
        val attachment = Attachment(
            assetUrl = url,
            mimeType = mimeType,
            name = name,
            fileSize = fileSize,
        )
        shareJob = lifecycleOwner.lifecycleScope.launch {
            val result = shareFileManager.writeAttachmentToShareableFile(context, attachment)
            onResult.accept(result)
        }
    }

    /**
     * Cancels the current share operation, if any.
     */
    public fun cancel() {
        shareJob?.cancel()
        shareJob = null
    }
}
