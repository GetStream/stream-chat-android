package io.getstream.chat.android.offline.repository.domain.message.channelinfo

import io.getstream.chat.android.offline.repository.domain.message.MessageInnerEntity

/**
 * Channel information embedded within message.
 *
 * All the fields are nullable so that Room is able to distinguish when channel information is completely absent.
 * In that case, when embedded field is read ([MessageInnerEntity.channelInfo]), the embedded object is not
 * constructed and the reference is set to null.
 */
internal data class ChannelInfoEntity(
    val cid: String?,
    val id: String?,
    val type: String?,
    val memberCount: Int?,
    val name: String?,
)
