package io.getstream.chat.android.offline.event.handler.internal.utils

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUser
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserPart
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState

/**
 * Updates [MutableGlobalState] with [SelfUser] instance.
 */
internal fun MutableGlobalState.updateCurrentUser(self: SelfUser) {
    val me = when (self) {
        is SelfUserFull -> self.me
        is SelfUserPart -> user.value?.mergePartially(self.me) ?: self.me
    }
    setUser(me)
    setBanned(me.banned)
    setBanned(me.banned)
    setMutedUsers(me.mutes)
    setChannelMutes(me.channelMutes)
    setTotalUnreadCount(me.totalUnreadCount)
    setChannelUnreadCount(me.unreadChannels)
}

/**
 * Partially merges [that] user data into [this] user data.
 */
private fun User.mergePartially(that: User): User {
    this.role = that.role
    this.createdAt = that.createdAt
    this.updatedAt = that.updatedAt
    this.lastActive = that.lastActive
    this.banned = that.banned
    this.name = that.name
    this.image = that.image
    return this
}
