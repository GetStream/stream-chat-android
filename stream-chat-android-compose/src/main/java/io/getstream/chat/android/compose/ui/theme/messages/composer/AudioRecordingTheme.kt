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

package io.getstream.chat.android.compose.ui.theme.messages.composer

/**
 * Represents the theme for the audio recording component.
 *
 * @property enabled If the audio recording is enabled.
 * @property sendOnComplete Sends the recording on "Complete" button click.  If false, attaches it for manual sending.
 */
public data class AudioRecordingTheme(
    val enabled: Boolean,
    val sendOnComplete: Boolean,
) {

    public companion object {
        public fun defaultTheme(): AudioRecordingTheme {
            return AudioRecordingTheme(
                enabled = false,
                sendOnComplete = true,
            )
        }
    }
}
