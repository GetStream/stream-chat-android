package io.getstream.chat.docs.kotlin.ui.guides

import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle

/**
 * [Customizing MessageListView Scrolling Behavior](https://getstream.io/chat/docs/sdk/android/ui/guides/customizing-message-list-scroll-behavior/)
 */
class CustomizingScrollBehavior {

    fun customization() {
        TransformStyle.messageListStyleTransformer = StyleTransformer { defaultViewStyle ->
            defaultViewStyle.copy(
                disableScrollWhenShowingDialog = false
            )
        }
    }
}
