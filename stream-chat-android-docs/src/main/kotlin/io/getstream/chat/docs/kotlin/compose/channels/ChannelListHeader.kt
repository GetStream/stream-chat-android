// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.channels

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.state.extensions.globalState

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/channel-components/channel-list-header/#usage)
 */
private object ChannelListHeaderUsageSnippet {

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val user by ChatClient.instance().clientState.user.collectAsState()
                    val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()
                    ChannelListHeader(
                        modifier = Modifier.fillMaxWidth(),
                        currentUser = user,
                        title = "My Awesome App",
                        connectionState = connectionState,
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

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()
                    ChannelListHeader(
                        onHeaderActionClick = {}, // Default header action
                        onAvatarClick = {}, // Avatar click action
                        connectionState = connectionState,
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

    class ChannelsActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    val user by ChatClient.instance().clientState.user.collectAsState()
                    val connectionState by ChatClient.instance().clientState.connectionState.collectAsState()
                    ChannelListHeader(
                        // Customizing the appearance
                        modifier = Modifier.fillMaxWidth(),
                        currentUser = user,
                        title = "My Chat App",
                        connectionState = connectionState,
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
