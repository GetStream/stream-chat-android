package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Message

public fun Message.enrichWithCid(cid: String) {
    this.cid = cid
    replyTo?.cid = cid
}
