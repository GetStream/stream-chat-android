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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiAudioRecordPlayerBinding
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

private const val PERCENTAGE = 100

/**
 * Embedded player of audio messages.
 */
internal class AudioRecordPlayerView : LinearLayoutCompat {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    internal val binding = StreamUiAudioRecordPlayerBinding.inflate(streamThemeInflater, this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        setPadding(2.dpToPx())

        background = ShapeAppearanceModelFactory.audioBackground(context)
    }

    private val logger by taggedLogger("Chat:PlayerView")

    private var totalDuration: String? = null

    /**
     * Sets total duration of audio tracker as a String. When the view goes to idle state, this is the duration show
     * to the user.
     *
     * @param duration
     */
    public fun setTotalDuration(duration: String) {
        logger.i { "[setTotalDuration] duration: $duration" }
        totalDuration = duration
        setDuration(duration)
    }

    /**
     * Sets the wave bars of the seekbar inside the view.
     *
     * @param waveBars each from 0 to 1.
     */
    public fun setWaveBars(waveBars: List<Float>) {
        logger.i { "[setWaveBars] value: $waveBars" }
        binding.audioSeekBar.waveBars = waveBars
    }

    /**
     * Sets the current duration of the audio.
     *
     * @param duration
     */
    public fun setDuration(duration: String) {
        binding.duration.run {
            text = duration
            visibility = View.VISIBLE
        }
    }

    /**
     * Sets the progress of the seekbar.
     *
     * @param progress
     */
    public fun setProgress(progress: Double) {
        binding.audioSeekBar.setProgress((progress * PERCENTAGE).toFloat())
    }

    /**
     * Sets the view into loading state.
     */
    public fun setLoading() {
        binding.loadingView.isVisible = true
        binding.playButton.isVisible = false
    }

    /**
     * Set the view into playing state.
     */
    public fun setPlaying() {
        binding.loadingView.isVisible = false
        binding.playButton.run {
            setImageResource(R.drawable.stream_ui_ic_pause)
            isVisible = true
        }
        binding.speedButton.isVisible = true
        binding.fileView.isVisible = false
    }

    /**
     * Sets the view into idle state.
     */
    public fun setIdle() {
        totalDuration?.let(::setDuration)
        binding.loadingView.isVisible = false
        binding.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_play)
        }
        setProgress(0.0)
        binding.speedButton.isVisible = false
        binding.fileView.isVisible = true
    }

    /**
     * Set sthe view into paused state.
     */
    public fun setPaused() {
        binding.loadingView.isVisible = false
        binding.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_play)
        }
        binding.speedButton.isVisible = true
        binding.fileView.isVisible = false
    }

    /**
     * The the text of the speed button.
     *
     * @param speed
     */
    public fun setSpeedText(speed: Float) {
        logger.d { "[setSpeedText] speed: $speed" }
        binding.speedButton.text = when (speed.isInt()) {
            true -> "x${speed.toInt()}"
            else -> "x$speed"
        }
    }

    /**
     * Register a callback for the play button
     *
     * @param func
     */
    public fun onPlayButtonPress(func: () -> Unit) {
        binding.playButton.setOnClickListener { func() }
    }

    /**
     * Register a callback for the speed button
     *
     * @param func
     */
    public fun onSpeedButtonPress(func: () -> Unit) {
        binding.speedButton.setOnClickListener { func() }
    }

    /**
     * Register a callback for the seekbar movement.
     *
     * @param startDrag Triggered when the drag of the seekbar starts
     * @param stopDrag Triggered when the drag of the seekbar stops
     */
    public fun onSeekbarMove(startDrag: () -> Unit, stopDrag: (Int) -> Unit) {
        binding.audioSeekBar.run {
            setOnStartDrag(startDrag)
            setOnEndDrag(stopDrag)
        }
    }
}
