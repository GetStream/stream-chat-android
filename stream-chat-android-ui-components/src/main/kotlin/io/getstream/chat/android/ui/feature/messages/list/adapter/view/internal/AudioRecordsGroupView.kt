package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment

public class AudioRecordsGroupView : LinearLayoutCompat {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    public fun showAudioAttachments(attachments: List<Attachment>) {
        attachments.filter { attachment -> attachment.isAudioRecording() }
            .forEach(::addAttachmentPlayerView)
    }

    private fun addAttachmentPlayerView(attachment: Attachment) {
        AudioRecordPlayer(context).apply {
            setDuration((attachment.extraData["duration"] as? String) ?: "4:19")
        }.let(::addView)
    }

}
