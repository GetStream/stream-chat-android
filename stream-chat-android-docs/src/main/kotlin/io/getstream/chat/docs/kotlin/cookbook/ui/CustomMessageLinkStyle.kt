package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ReactionSortingByCount
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState

/**
 * [Custom Message Link Style](https://getstream.io/chat/docs/sdk/android/compose-cookbook/custom-message-link-style)
 */
@Composable
private fun CustomMessageLinkStyle() {
    val currentUser = User()
    val ownMessageTheme = MessageTheme.defaultOwnTheme().copy(
        linkStyle = TextStyle(
            color = Color.Red,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline
        )
    )
    val otherMessageTheme = MessageTheme.defaultOtherTheme().copy(
        linkStyle = TextStyle(
            color = Color.Green,
            fontSize = 16.sp,
            textDecoration = TextDecoration.Underline
        )
    )
    ChatTheme(
        ownMessageTheme = ownMessageTheme,
        otherMessageTheme = otherMessageTheme,
    ) {
        MessageList(
            currentState = MessageListState(
                messageItems = listOf(
                    MessageItemState(
                        currentUser = currentUser,
                        message = Message(
                            user = currentUser,
                            id = "message-1",
                            text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. www.google.com",
                        ),
                        isMine = true,
                        ownCapabilities = emptySet(),
                    ),
                    MessageItemState(
                        message = Message(
                            id = "message-2",
                            text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. getstream.io",
                        ),
                        isMine = false,
                        ownCapabilities = emptySet(),
                    ),
                ),
            ),
            reactionSorting = ReactionSortingByCount,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomMessageLinkStylePreview() {
    CustomMessageLinkStyle()
}
