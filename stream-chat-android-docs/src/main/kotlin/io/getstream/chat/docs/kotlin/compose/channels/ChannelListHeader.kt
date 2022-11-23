// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.state.extensions.globalState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list-header/#usage)
 */
private object ChannelListHeaderUsageSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val user = ChatClient.instance().globalState.user.value
            setContent {
                ChatTheme {
                    ChannelListHeader(
                        modifier = Modifier.fillMaxWidth(),
                        currentUser = user,
                        title = "My Awesome App"
                    )
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list-header/#handling-actions)
 */
private object ChannelListHeaderHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    ChannelListHeader(
                        onHeaderActionClick = {}, // Default header action
                        onAvatarClick = {} // Avatar click action
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list-header/#customization)
 */
private object ChannelListHeaderCustomizationSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val user = ChatClient.instance().globalState.user.value
            setContent {
                ChatTheme {
                    ChannelListHeader(
                        // Customizing the appearance
                        modifier = Modifier.fillMaxWidth(),
                        currentUser = user,
                        title = "My Chat App",
                        connectionState = ConnectionState.CONNECTED,
                        trailingContent = { // Customizing the trailing action
                            Icon(
                                modifier = Modifier.clickable {
                                    // Click handler for the custom action
                                },
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = ChatTheme.colors.textHighEmphasis
                            )
                        }
                    )
                }
            }
        }
    }
}
