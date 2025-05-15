/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.helper

import androidx.core.net.toUri
import io.getstream.chat.android.models.Attachment

/**
 * Interface for providing the URI of an audio recording attachment.
 */
public fun interface StreamAudioRecordingUriProvider {

    /**
     * Returns the URI for the audio recording attachment.
     *
     * @param attachment The attachment containing the audio recording.
     */
    public fun getAudioRecordingUri(attachment: Attachment): String
}

/**
 * Default implementation of [StreamAudioRecordingUriProvider]
 * that returns the asset URL or upload URI of the attachment.
 */
public object DefaultAudioRecordingUriProvider : StreamAudioRecordingUriProvider {
    override fun getAudioRecordingUri(attachment: Attachment): String =
        attachment.assetUrl ?: attachment.upload?.toUri()?.toString().orEmpty()
}
