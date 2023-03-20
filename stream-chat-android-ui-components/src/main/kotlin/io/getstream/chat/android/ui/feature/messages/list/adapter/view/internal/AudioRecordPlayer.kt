package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiAudioRecordPlayerBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class AudioRecordPlayer : LinearLayoutCompat {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val playerView =
        StreamUiAudioRecordPlayerBinding.inflate(streamThemeInflater, this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    public fun setDuration(duration: String) {
        playerView.duration.run {
            text = duration
            visibility = View.VISIBLE
        }
    }

    public fun setPlayCallBack(func: () -> Unit) {
        playerView.playButton.setOnClickListener { func() }
    }

    public fun setLoading() {
        playerView.loadingView.isVisible = true
        playerView.playButton.isVisible = false
    }

    public fun setPlaying() {
        playerView.loadingView.isVisible = false
        playerView.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_user_block)
        }
        playerView.speedButton.isVisible = true
        playerView.fileView.isVisible = false
    }

    public fun setIdle() {
        playerView.loadingView.isVisible = false
        playerView.playButton.run {
            isVisible = true
            setImageResource(R.drawable.stream_ui_ic_play)
        }
        playerView.speedButton.isVisible = false
        playerView.fileView.isVisible = true
    }
}

