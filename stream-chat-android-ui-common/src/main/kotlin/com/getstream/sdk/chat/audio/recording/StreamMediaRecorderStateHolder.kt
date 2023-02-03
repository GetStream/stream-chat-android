/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package com.getstream.sdk.chat.audio.recording

import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A wrapper class that wraps around [StreamMediaRecorder] and tracks the internal [MediaRecorder] state using
 * [StateFlow]s.
 *
 * @param streamMediaRecorder The media recorder whose state this class is tracking.
 */
public class StreamMediaRecorderStateHolder(
    private val streamMediaRecorder: StreamMediaRecorder,
) {

    /**
     * Represents the latest info state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val _onInfoStateFlow: MutableStateFlow<StreamMediaRecorderState?> = MutableStateFlow(null)

    /**
     * Represents the latest info state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    public val onInfoStateFlow: StateFlow<StreamMediaRecorderState?> = _onInfoStateFlow

    /**
     * Represents the latest error state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val _onErrorStateFlow: MutableStateFlow<StreamMediaRecorderState?> = MutableStateFlow(null)

    /**
     * Represents the latest error state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val onErrorStateFlow: StateFlow<StreamMediaRecorderState?> = MutableStateFlow(null)

    init {
        streamMediaRecorder.setOnInfoListener { streamMediaRecorder, what, extra ->
            _onInfoStateFlow.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }
        streamMediaRecorder.setOnErrorListener { streamMediaRecorder, what, extra ->
            _onErrorStateFlow.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }
    }
}
