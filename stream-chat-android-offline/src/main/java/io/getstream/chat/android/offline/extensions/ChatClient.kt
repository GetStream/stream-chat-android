@file:JvmName("ChatClientExtensions")

package io.getstream.chat.android.offline.extensions

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.doOnResult
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.CreateChannelService
import io.getstream.chat.android.offline.usecase.DownloadAttachment
import io.getstream.chat.android.offline.utils.validateCid

private const val KEY_MESSAGE_ACTION = "image_action"
private const val MESSAGE_ACTION_SHUFFLE = "shuffle"
private const val MESSAGE_ACTION_SEND = "send"

/**
 * Returns the instance of [ChatDomainImpl] as cast of singleton [ChatDomain.instance] to the [ChatDomainImpl] class.
 */
private fun domainImpl(): ChatDomainImpl {
    return ChatDomain.instance as ChatDomainImpl
}

/**
 * Adds the provided channel to the active channels and replays events for all active channels.
 *
 * @return Executable async [Call] responsible for obtaining list of historical [ChatEvent] objects.
 */
@CheckResult
public fun ChatClient.replayEventsForActiveChannels(cid: String): Call<List<ChatEvent>> {
    validateCid(cid)

    val domainImpl = domainImpl()
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

    val chatDomain = domainImpl()
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
    DownloadAttachment(domainImpl()).invoke(attachment)

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

    val chatDomain = domainImpl()
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

    val chatDomain = domainImpl()
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

    val domainImpl = domainImpl()
    val channelController = domainImpl.channel(cid)
    return CoroutineCall(domainImpl.scope) {
        channelController.loadOlderMessages(messageLimit)
    }
}

/**
 *  Cancels the message of "ephemeral" type. Removes the message from local storage.
 * API call to remove the message is retried according to the retry policy specified on the chatDomain.
 *
 * @param message The `ephemeral` message to cancel.
 *
 * @return Executable async [Call] responsible for canceling ephemeral message.
 */
public fun ChatClient.cancelMessage(message: Message): Call<Boolean> {
    val cid = message.cid
    validateCid(cid)

    val domainImpl = domainImpl()
    val channelController = domainImpl.channel(cid)
    return CoroutineCall(domainImpl.scope) {
        channelController.cancelEphemeralMessage(message)
    }
}

/**
 * Creates a new channel. Will retry according to the retry policy if it fails.
 *
 * @param channel The channel object.
 *
 * @return Executable async [Call] responsible for creating a channel.
 *
 * @see io.getstream.chat.android.offline.utils.RetryPolicy
 */
@CheckResult
public fun ChatClient.createChannel(channel: Channel): Call<Channel> {
    val domainImpl = domainImpl()
    return CoroutineCall(domainImpl.scope) {
        CreateChannelService(
            scope = domainImpl.scope,
            client = this@createChannel,
            repositoryFacade = domainImpl.repos,
            getChannelController = domainImpl::channel,
            activeQueries = domainImpl.getActiveQueries(),
        ).createChannel(channel, domainImpl.isOnline(), domainImpl.user.value)
    }
}

/**
 * Checks if channel needs to be marked read.
 *
 * @param cid The full channel id i.e. "messaging:123".
 *
 * @return True if channel is needed to be marked otherwise false.
 */
internal fun ChatClient.needsMarkRead(cid: String): Boolean {
    validateCid(cid)
    val channelController = domainImpl().channel(cid)

    return channelController.markRead()
}

/**
 * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
 * Returns new "ephemeral" message with new giphy url.
 * API call to remove the message is retried according to the retry policy specified on the chatDomain.
 *
 * @param message The message to send.
 * @see io.getstream.chat.android.offline.utils.RetryPolicy
 */
internal fun ChatClient.sendGiphy(message: Message): Call<Message> {
    val domainImpl = domainImpl()

    return CoroutineCall(domainImpl.scope) {
        val cid = message.cid
        val channelController = domainImpl.channel(cid)
        val channelClient = channel(channelController.channelType, channelController.channelId)

        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SEND))
        }

        validateCid(cid)

        channelClient.sendAction(request).retry(domainImpl.scope, retryPolicy).await().also { resultMessage ->
            if (resultMessage.isSuccess) {
                channelController.removeLocalMessage(resultMessage.data())
            }
        }
    }
}

/**
 * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
 * Returns new "ephemeral" message with new giphy url.
 * API call to remove the message is retried according to the retry policy specified on the chatDomain
 *
 * @param message The message to send.
 * @see io.getstream.chat.android.offline.utils.RetryPolicy
 */
internal fun ChatClient.shuffleGiphy(message: Message): Call<Message> {
    val domainImpl = domainImpl()
    return CoroutineCall(domainImpl.scope) {
        val cid = message.cid
        val channelController = domainImpl.channel(cid)
        val channelClient = channel(channelController.channelType, channelController.channelId)

        validateCid(cid)

        val request = message.run {
            SendActionRequest(cid, id, type, mapOf(KEY_MESSAGE_ACTION to MESSAGE_ACTION_SHUFFLE))
        }

        val result = channelClient.sendAction(request).retry(domainImpl.scope, retryPolicy).await()

        if (result.isSuccess) {
            val processedMessage: Message = result.data()
            processedMessage.apply {
                syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.insertMessage(this)
            }
            channelController.upsertMessage(processedMessage)
            Result(processedMessage)
        } else {
            Result(result.error())
        }
    }
}

/**
 * Loads message for a given message id and channel id.
 *
 * @param cid The full channel id i. e. messaging:123.
 * @param messageId The id of the message.
 * @param olderMessagesOffset How many new messages to load before the requested message.
 * @param newerMessagesOffset How many new messages to load after the requested message.
 *
 * @return Executable async [Call] responsible for loading a message.
 */
@OptIn(ExperimentalStreamChatApi::class)
@CheckResult
public fun ChatClient.loadMessageById(
    cid: String,
    messageId: String,
    olderMessagesOffset: Int,
    newerMessagesOffset: Int,
): Call<Message> {
    val relevantPlugins = plugins.filterIsInstance<GetMessageListener>()
    return this.getMessage(messageId)
        .onErrorReturn(domainImpl().scope) {
            relevantPlugins.first().onGetMessageError(cid, messageId, olderMessagesOffset, newerMessagesOffset)
        }
        .doOnResult(domainImpl().scope) { result ->
            relevantPlugins.forEach {
                it.onGetMessageResult(
                    result,
                    cid,
                    messageId,
                    olderMessagesOffset,
                    newerMessagesOffset
                )
            }
        }
}
