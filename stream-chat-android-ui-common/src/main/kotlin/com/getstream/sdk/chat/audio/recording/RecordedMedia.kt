package com.getstream.sdk.chat.audio.recording

import io.getstream.chat.android.models.Attachment

public data class RecordedMedia(
    val durationInMs: Int,
    val attachment: Attachment,
)