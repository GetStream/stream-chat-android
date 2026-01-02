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

package io.getstream.sdk.chat.audio.recording

import android.media.MediaRecorder

/**
 * Holds information about the current state of the [MediaRecorder] used by [StreamMediaRecorder].
 * The values correspond to the info and error values found inside [MediaRecorder].
 *
 * @param streamMediaRecorder The [StreamMediaRecorder] instance this event is tied to.
 * @param what Error or info type.
 * @param extra An extra code, specific to the error or info type.
 */
public class StreamMediaRecorderState(
    private val streamMediaRecorder: StreamMediaRecorder,
    private val what: Int,
    private val extra: Int,
)
