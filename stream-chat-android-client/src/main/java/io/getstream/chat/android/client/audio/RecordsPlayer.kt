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

package io.getstream.chat.android.client.audio

public interface RecordsPlayer {

    public fun onAudioStateChange(hash: Int, func: (AudioState) -> Unit)

    public fun onProgressStateChange(hash: Int, func: (ProgressData) -> Unit)

    public fun onSpeedChange(hash: Int, func: (Float) -> Unit)

    public fun play(sourceUrl: String, audioHash: Int)

    public fun changeSpeed()

    public fun currentSpeed(): Float

    public fun dispose()
}

public data class ProgressData(public val duration: Int, public val progress: Double)

public enum class AudioState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}

public enum class PlayerState {
    UNSET, LOADING, IDLE, PLAYING;
}
