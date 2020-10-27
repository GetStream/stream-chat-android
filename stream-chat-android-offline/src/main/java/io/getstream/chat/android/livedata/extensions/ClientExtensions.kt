package io.getstream.chat.android.livedata.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

private const val EQUAL_ON_COMPARISON = 0

internal fun Message.users(): List<User> = latestReactions.mapNotNull(Reaction::user) + user

internal fun Channel.users(): List<User> = members.map(Member::user) +
    read.map(ChannelUserRead::user) +
    createdBy +
    messages.flatMap { it.users() }

internal fun Message.addReaction(reaction: Reaction, isMine: Boolean) {

    // add to own reactions
    if (isMine) {
        this.ownReactions.add(reaction)
    }

    // add to latest reactions
    this.latestReactions.add(reaction)

    // update the count
    val currentCount = this.reactionCounts.getOrElse(reaction.type) { 0 }
    // copy the object so livedata's diffutils can notice a change
    this.reactionCounts = this.reactionCounts.toMutableMap()
    this.reactionCounts[reaction.type] = currentCount + 1
    // update the score
    val currentScore = this.reactionScores.getOrElse(reaction.type) { 0 }
    this.reactionScores = this.reactionScores.toMutableMap()
    this.reactionScores[reaction.type] = currentScore + reaction.score
}

internal fun Message.removeReaction(reaction: Reaction, updateCounts: Boolean) {

    val countBeforeFilter = ownReactions.size + latestReactions.size
    ownReactions = ownReactions.filterNot { it.type == reaction.type && it.userId == reaction.userId }.toMutableList()
    latestReactions =
        latestReactions.filterNot { it.type == reaction.type && it.userId == reaction.userId }.toMutableList()
    val countAfterFilter = ownReactions.size + latestReactions.size

    if (updateCounts) {
        val shouldDecrement = countBeforeFilter > countAfterFilter || this.latestReactions.size >= 15
        if (shouldDecrement) {
            this.reactionCounts = this.reactionCounts.toMutableMap()
            val currentCount = this.reactionCounts.getOrElse(reaction.type) { 1 }
            val newCount = currentCount - 1
            this.reactionCounts[reaction.type] = newCount
            if (newCount <= 0) {
                reactionCounts.remove(reaction.type)
            }
            this.reactionScores = this.reactionScores.toMutableMap()
            val currentScore = this.reactionScores.getOrElse(reaction.type) { 1 }
            val newScore = currentScore - reaction.score
            this.reactionScores[reaction.type] = newScore
            if (newScore <= 0) {
                reactionScores.remove(reaction.type)
            }
        }
    }
}

internal val Channel.lastMessage: Message?
    get() = messages.lastOrNull()

internal fun Channel.updateLastMessage(message: Message) {
    val createdAt = message.createdAt ?: message.createdLocallyAt
    val messageCreatedAt = checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

    val updateNeeded = message.id == lastMessage?.id
    val newLastMessage = lastMessageAt == null || messageCreatedAt.after(lastMessageAt)
    if (newLastMessage || updateNeeded) {
        lastMessageAt = messageCreatedAt
        messages = messages + message
    }
}

internal fun Channel.setMember(userId: String, member: Member?) {
    if (member == null) {
        members.firstOrNull { it.user.id == userId }?.also { foundMember ->
            members = members - foundMember
        }
    } else {
        members = members + member
    }
}

internal fun Channel.updateReads(newRead: ChannelUserRead) {
    val oldRead = read.firstOrNull { it.user == newRead.user }
    read = if (oldRead != null) {
        read - oldRead + newRead
    } else {
        read + newRead
    }
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_TIMEOUT = 408
private const val NETWORK_NOT_AVAILABLE = -1

/**
 * Returns true if an error is a permanent failure instead of a temporary one (broken network, 500, rate limit etc.)
 *
 * A permanent error is an error returned by Stream's API (IE a validation error on the input)
 * Any permanent error will always have a stream error code
 *
 * Temporary errors are retried. Network not being available is a common example of a temporary error.
 *
 * See the error codes here
 * https://getstream.io/chat/docs/api_errors_response/?language=js
 */
public fun ChatError.isPermanent(): Boolean {
    var isPermanent = false
    if (this is ChatNetworkError) {
        val networkError: ChatNetworkError = this
        // stream errors are mostly permanent. the exception to this are the rate limit and timeout error
        val temporaryStreamErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT)
        if (networkError.streamCode > 0) {
            isPermanent = true
            if (networkError.statusCode in temporaryStreamErrors) {
                isPermanent = false
            }
        }
    }
    return isPermanent
}

public fun ChatClient.domain(): ChatDomain {

    // The builder/config logic happens at the ChatClient level
    val domainBuilder = ChatDomain.Builder(appContext, this).setConfig(offlineConfig)

    // We use listeners to tie into the ChatClient's setUser and disconnect flow
    //
    // // hookup listeners for pre set User and disconnect()
    // setPreUserConnectListener {
    //     domainBuilder.setUser(it).build()
    // }
    //
    // setDisconnectListener {
    //     GlobalScope.launch { domain.disconnect() }
    // }
    return ChatDomain.instance()
}

internal fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> =
    sortedWith(pagination.sort.comparator).drop(pagination.channelOffset).take(pagination.channelLimit)

internal val QuerySort.comparator: Comparator<in Channel>
    get() =
        CompositeComparator(data.mapNotNull { it.comparator as? Comparator<Channel> })

private val snakeRegex = "_[a-zA-Z]".toRegex()

/**
 * turns created_at into createdAt
 */
internal fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .toUpperCase()
    }
}

internal val Map<String, Any>.comparator: Comparator<in Channel>?
    get() =
        (this["field"] as? String)?.let { fieldName ->
            (this["direction"] as? Int)?.let { sortDirection ->
                Channel::class.declaredMemberProperties
                    .find { it.name == fieldName.snakeToLowerCamelCase() }
                    ?.comparator(sortDirection)
            }
        }

internal fun KProperty1<Channel, *>?.comparator(sortDirection: Int): Comparator<Channel>? =
    this?.let { compareProperty ->
        Comparator { c0, c1 ->
            (compareProperty.getter.call(c0) as? Comparable<Any>)?.let { a ->
                (compareProperty.getter.call(c1) as? Comparable<Any>)?.let { b ->
                    a.compareTo(b) * sortDirection
                }
            } ?: EQUAL_ON_COMPARISON
        }
    }

internal class CompositeComparator<T>(private val comparators: List<Comparator<T>>) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int =
        comparators.fold(EQUAL_ON_COMPARISON) { currentComparisonValue, comparator ->
            when (currentComparisonValue) {
                EQUAL_ON_COMPARISON -> comparator.compare(o1, o2)
                else -> currentComparisonValue
            }
        }
}

internal fun String?.isImageMimetype() = this?.contains("image") ?: false
