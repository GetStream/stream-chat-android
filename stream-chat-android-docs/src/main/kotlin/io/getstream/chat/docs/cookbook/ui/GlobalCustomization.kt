package io.getstream.chat.docs.cookbook.ui

import android.graphics.Color
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#global-customization">Global Customization</a>
 */
class GlobalCustomization {

    fun usingTransformStyle() {
        TransformStyle.avatarStyleTransformer = StyleTransformer { avatarStyle ->
            avatarStyle.copy(
                onlineIndicatorColor = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.channelListStyleTransformer = StyleTransformer { channelListViewStyle ->
            channelListViewStyle.copy(
                optionsEnabled = false,
                // more overrides here
            )
        }
        TransformStyle.messageListStyleTransformer = StyleTransformer { messageListViewStyle ->
            messageListViewStyle.copy(
                backgroundColor = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.messageListItemStyleTransformer = StyleTransformer { messageListItemStyle ->
            messageListItemStyle.copy(
                messageBackgroundColorMine = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.messageInputStyleTransformer = StyleTransformer { messageInputViewStyle ->
            messageInputViewStyle.copy(
                backgroundColor = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.scrollButtonStyleTransformer = StyleTransformer { scrollButtonViewStyle ->
            scrollButtonViewStyle.copy(
                scrollButtonColor = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.viewReactionsStyleTransformer = StyleTransformer { viewReactionsViewStyle ->
            viewReactionsViewStyle.copy(
                bubbleColorMine = Color.BLUE,
                // more overrides here
            )
        }
        TransformStyle.editReactionsStyleTransformer = StyleTransformer { editReactionsViewStyle ->
            editReactionsViewStyle.copy(
                bubbleColorMine = Color.BLUE,
                // more overrides here
            )
        }
    }
}
