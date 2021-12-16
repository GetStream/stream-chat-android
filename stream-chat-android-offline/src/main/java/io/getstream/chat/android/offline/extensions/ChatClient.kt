@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.offline.extensions

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.usecase.DownloadAttachment
import io.getstream.chat.android.offline.utils.validateCid

/**
 * Query members of a channel.
 *
 * @param cid CID of the Channel whose members we are querying.
 * @param offset Indicates how many items to exclude from the start of the result.
 * @param limit Indicates the maximum allowed number of items in the result.
 * @param filter Filter applied to online queries for advanced selection criteria.
 * @param sort The sort criteria applied to the result.
 * @param members
 *
 * @return Executable async [Call] querying members.
 */
@JvmOverloads
@CheckResult
public fun ChatClient.requestMembers(
    cid: String,
    offset: Int = 0,
    limit: Int = 0,
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySort<Member> = QuerySort.desc(Member::createdAt),
    members: List<Member> = emptyList(),
): Call<List<Member>> = ChatDomain.instance().queryMembers(cid, offset, limit, filter, sort, members)

/**
 * Perform api request with a search string as autocomplete if in online state. Otherwise performs search by name
 * in local database.
 *
 * @param querySearch Search string used as autocomplete.
 * @param offset Offset for paginated requests.
 * @param userLimit The page size in the request.
 * @param userPresence Presence flag to obtain additional info such as last active date.
 *
 * @return Executable async [Call] querying users.
 */
@CheckResult
public fun ChatClient.searchUsersByName(
    querySearch: String,
    offset: Int,
    userLimit: Int,
    userPresence: Boolean,
): Call<List<User>> = ChatDomain.instance().searchUsersByName(querySearch, offset, userLimit, userPresence)

/**
 * Adds the provided channel to the active channels and replays events for all active channels.
 *
 * @return Executable async [Call] responsible for obtaining list of historical [ChatEvent] objects.
 */
@CheckResult
public fun ChatClient.replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>> {
    validateCid(cid)

    val domainImpl = ChatDomain.instance() as ChatDomainImpl
    return CoroutineCall(domainImpl.scope) {
        domainImpl.replayEvents(cid)
    }
}

/**
 * Set the reply state for the channel.
 *
 * @param cid CID of the channel where reply state is being set.
 * @param message The message we want reply to. The null value means dismiss reply state.
 *
 * @return Executable async [Call].
 */
@CheckResult
public fun ChatClient.setMessageForReply(cid: String, message: Message?): Call<Unit> {
    validateCid(cid)

    val chatDomain = ChatDomain.instance() as ChatDomainImpl
    val channelController = chatDomain.channel(cid)
    return CoroutineCall(chatDomain.scope) {
        channelController.replyMessage(message)
        Result(Unit)
    }
}

/**
 * Downloads the selected attachment to the "Download" folder in the public external storage directory.
 *
 * @param attachment The attachment to download.
 *
 * @return Executable async [Call] downloading attachment.
 */
@CheckResult
public fun ChatClient.downloadAttachment(attachment: Attachment): Call<Unit> =
    DownloadAttachment(ChatDomain.instance() as ChatDomainImpl).invoke(attachment)

/**
 * Keystroke should be called whenever a user enters text into the message input.
 * It automatically calls stopTyping when the user stops typing after 5 seconds.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
 *
 * @return Executable async [Call] which completes with [Result] having data true when a typing event was sent, false if it wasn't sent.
 */
@CheckResult
public fun ChatClient.keystroke(cid: String, parentId: String? = null): Call<Boolean> {
    validateCid(cid)

    val chatDomain = ChatDomain.instance() as ChatDomainImpl
    val channelController = chatDomain.channel(cid)
    return CoroutineCall(chatDomain.scope) {
        channelController.keystroke(parentId)
    }
}

/**
 * StopTyping should be called when the user submits the text and finishes typing.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param parentId Set this field to `message.id` to indicate that typing event is happening in a thread.
 *
 * @return Executable async [Call] which completes with [Result] having data equal true when a typing event was sent,
 * false if it wasn't sent.
 */
@CheckResult
public fun ChatClient.stopTyping(cid: String, parentId: String? = null): Call<Boolean> {
    validateCid(cid)

    val chatDomain = ChatDomain.instance() as ChatDomainImpl
    val channelController = chatDomain.channel(cid)
    return CoroutineCall(chatDomain.scope) {
        channelController.stopTyping(parentId)
    }
}

/**
 * Loads older messages for the channel.
 *
 * @param cid The full channel id i.e. "messaging:123".
 * @param messageLimit How many new messages to load.
 *
 * @return The channel wrapped in [Call]. This channel contains older requested messages.
 */
public fun ChatClient.loadOlderMessages(cid: String, messageLimit: Int): Call<Channel> {
    validateCid(cid)

    val domainImpl = ChatDomain.instance as ChatDomainImpl
    val channelController = domainImpl.channel(cid)
    return CoroutineCall(domainImpl.scope) {
        channelController.loadOlderMessages(messageLimit)
    }
}
