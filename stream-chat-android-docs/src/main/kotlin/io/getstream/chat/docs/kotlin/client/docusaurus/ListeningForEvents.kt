// ktlint-disable filename

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.utils.observable.Disposable

/**
 * [Listening for Events](https://getstream.io/chat/docs/sdk/android/client/guides/listening-for-events/)
 */
class ListeningForEventsSnippets {

    class ListeningForEventsSnippet1 {

        fun listeningForEvents() {
            val chatClient = ChatClient.instance()
            val channelClient = chatClient.channel("messaging", "general")

            // Subscribe for new message events
            val disposable: Disposable = channelClient.subscribeFor<NewMessageEvent> { newMessageEvent ->
                val message = newMessageEvent.message
            }

            // Dispose when you want to stop receiving events
            disposable.dispose()
        }
    }

    class ListeningForEventsSnippet2 : Fragment() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Setup chat screen components

            val chatClient = ChatClient.instance()
            val channelClient = chatClient.channel("messaging", "general")
            channelClient.subscribeFor<NotificationRemovedFromChannelEvent>(viewLifecycleOwner) { event ->
                val removedUserId = event.member.user.id
                val currentUserId = chatClient.getCurrentUser()?.id
                if (removedUserId == currentUserId) {
                    // Close the current chat screen as the current user has been removed
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}
