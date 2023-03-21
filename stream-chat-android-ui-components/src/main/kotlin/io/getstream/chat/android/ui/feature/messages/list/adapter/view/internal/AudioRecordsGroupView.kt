package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.StreamAudioPlayer
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

    private lateinit var audioPlayer: StreamAudioPlayer

    public fun showAudioAttachments(attachments: List<Attachment>) {
        removeAllViews()

        audioPlayer = StreamAudioPlayer(
            MediaPlayer().apply {
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                    .let(this::setAudioAttributes)
            }
        )

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

            audioPlayer.onAudioStateChange(attachment.hashCode().toString()) { audioState ->
                when (audioState) {
                    AudioState.UNSET, AudioState.LOADING -> playerView.setLoading()
                    AudioState.IDLE -> playerView.setIdle()
                    AudioState.PLAYING -> playerView.setPlaying()
                }
            }

            playerView.setPlayCallBack {
                if (attachment.assetUrl != null) {
                    audioPlayer.play(attachment.assetUrl!!)
                } else {
                    playerView.setLoading()
                }
            }
        }
    }
}
