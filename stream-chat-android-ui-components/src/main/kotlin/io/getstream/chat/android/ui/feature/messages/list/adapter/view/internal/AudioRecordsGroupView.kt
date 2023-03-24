package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.DurationParser
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper

public class AudioRecordsGroupView : LinearLayoutCompat {

    public constructor(context: Context) : super(context.createStreamThemeWrapper())
    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context.createStreamThemeWrapper(), attrs, defStyleAttr)

    public fun showAudioAttachments(attachments: List<Attachment>) {
        removeAllViews()

        val audiosAttachment = attachments.filter { attachment -> attachment.isAudioRecording() }
        audiosAttachment.forEach(::addAttachmentPlayerView)
    }

    private fun addAttachmentPlayerView(attachment: Attachment) {
        AudioRecordPlayer(context).apply {
            (attachment.extraData["duration"] as? Double)
                ?.toInt()
                ?.let(DurationParser::durationInMilliToReadableTime)
                ?.let(this::setDuration)
        }.let { playerView ->
            addView(playerView)

            val audioPlayer = ChatClient.instance().recordsPlayer
            val hashCode = attachment.hashCode()

            audioPlayer.onAudioStateChange(hashCode) { audioState ->
                when (audioState) {
                    AudioState.UNSET, AudioState.LOADING -> playerView.setLoading()
                    AudioState.IDLE, AudioState.PAUSE -> playerView.setIdle()
                    AudioState.PLAYING -> playerView.setPlaying()
                }
            }
            audioPlayer.onProgressStateChange(hashCode) { (duration, progress) ->
                playerView.setDuration(DurationParser.durationInMilliToReadableTime(duration))
                playerView.setProgress(progress)
            }
            audioPlayer.onSpeedChange(hashCode, playerView::setSpeedText)

            playerView.setPlayButtonCallBack {
                if (attachment.assetUrl != null) {
                    audioPlayer.play(attachment.assetUrl!!, hashCode)
                } else {
                    playerView.setLoading()
                }
            }

            playerView.setSpeedButtonCallBack {
                audioPlayer.changeSpeed()
            }
        }
    }
}
