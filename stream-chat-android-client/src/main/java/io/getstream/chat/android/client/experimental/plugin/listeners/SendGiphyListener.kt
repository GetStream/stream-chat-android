package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.sendGiphy] calls.
 */
public interface SendGiphyListener {

    /**
     * A method called after receiving the response from the send Giphy call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    public fun onGiphySendResult(
        cid: String,
        result: Result<Message>,
    )
}
