package io.getstream.chat.docs.cookbook.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.enums.GiphyAction
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.docs.R

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#ui-customisation">Message List View</a>
 */
class MessageListViewExample {

    private lateinit var messageListView: MessageListView

    fun setActionHandlers() {
        messageListView.setLastMessageReadHandler {
            // Handle when last message got read
        }
        messageListView.setEndRegionReachedHandler {
            // Handle when end region reached
        }
        messageListView.setMessageDeleteHandler { message: Message ->
            // Handle when message is going to be deleted
        }
        messageListView.setThreadStartHandler { message: Message ->
            // Handle when new thread for message is started
        }
        messageListView.setMessageFlagHandler { message: Message ->
            // Handle when message is going to be flagged
        }
        messageListView.setGiphySendHandler { message: Message, giphyAction: GiphyAction ->
            // Handle when some giphyAction is going to be performed
        }
        messageListView.setMessageRetryHandler { message: Message ->
            // Handle when some failed message is going to be retried
        }
        messageListView.setMessageReactionHandler { message: Message, reactionType: String ->
            // Handle when some reaction for message is going to be send
        }
        messageListView.setUserMuteHandler { user: User ->
            // Handle when a user is going to be muted
        }
        messageListView.setUserUnmuteHandler { user: User ->
            // Handle when a user is going to be unmuted
        }
        messageListView.setUserBlockHandler { user: User, cid: String ->
            // Handle when a user is going to be blocked in the channel with cid
        }
        messageListView.setMessageReplyHandler { cid: String, message: Message ->
            // Handle when message is going to be replied in the channel with cid
        }
        messageListView.setAttachmentDownloadHandler { attachment: Attachment ->
            // Handle when attachment is going to be downloaded
        }
    }

    fun customiseMessageListViewProgrammatically(context: Context) {
        TransformStyle.messageInputStyleTransformer = StyleTransformer { viewStyle ->
            viewStyle.copy(
                messageInputTextColor = ContextCompat.getColor(context, R.color.stream_ui_white)
            )
        }
    }
}
