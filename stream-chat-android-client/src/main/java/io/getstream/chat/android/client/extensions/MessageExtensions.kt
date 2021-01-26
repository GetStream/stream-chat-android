package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Message

private const val EXTRA_UPLOAD_ID: String = "uploadId"

public fun Message.enrichWithCid(cid: String): Message = apply {
    this.cid = cid
    replyTo?.cid = cid
}

public var Message.uploadId: String?
    get() = extraData[EXTRA_UPLOAD_ID] as String?
    set(value) {
        value?.let { extraData[EXTRA_UPLOAD_ID] = it }
    }

