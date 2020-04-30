package io.getstream.chat.android.client.models

fun Channel.getUnreadMessagesCount(): Int {
    return read.sumBy {
        it.unreadMessages
    }
}