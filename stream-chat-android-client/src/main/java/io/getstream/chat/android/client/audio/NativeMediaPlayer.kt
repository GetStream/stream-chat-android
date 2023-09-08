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
import java.io.IOException

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
    public fun setOnErrorListener(listener: (mp: NativeMediaPlayer, what: Int, extra: Int) -> Boolean)

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public fun setOnPreparedListener(listener: () -> Unit)
}

internal class NativeMediaPlayerImpl(
    private val mediaPlayer: MediaPlayer,
) : NativeMediaPlayer {

    override var speed: Float
        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class)
        get() = mediaPlayer.playbackParams.speed

        @RequiresApi(Build.VERSION_CODES.M)
        @Throws(IllegalStateException::class, IllegalArgumentException::class)
        set(value) {
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
    override fun setDataSource(path: String) = mediaPlayer.setDataSource(path)

    @Throws(IllegalStateException::class)
    override fun prepareAsync() = mediaPlayer.prepareAsync()

    @Throws(IOException::class, IllegalStateException::class)
    override fun prepare() = mediaPlayer.prepare()

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Int) = mediaPlayer.seekTo(msec)

    @Throws(IllegalStateException::class)
    override fun start() = mediaPlayer.start()

    @Throws(IllegalStateException::class)
    override fun pause() = mediaPlayer.pause()

    @Throws(IllegalStateException::class)
    override fun stop() = mediaPlayer.stop()

    override fun reset() = mediaPlayer.reset()

    override fun release() = mediaPlayer.release()

    override fun setOnPreparedListener(listener: () -> Unit) {
        mediaPlayer.setOnPreparedListener {
            listener()
        }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer.setOnCompletionListener {
            listener()
        }
    }

    override fun setOnErrorListener(listener: (mp: NativeMediaPlayer, what: Int, extra: Int) -> Boolean) {
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            listener(NativeMediaPlayerImpl(mp), what, extra)
        }
    }
}
