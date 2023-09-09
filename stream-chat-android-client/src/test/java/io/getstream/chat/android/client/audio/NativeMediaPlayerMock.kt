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
    private var _onErrorListener: ((mp: NativeMediaPlayer, what: Int, extra: Int) -> Boolean)? = null
    private var _onPreparedListener: (() -> Unit)? = null

    private val validStates = hashSetOf(
        State.PREPARED,
        State.STARTED,
        State.PAUSED,
        State.PLAYBACK_COMPLETED,
    )

    private var _state: State = State.IDLE
        set(value) {
            if (field == State.END) {
                logger.e(IllegalStateException()) { "[setState] rejected (player is released): $value" }
                return
            }
            field = value
        }

    override var speed: Float = 1.0f
        set(value) {
            if (_state !in validStates) {
                onError("[setSpeed] invalid state: $_state", what = 1)
                throw IllegalStateException("[setSpeed] invalid state: $_state")
            }
            field = value
        }
    override val currentPosition: Int
        get() {
            if (_state !in validStates) {
                onError("[getCurrentPosition] invalid state: $_state", what = 2)
            }
            return _currentPosition++
        }
    override val duration: Int
        get() {
            if (_state !in validStates) {
                onError("[getDuration] invalid state: $_state", what = 3)
            }
            return _duration
        }

    override fun setDataSource(path: String) {
        publishState(State.INITIALIZED)
    }

    override fun prepare() {
        publishState(State.PREPARED)
    }

    override fun prepareAsync() {
        publishState(State.PREPARING)
        userScope.launch {
            delay(1000L)
            publishState(State.PREPARED)
            _onPreparedListener?.invoke()
        }
    }

    override fun seekTo(msec: Int) {
        _currentPosition = msec
    }

    override fun start() {
        if (_state != State.PREPARED) {
            onError("[start] invalid state: $_state", what = 4)
            return
        }
        publishState(State.STARTED)
    }

    override fun pause() {
        publishState(State.PAUSED)
    }

    override fun stop() {
        publishState(State.STOPPED)
    }

    override fun reset() {
        publishState(State.IDLE)
    }

    override fun release() {
        publishState(State.END)
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        _onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: (mp: NativeMediaPlayer, what: Int, extra: Int) -> Boolean) {
        _onErrorListener = listener
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        _onPreparedListener = listener
    }

    private fun onError(message: String, what: Int = 0, extra: Int = 0) {
        // logger.e { message }
        if (publishState(State.ERROR)) {
            _onErrorListener?.invoke(this, what, extra)
        }
    }

    private fun publishState(state: State): Boolean {
        if (_state == state) {
            return false
        }
        _state = state
        return true
    }

    internal enum class State {
        IDLE,
        PREPARING,
        PREPARED,
        INITIALIZED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        END,
        ERROR,
    }
}
