package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

/**
 * Intercepts [ChatApi] calls and validates [CustomObject.extraData] keys to prevent passing reserved names.
 */
internal class ExtraDataValidator(
    private val delegate: ChatApi,
) : ChatApi by delegate {

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        val reservedInChannel = extraData.findReserved<Channel>()
        val reservedInMessage = updateMessage?.findReserved() ?: emptyList()
        return when (reservedInChannel.isEmpty() && reservedInMessage.isEmpty()) {
            true -> delegate.updateChannel(channelType, channelId, extraData, updateMessage)
            else -> ErrorCall(
                ChatError(
                    message = when (reservedInMessage.isEmpty()) {
                        true -> "[ChatApi.updateChannel] 'extraData' param contains reserved keys: $reservedInChannel"
                        else -> "[ChatApi.updateChannel] 'updateMessage.extraData' property contains reserved keys: $reservedInMessage"
                    }
                )
            )
        }
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        val reserved = set.findReserved<Channel>()
        return when (reserved.isEmpty()) {
            true -> delegate.updateChannelPartial(channelType, channelId, set, unset)
            else -> ErrorCall(
                ChatError(
                    message = "[ChatApi.updateChannelPartial] 'set' param contains reserved keys: $reserved"
                )
            )
        }
    }

    override fun updateMessage(message: Message): Call<Message> {
        val reserved = message.findReserved()
        return when (reserved.isEmpty()) {
            true -> delegate.updateMessage(message)
            else -> ErrorCall(
                ChatError(
                    message = "[ChatApi.updateMessage] 'message.extraData' property contains reserved keys: $reserved"
                )
            )
        }
    }

    override fun partialUpdateMessage(messageId: String, set: Map<String, Any>, unset: List<String>): Call<Message> {
        val reserved = set.findReserved<Message>()
        return when (reserved.isEmpty()) {
            true -> delegate.partialUpdateMessage(messageId, set, unset)
            else -> ErrorCall(
                ChatError(
                    message = "[ChatApi.partialUpdateMessage] 'set' param contains reserved keys: $reserved"
                )
            )
        }
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        val (user, reserved) = users.findReserved()
        return when (user == null || reserved == null) {
            true -> delegate.updateUsers(users)
            else -> ErrorCall(
                ChatError(
                    message = "[ChatApi.updateUsers] 'user(id=${user.id}).extraData' property contains reserved keys: $reserved"
                )
            )
        }
    }

    override fun partialUpdateUser(id: String, set: Map<String, Any>, unset: List<String>): Call<User> {
        val reserved = set.findReserved<User>()
        return when (reserved.isEmpty()) {
            true -> delegate.partialUpdateUser(id, set, unset)
            else -> ErrorCall(
                ChatError(
                    message = "[ChatApi.partialUpdateUser] 'set' param contains reserved keys: $reserved"
                )
            )
        }
    }

    private fun <T : CustomObject> List<T>.findReserved(): Pair<T?, List<String>?> {
        for (obj in this) {
            val reserved = obj.findReserved()
            if (reserved.isNotEmpty()) {
                return Pair(obj, reserved)
            }
        }
        return Pair(null, null)
    }

    private fun CustomObject.findReserved(): List<String> {
        return when (this) {
            is Channel -> extraData.keys.filter(reservedInChannelPredicate)
            is Message -> extraData.keys.filter(reservedInChannelPredicate)
            is User -> extraData.keys.filter(reservedInChannelPredicate)
            else -> emptyList()
        }
    }

    private inline fun <reified T : CustomObject> Map<String, Any>.findReserved(): List<String> {
        return when (T::class) {
            Channel::class -> keys.filter(reservedInChannelPredicate)
            Message::class -> keys.filter(reservedInMessagePredicate)
            User::class -> keys.filter(reservedInUserPredicate)
            else -> emptyList()
        }
    }

    private companion object {
        private val reservedInChannelPredicate: (String) -> Boolean = { reservedInChannel.contains(it) }
        private val reservedInMessagePredicate: (String) -> Boolean = { reservedInMessage.contains(it) }
        private val reservedInUserPredicate: (String) -> Boolean = { reservedInUser.contains(it) }

        private val reservedInChannel = arrayOf(
            "cid",
            "id",
            "type",
            "created_at",
            "deleted_at",
            "updated_at",
            "member_count",
            "created_by",
            "last_message_at",
            "own_capabilities",
            "config",
        )

        private val reservedInMessage = arrayOf(
            "id",
            "cid",
            "created_at",
            "updated_at",
            "deleted_at",
        )

        private val reservedInUser = arrayOf(
            "id",
            "cid",
            "created_at",
            "updated_at",
        )
    }
}
