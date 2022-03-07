package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.shuffleGiphy] calls.
 */
public interface ShuffleGiphyListener {

    /**
     * A method called after receiving the response from the shuffle Giphy call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    public suspend fun onShuffleGiphyResult(
        cid: String,
        result: Result<Message>,
    )
}
