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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiAudioRecordPlayerBinding
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

private const val PERCENTAGE = 100

public class AudioRecordPlayer : LinearLayoutCompat {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val playerView =
        StreamUiAudioRecordPlayerBinding.inflate(streamThemeInflater, this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        setPadding(2.dpToPx())

        background = ShapeAppearanceModel.builder()
            .setAllCornerSizes(CORNER_SIZE_PX)
            .build()
            .let(::MaterialShapeDrawable)
            .apply {
                setStroke(
                    STROKE_WIDTH_PX,
                    ContextCompat.getColor(context, R.color.stream_ui_grey_whisper)
                )
                setTint(ContextCompat.getColor(context, R.color.stream_ui_white))
            }
    }

    private var totalDuration: String? = null

    public fun setTotalDuration(duration: String) {
        totalDuration = duration
        setDuration(duration)
    }

    public fun setWaveBars(waveBars: List<Float>) {
        playerView.progressBar.waveBars = waveBars
    }

    public fun setDuration(duration: String) {
        playerView.duration.run {
            text = duration
            visibility = View.VISIBLE
        }
    }

    public fun setProgress(progress: Double) {
        playerView.progressBar.setProgress((progress * PERCENTAGE).toFloat())
    }

    public fun setLoading() {
        playerView.loadingView.isVisible = true
        playerView.playButton.isVisible = false
    }

    public fun setPlaying() {
        playerView.loadingView.isVisible = false
        playerView.playButton.run {
            setImageResource(R.drawable.stream_ui_ic_user_block)
            isVisible = true
        }
        playerView.speedButton.isVisible = true
        playerView.fileView.isVisible = false
    }

    public fun setIdle() {
        totalDuration?.let(::setDuration)
        playerView.loadingView.isVisible = false
        playerView.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_play)
        }
        setProgress(0.0)
        playerView.speedButton.isVisible = false
        playerView.fileView.isVisible = true
    }

    public fun setPaused() {
        playerView.loadingView.isVisible = false
        playerView.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_play)
        }
        playerView.speedButton.isVisible = true
        playerView.fileView.isVisible = false
    }

    public fun setSpeedText(speed: Float) {
        playerView.speedButton.text = "${speed}x"
    }

    public fun onPlayButtonPress(func: () -> Unit) {
        playerView.playButton.setOnClickListener { func() }
    }

    public fun onSpeedButtonPress(func: () -> Unit) {
        playerView.speedButton.setOnClickListener { func() }
    }

    public fun onSeekbarMove(startDrag: () -> Unit, stopDrag: (Int) -> Unit) {
        playerView.progressBar.run {
            setOnStartDrag(startDrag)
            setOnEndDrag(stopDrag)
        }
    }

    private companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()
    }
}
