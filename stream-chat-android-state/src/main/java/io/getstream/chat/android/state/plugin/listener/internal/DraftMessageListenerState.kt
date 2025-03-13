package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Result

/**
 * Implementation for [DraftMessageListener] that handles state update.
 */
internal class DraftMessageListenerState(
    private val mutableGlobalState: MutableGlobalState
) : DraftMessageListener {

    /**
     * Keeps a reference of the [DraftMessage] in the [MutableGlobalState] when the request to create a draft message
     * is successful.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is created.
     * @param channelId The id of the the channel in which message is created.
     * @param message [DraftMessage] to be created.
     */
    override suspend fun onCreateDraftMessageResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        result.onSuccess { draftMessage ->
            mutableGlobalState.updateDraftMessage(draftMessage)
        }
    }

    /**
     * Removes the reference of the [DraftMessage] from the [MutableGlobalState] when the request to delete
     * a draft message is successful.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is updated.
     * @param channelId The id of the the channel in which message is updated.
     * @param message [DraftMessage] to be updated.
     */
    override suspend fun onDeleteDraftMessagesResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        result.onSuccess { draftMessage ->
            mutableGlobalState.removeDraftMessage(draftMessage)
        }
    }

    /**
     * Updates the [MutableGlobalState] with the list of [DraftMessage] when the request to query draft messages
     * is successful.
     *
     * @param result [Result] response from the original request.
     */
    override suspend fun onQueryDraftMessagesResult(result: Result<List<DraftMessage>>) {
        result.onSuccess { draftMessages ->
            draftMessages.forEach { draftMessage ->
                mutableGlobalState.updateDraftMessage(draftMessage)
            }
        }
    }
}