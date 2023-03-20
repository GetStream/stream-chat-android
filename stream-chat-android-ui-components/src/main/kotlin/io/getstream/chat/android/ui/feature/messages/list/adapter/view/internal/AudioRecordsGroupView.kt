package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.singletonList
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

    private var mediaPlayer: MediaPlayer? = null
    private var mediaPayerSet = false

    public fun showAudioAttachments(attachments: List<Attachment>) {
        removeAllViews()

        val audiosAttachment = attachments.filter { attachment -> attachment.isAudioRecording() }
        audiosAttachment.forEachIndexed(::addAttachmentPlayerView)

        mediaPlayer = MediaPlayer().apply {
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
                .let(this::setAudioAttributes)
        }
    }

    private fun addAttachmentPlayerView(index: Int, attachment: Attachment) {
        AudioRecordPlayer(context).apply {
            (attachment.extraData["duration"] as? Double)
                ?.toInt()
                ?.let(DurationParser::durationInMilliToReadableTime)
                ?.let(this::setDuration)
        }.let { playerView ->
            addView(playerView)

            playerView.setPlayCallBack {
                mediaPlayer?.run {
                    if (mediaPayerSet) {
                        seekTo(0);
                        start()
                    } else {
                        setOnPreparedListener { player ->
                            player.start()
                            playerView.setPlaying()
                        }

                        setOnCompletionListener {
                            playerView.setIdle()
                        }

                        setDataSource(attachment.assetUrl)
                        prepareAsync()
                    }
                }

                playerView.setLoading()
            }
        }
    }
}
