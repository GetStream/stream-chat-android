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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.taggedLogger
import java.io.IOException

@InternalStreamChatApi
public interface NativeMediaPlayer {

    public companion object {
        /**
         * Unspecified media player error.
         */
        public const val MEDIA_ERROR_UNKNOWN: Int = 1

        /**
         * Media server died. In this case, the application must release the
         * MediaPlayer object and instantiate a new one.
         */
        public const val MEDIA_ERROR_SERVER_DIED: Int = 100

        /**
         * The video is streamed and its container is not valid for progressive
         * playback i.e the video's index (e.g moov atom) is not at the start of the file.
         */
        public const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: Int = 200

        /** File or network related operation errors.  */
        public const val MEDIA_ERROR_IO: Int = -1004

        /** Bitstream is not conforming to the related coding standard or file spec.  */
        public const val MEDIA_ERROR_MALFORMED: Int = -1007

        /**
         * Bitstream is conforming to the related coding standard or file spec, but
         * the media framework does not support the feature.
         */
        public const val MEDIA_ERROR_UNSUPPORTED: Int = -1010

        /** Some operation takes too long to complete, usually more than 3-5 seconds.  */
        public const val MEDIA_ERROR_TIMED_OUT: Int = -110

        /**
         * Unspecified low-level system error. This value originated from UNKNOWN_ERROR in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_SYSTEM: Int = -2147483648

        /**
         * Unspecified low-level system error. This value originated from OK in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_OK: Int = 0

        /**
         * Unspecified low-level system error. This value originated from UNKNOWN_ERROR in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_UNKNOWN_ERROR: Int = -2147483648

        /**
         * Unspecified low-level system error. This value originated from NO_MEMORY in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_NO_MEMORY: Int = -12

        /**
         * Unspecified low-level system error. This value originated from INVALID_OPERATION in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_INVALID_OPERATION: Int = -38

        /**
         * Unspecified low-level system error. This value originated from BAD_VALUE in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_BAD_VALUE: Int = -22

        /**
         * Unspecified low-level system error. This value originated from BAD_TYPE in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_BAD_TYPE: Int = -2147483647

        /**
         * Unspecified low-level system error. This value originated from NAME_NOT_FOUND in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_NAME_NOT_FOUND: Int = -2

        /**
         * Unspecified low-level system error. This value originated from PERMISSION_DENIED in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_PERMISSION_DENIED: Int = -1

        /**
         * Unspecified low-level system error. This value originated from NO_INIT in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_NO_INIT: Int = -19

        /**
         * Unspecified low-level system error. This value originated from ALREADY_EXISTS in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_ALREADY_EXISTS: Int = -17

        /**
         * Unspecified low-level system error. This value originated from DEAD_OBJECT in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_DEAD_OBJECT: Int = -32

        /**
         * Unspecified low-level system error. This value originated from FAILED_TRANSACTION in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_FAILED_TRANSACTION: Int = -2147483646

        /**
         * Unspecified low-level system error. This value originated from JPARKS_BROKE_IT in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_JPARKS_BROKE_IT: Int = -32

        /**
         * Unspecified low-level system error. This value originated from BAD_INDEX in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_BAD_INDEX: Int = -75

        /**
         * Unspecified low-level system error. This value originated from NOT_ENOUGH_DATA in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_NOT_ENOUGH_DATA: Int = -61

        /**
         * Unspecified low-level system error. This value originated from WOULD_BLOCK in
         * system/core/include/utils/Errors.h
         */
        public const val MEDIA_ERROR_WOULD_BLOCK: Int = -11
    }

    /**
     * Retrieves/Sets the speed factor.
     * @return speed
     * @throws IllegalStateException when retrieving if speed is not set.
     * @throws IllegalStateException when setting if the internal player engine has not been
     * initialized or has been released.
     * @throws IllegalArgumentException when setting if params is not supported.
     */
    // @get:RequiresApi(Build.VERSION_CODES.M)
    // @set:RequiresApi(Build.VERSION_CODES.M)
    @get:Throws(IllegalStateException::class)
    @set:Throws(IllegalStateException::class, IllegalArgumentException::class)
    public var speed: Float

    /**
     * Gets the current player state.
     *
     * @return the current position in milliseconds
     */
    public val state: NativeMediaPlayerState

    /**
     * Gets the current playback position.
     *
     * @return the current position in milliseconds
     */
    public val currentPosition: Int

    /**
     * Gets the duration of the file.
     *
     * @return the duration in milliseconds, if no duration is available
     *         (for example, if streaming live content), -1 is returned.
     */
    public val duration: Int

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class,
    )
    public fun setDataSource(path: String)

    /**
     * Prepares the player for playback, synchronously.
     *
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For files, it is OK to call prepare(),
     * which blocks until MediaPlayer is ready for playback.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun prepare()

    /**
     * Prepares the player for playback, asynchronously.
     *
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For streams, you should call prepareAsync(),
     * which returns immediately, rather than blocking until enough data has been
     * buffered.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Throws(IllegalStateException::class)
    public fun prepareAsync()

    /**
     * Seeks to specified time position.
     *
     * @param msec the offset in milliseconds from the start to seek to
     * @throws IllegalStateException if the internal player engine has not been
     * initialized
     */
    @Throws(IllegalStateException::class)
    public fun seekTo(msec: Int)

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had
     * been stopped, or never started before, playback will start at the
     * beginning.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    @Throws(IllegalStateException::class)
    public fun start()

    /**
     * Pauses playback. Call start() to resume.
     *
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    @Throws(IllegalStateException::class)
    public fun pause()

    /**
     * Stops playback after playback has been started or paused.
     *
     * @throws IllegalStateException if the internal player engine has not been
     * initialized.
     */
    @Throws(IllegalStateException::class)
    public fun stop()

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling
     * this method, you will have to initialize it again by setting the
     * data source and calling prepare().
     */
    public fun reset()

    /**
     * Releases resources associated with this MediaPlayer object.
     *
     * <p>You must call this method once the instance is no longer required.
     */
    public fun release()

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public fun setOnCompletionListener(listener: () -> Unit)

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     *
     * Listener returns True if it handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    public fun setOnErrorListener(listener: (what: Int, extra: Int) -> Boolean)

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public fun setOnPreparedListener(listener: () -> Unit)
}

@InternalStreamChatApi
public enum class NativeMediaPlayerState {
    IDLE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTED,
    PAUSED,
    STOPPED,
    PLAYBACK_COMPLETED,
    END,
    ERROR,
}

internal class NativeMediaPlayerImpl(
    private val builder: () -> ExoPlayer,
) : NativeMediaPlayer {

    companion object {
        private const val DEBUG = false
    }

    private val logger by taggedLogger("Chat:NativeMediaPlayer")

    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((what: Int, extra: Int) -> Boolean)? = null
    private var onPreparedListener: (() -> Unit)? = null

    override var state: NativeMediaPlayerState = NativeMediaPlayerState.END
        set(value) {
            if (DEBUG) logger.d { "[setMediaPlayerState] state: $value <= $field" }
            field = value
        }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (DEBUG) logger.d { "[onPlaybackStateChanged] playbackState: $playbackState" }
            updateState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            if (DEBUG) logger.d { "[onPlayWhenReadyChanged] playWhenReady: $playWhenReady, reason: $reason" }
            updateState()
        }

        override fun onPlayerError(error: PlaybackException) {
            if (DEBUG) logger.e { "[onPlayerError] error: ${error.message}" }
            state = NativeMediaPlayerState.ERROR
            val errorCode = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE,
                PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED,
                -> NativeMediaPlayer.MEDIA_ERROR_IO

                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
                -> NativeMediaPlayer.MEDIA_ERROR_MALFORMED

                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                PlaybackException.ERROR_CODE_DECODING_FAILED,
                -> NativeMediaPlayer.MEDIA_ERROR_UNSUPPORTED

                PlaybackException.ERROR_CODE_TIMEOUT,
                -> NativeMediaPlayer.MEDIA_ERROR_TIMED_OUT

                else -> NativeMediaPlayer.MEDIA_ERROR_UNKNOWN
            }
            onErrorListener?.invoke(errorCode, 0)
        }

        private fun updateState() {
            val player = _exoPlayer ?: return
            val newState = when {
                player.playbackState == Player.STATE_IDLE -> NativeMediaPlayerState.IDLE
                player.playbackState == Player.STATE_BUFFERING -> NativeMediaPlayerState.PREPARING
                player.playbackState == Player.STATE_READY && !player.playWhenReady -> {
                    // Player is ready but paused
                    if (state == NativeMediaPlayerState.PREPARING) {
                        // This is the first time we're ready after prepare
                        NativeMediaPlayerState.PREPARED
                    } else {
                        NativeMediaPlayerState.PAUSED
                    }
                }
                player.playbackState == Player.STATE_READY && player.playWhenReady -> NativeMediaPlayerState.STARTED
                player.playbackState == Player.STATE_ENDED -> NativeMediaPlayerState.PLAYBACK_COMPLETED
                else -> state
            }

            if (newState != state) {
                val previousState = state
                state = newState

                // Trigger callbacks based on state transitions
                when (newState) {
                    NativeMediaPlayerState.PREPARED -> {
                        if (previousState == NativeMediaPlayerState.PREPARING) {
                            onPreparedListener?.invoke()
                        }
                    }
                    NativeMediaPlayerState.PLAYBACK_COMPLETED -> {
                        onCompletionListener?.invoke()
                    }
                    else -> {}
                }
            }
        }
    }

    private var _exoPlayer: ExoPlayer? = null
        set(value) {
            if (DEBUG) logger.i { "[setExoPlayerInstance] instance: $value" }
            field = value
        }

    private val exoPlayer: ExoPlayer
        get() {
            return _exoPlayer ?: builder().also {
                _exoPlayer = it.apply {
                    addListener(playerListener)
                }
                state = NativeMediaPlayerState.IDLE
            }
        }

    override var speed: Float
        @Throws(IllegalStateException::class)
        get() = exoPlayer.playbackParameters.speed

        @Throws(IllegalStateException::class, IllegalArgumentException::class)
        set(value) {
            val player = exoPlayer
            if (DEBUG) logger.d { "[setSpeed] player: ${player.hashCode()}, speed: $value" }
            player.playbackParameters = PlaybackParameters(value)
        }

    override val currentPosition: Int
        get() = exoPlayer.currentPosition.toInt()

    override val duration: Int
        get() {
            val duration = exoPlayer.duration
            return if (duration == C.TIME_UNSET) -1 else duration.toInt()
        }

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class,
    )
    override fun setDataSource(path: String) {
        val player = exoPlayer
        if (DEBUG) logger.d { "[setDataSource] player: ${player.hashCode()}, path: $path" }
        val mediaItem = MediaItem.fromUri(path)
        player.setMediaItem(mediaItem)
        state = NativeMediaPlayerState.INITIALIZED
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[prepareAsync] player: ${player.hashCode()}" }
        state = NativeMediaPlayerState.PREPARING
        player.prepare()
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun prepare() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[prepare] player: ${player.hashCode()}" }
        player.prepare()
        state = NativeMediaPlayerState.PREPARED
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Int) {
        val player = exoPlayer
        if (DEBUG) logger.d { "[seekTo] player: ${player.hashCode()}, msec: $msec" }
        player.seekTo(msec.toLong())
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[start] player: ${player.hashCode()}" }
        player.playWhenReady = true
        state = NativeMediaPlayerState.STARTED
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[pause] player: ${player.hashCode()}" }
        player.playWhenReady = false
        state = NativeMediaPlayerState.PAUSED
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[stop] player: ${player.hashCode()}" }
        player.stop()
        state = NativeMediaPlayerState.STOPPED
    }

    override fun reset() {
        val player = exoPlayer
        if (DEBUG) logger.d { "[reset] player: ${player.hashCode()}" }
        player.stop()
        player.clearMediaItems()
        state = NativeMediaPlayerState.IDLE
    }

    override fun release() {
        val player = _exoPlayer ?: run {
            if (DEBUG) logger.d { "[release] player is null" }
            return
        }
        if (DEBUG) logger.d { "[release] player: ${player.hashCode()}" }
        player.removeListener(playerListener)
        player.release()
        state = NativeMediaPlayerState.END
        _exoPlayer = null
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        if (DEBUG) logger.d { "[setOnPreparedListener] listener: $listener" }
        this.onPreparedListener = listener
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        if (DEBUG) logger.d { "[setOnCompletionListener] listener: $listener" }
        this.onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: (what: Int, extra: Int) -> Boolean) {
        if (DEBUG) logger.d { "[setOnErrorListener] listener: $listener" }
        this.onErrorListener = listener
    }
}
