package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Message

public fun Message.enrichWithCid(cid: String): Message = apply {
    replyTo?.enrichWithCid(cid)
    this.cid = cid
}
