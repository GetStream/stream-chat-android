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

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
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
    @get:RequiresApi(Build.VERSION_CODES.M)
    @set:RequiresApi(Build.VERSION_CODES.M)
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
    private val builder: () -> MediaPlayer,
) : NativeMediaPlayer {

    companion object {
        private const val DEBUG = false
    }

    private val logger by taggedLogger("Chat:NativeMediaPlayer")

    private var _mediaPlayer: MediaPlayer? = null
        set(value) {
            if (DEBUG) logger.i { "[setMediaPlayerInstance] instance: $value" }
            field = value
        }

    private val mediaPlayer: MediaPlayer get() {
        return _mediaPlayer ?: builder().also {
            _mediaPlayer = it.setupListeners()
            state = NativeMediaPlayerState.IDLE
        }
    }

    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((what: Int, extra: Int) -> Boolean)? = null
    private var onPreparedListener: (() -> Unit)? = null

    override var state: NativeMediaPlayerState = NativeMediaPlayerState.END
        set(value) {
            if (DEBUG) logger.d { "[setMediaPlayerState] state: $value <= $field" }
            field = value
        }

    override var speed: Float
        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class)
        get() = mediaPlayer.playbackParams.speed

        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class, IllegalArgumentException::class)
        set(value) {
            if (DEBUG) logger.d { "[setSpeed] speed: $value" }
            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(value)
        }
    override val currentPosition: Int
        get() = mediaPlayer.currentPosition

    override val duration: Int
        get() = mediaPlayer.duration

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class,
    )
    override fun setDataSource(path: String) {
        if (DEBUG) logger.d { "[setDataSource] path: $path" }
        mediaPlayer.setDataSource(path)
        state = NativeMediaPlayerState.INITIALIZED
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        if (DEBUG) logger.d { "[prepareAsync] no args" }
        mediaPlayer.prepareAsync()
        state = NativeMediaPlayerState.PREPARING
    }

    @Throws(IOException::class, IllegalStateException::class)
    override fun prepare() {
        if (DEBUG) logger.d { "[prepare] no args" }
        mediaPlayer.prepare()
        state = NativeMediaPlayerState.PREPARED
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Int) {
        if (DEBUG) logger.d { "[seekTo] msec: $msec" }
        mediaPlayer.seekTo(msec)
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        if (DEBUG) logger.d { "[start] no args" }
        mediaPlayer.start()
        state = NativeMediaPlayerState.STARTED
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        if (DEBUG) logger.d { "[pause] no args" }
        mediaPlayer.pause()
        state = NativeMediaPlayerState.PAUSED
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        if (DEBUG) logger.d { "[stop] no args" }
        mediaPlayer.stop()
        state = NativeMediaPlayerState.STOPPED
    }

    override fun reset() {
        if (DEBUG) logger.d { "[reset] no args" }
        mediaPlayer.reset()
        state = NativeMediaPlayerState.IDLE
    }

    override fun release() {
        if (DEBUG) logger.d { "[release] no args" }
        mediaPlayer.release()
        state = NativeMediaPlayerState.END
        _mediaPlayer = null
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

    private fun MediaPlayer.setupListeners(): MediaPlayer {
        setOnErrorListener { _, what, extra ->
            if (DEBUG) logger.e { "[onError] what: $what, extra: $extra" }
            state = NativeMediaPlayerState.ERROR
            _mediaPlayer = null
            onErrorListener?.invoke(what, extra) ?: false
        }
        setOnPreparedListener {
            if (DEBUG) logger.d { "[onPrepared] no args" }
            state = NativeMediaPlayerState.PREPARED
            onPreparedListener?.invoke()
        }
        setOnCompletionListener {
            if (DEBUG) logger.d { "[onCompletion] no args" }
            state = NativeMediaPlayerState.PLAYBACK_COMPLETED
            onCompletionListener?.invoke()
        }
        return this
    }
}
