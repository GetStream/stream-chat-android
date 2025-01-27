/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ErrorCall
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.CustomObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope

/**
 * Intercepts [ChatApi] calls and validates [CustomObject.extraData] keys to prevent passing reserved names.
 */
@Suppress("TooManyFunctions")
internal class ExtraDataValidator(
    private val scope: CoroutineScope,
    private val delegate: ChatApi,
) : ChatApi by delegate {

    override fun updateChannel(
        channelType: String,
        channelId: String,
        extraData: Map<String, Any>,
        updateMessage: Message?,
    ): Call<Channel> {
        return delegate.updateChannel(channelType, channelId, extraData, updateMessage)
            .withExtraDataValidation(extraData)
            .withExtraDataValidation(updateMessage)
    }

    override fun updateChannelPartial(
        channelType: String,
        channelId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Channel> {
        return delegate.updateChannelPartial(channelType, channelId, set, unset)
            .withExtraDataValidation(set)
    }

    override fun updateMessage(message: Message): Call<Message> {
        return delegate.updateMessage(
            message = message,
        ).withExtraDataValidation(message.extraData)
    }

    override fun partialUpdateMessage(
        messageId: String,
        set: Map<String, Any>,
        unset: List<String>,
        skipEnrichUrl: Boolean,
    ): Call<Message> {
        return delegate.partialUpdateMessage(
            messageId = messageId,
            set = set,
            unset = unset,
            skipEnrichUrl = skipEnrichUrl,
        ).withExtraDataValidation(set)
    }

    override fun updateUsers(users: List<User>): Call<List<User>> {
        return delegate.updateUsers(users)
            .withExtraDataValidation(users)
    }

    override fun partialUpdateUser(id: String, set: Map<String, Any>, unset: List<String>): Call<List<User>> {
        return delegate.partialUpdateUser(id, set, unset)
            .withExtraDataValidationOnList(set)
    }

    override fun addMembers(
        channelType: String,
        channelId: String,
        members: List<MemberData>,
        systemMessage: Message?,
        hideHistory: Boolean?,
        skipPush: Boolean?,
    ): Call<Channel> {
        return delegate
            .addMembers(channelType, channelId, members, systemMessage, hideHistory, skipPush)
            .withExtraDataValidation(systemMessage)
            .validateMembersExtraData(members)
    }

    override fun partialUpdateMember(
        channelType: String,
        channelId: String,
        userId: String,
        set: Map<String, Any>,
        unset: List<String>,
    ): Call<Member> {
        return delegate
            .partialUpdateMember(channelType, channelId, userId, set, unset)
            .withExtraDataValidation(set)
    }

    private fun <T : CustomObject> Call<List<T>>.withExtraDataValidation(
        objects: List<T>,
    ): Call<List<T>> {
        val (obj, reserved) = objects.findReserved()
        return when (obj == null || reserved == null) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = obj.composeErrorMessage(reserved),
                ),
            )
        }
    }

    private fun <T : CustomObject, K : CustomObject> Call<T>.withExtraDataValidation(obj: K?): Call<T> {
        val reserved = obj?.findReserved() ?: emptyList()
        return when (reserved.isEmpty()) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = obj.composeErrorMessage(reserved),
                ),
            )
        }
    }

    private fun Call<Channel>.validateMembersExtraData(members: List<MemberData>): Call<Channel> {
        val (obj, reserved) = members.findReserved()
        return when (obj == null || reserved == null) {
            true -> this
            else -> ErrorCall(scope, Error.GenericError(message = obj.composeErrorMessage(reserved)))
        }
    }

    private inline fun <reified T : CustomObject> Call<T>.withExtraDataValidation(
        extraData: Map<String, Any>,
    ): Call<T> {
        val reserved = extraData.findReserved<T>()
        return when (reserved.isEmpty()) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = "'extraData' contains reserved keys: ${reserved.joinToString()}",
                ),
            )
        }
    }

    private inline fun <reified T : CustomObject> Call<List<T>>.withExtraDataValidationOnList(
        extraData: Map<String, Any>,
    ): Call<List<T>> {
        val reserved = extraData.findReserved<T>()
        return when (reserved.isEmpty()) {
            true -> this
            else -> ErrorCall(
                scope,
                Error.GenericError(
                    message = "'extraData' contains reserved keys: ${reserved.joinToString()}",
                ),
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
            is Member -> extraData.keys.filter(reservedInMemberPredicate)
            is MemberData -> extraData.keys.filter(reservedInMemberPredicate)
            else -> emptyList()
        }
    }

    private inline fun <reified T : CustomObject> Map<String, Any>.findReserved(): List<String> {
        return when (T::class) {
            Channel::class -> keys.filter(reservedInChannelPredicate)
            Message::class -> keys.filter(reservedInMessagePredicate)
            User::class -> keys.filter(reservedInUserPredicate)
            Member::class -> keys.filter(reservedInMemberPredicate)
            MemberData::class -> keys.filter(reservedInMemberPredicate)
            else -> emptyList()
        }
    }

    private fun <T : CustomObject> T?.composeErrorMessage(reserved: List<String>): String {
        return "'${resolveName()}(id=${resolveId()}" + ").extraData' contains reserved keys: ${reserved.joinToString()}"
    }

    private fun <T : CustomObject> T?.resolveName(): String {
        return when (this) {
            is Channel -> "channel"
            is Message -> "message"
            is User -> "user"
            is Member -> "member"
            is MemberData -> "member"
            else -> ""
        }
    }

    private fun <T : CustomObject> T?.resolveId(): String {
        return when (this) {
            is Channel -> id
            is Message -> id
            is User -> id
            is Member -> getUserId()
            is MemberData -> userId
            else -> ""
        }
    }

    private companion object {

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

        private val reservedInMember = setOf(
            "user_id",
            "user",
            "created_at",
            "updated_at",
            "deleted_at",
            "invited",
            "invite_accepted_at",
            "invite_rejected_at",
            "shadow_banned",
            "banned",
            "notifications_muted",
            "status",
            "ban_expires",
            "pinned_at",
            "archived_at",
        )

        private val reservedInChannelPredicate: (String) -> Boolean = reservedInChannel::contains
        private val reservedInMessagePredicate: (String) -> Boolean = reservedInMessage::contains
        private val reservedInUserPredicate: (String) -> Boolean = reservedInUser::contains
        private val reservedInMemberPredicate: (String) -> Boolean = reservedInMember::contains
    }
}
