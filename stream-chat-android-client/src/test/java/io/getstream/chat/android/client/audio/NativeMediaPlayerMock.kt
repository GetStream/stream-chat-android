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

import androidx.media3.common.PlaybackException
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.log.taggedLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class NativeMediaPlayerMock(
    private val userScope: UserScope,
) : NativeMediaPlayer {

    private val logger by taggedLogger("NativeMediaPlayerMock")

    private var _currentPosition: Int = 0
    private var _duration: Int = Int.MAX_VALUE

    private var _onCompletionListener: (() -> Unit)? = null
    private var _onErrorListener: ((errorCode: Int) -> Boolean)? = null
    private var _onPreparedListener: (() -> Unit)? = null

    private val validStates = hashSetOf(
        NativeMediaPlayerState.PREPARED,
        NativeMediaPlayerState.STARTED,
        NativeMediaPlayerState.PAUSED,
        NativeMediaPlayerState.PLAYBACK_COMPLETED,
    )

    private var _state: NativeMediaPlayerState = NativeMediaPlayerState.IDLE
        set(value) {
            if (field == NativeMediaPlayerState.END) {
                logger.e(IllegalStateException()) { "[setState] rejected (player is released): $value" }
                return
            }
            field = value
        }

    override val state: NativeMediaPlayerState
        get() = _state

    override var speed: Float = 1.0f
        set(value) {
            if (_state !in validStates) {
                onError("[setSpeed] invalid state: $_state", 1)
                throw IllegalStateException("[setSpeed] invalid state: $_state")
            }
            field = value
        }
    override val currentPosition: Int
        get() {
            if (_state !in validStates) {
                onError("[getCurrentPosition] invalid state: $_state", 2)
            }
            return _currentPosition++
        }
    override val duration: Int
        get() {
            if (_state !in validStates) {
                onError("[getDuration] invalid state: $_state", 3)
            }
            return _duration
        }

    override fun setDataSource(path: String) {
        publishState(NativeMediaPlayerState.INITIALIZED)
    }

    override fun prepareAsync() {
        publishState(NativeMediaPlayerState.PREPARING)
        userScope.launch {
            delay(1000L)
            publishState(NativeMediaPlayerState.PREPARED)
            _onPreparedListener?.invoke()
        }
    }

    override fun seekTo(msec: Int) {
        if (_state != NativeMediaPlayerState.PREPARED &&
            _state != NativeMediaPlayerState.PAUSED &&
            _state != NativeMediaPlayerState.STARTED &&
            _state != NativeMediaPlayerState.PLAYBACK_COMPLETED
        ) {
            onError("[seekTo] invalid state: $_state", PlaybackException.ERROR_CODE_BAD_VALUE)
            return
        }
        _currentPosition = msec
    }

    override fun start() {
        if (_state != NativeMediaPlayerState.PREPARED &&
            _state != NativeMediaPlayerState.PAUSED &&
            _state != NativeMediaPlayerState.PLAYBACK_COMPLETED
        ) {
            onError("[start] invalid state: $_state", 4)
            return
        }
        publishState(NativeMediaPlayerState.STARTED)
    }

    override fun pause() {
        publishState(NativeMediaPlayerState.PAUSED)
    }

    override fun stop() {
        publishState(NativeMediaPlayerState.STOPPED)
    }

    override fun reset() {
        publishState(NativeMediaPlayerState.IDLE)
    }

    override fun release() {
        publishState(NativeMediaPlayerState.END)
    }

    fun complete() {
        onComplete()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        _onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: (errorCode: Int) -> Boolean) {
        _onErrorListener = listener
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        _onPreparedListener = listener
    }

    private fun onError(message: String, errorCode: Int = 0) {
        // logger.e { message }
        if (publishState(NativeMediaPlayerState.ERROR)) {
            _onErrorListener?.invoke(errorCode)
        }
    }

    private fun onComplete() {
        // logger.e { message }
        if (publishState(NativeMediaPlayerState.PLAYBACK_COMPLETED)) {
            _onCompletionListener?.invoke()
        }
    }

    private fun publishState(state: NativeMediaPlayerState): Boolean {
        if (_state == state) {
            return false
        }
        _state = state
        return true
    }
}
