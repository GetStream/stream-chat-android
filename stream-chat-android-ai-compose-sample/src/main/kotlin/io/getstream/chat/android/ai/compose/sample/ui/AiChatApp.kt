/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.sample.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.ai.compose.sample.ChatDependencies
import io.getstream.chat.android.ai.compose.sample.di.ConversationListViewModelFactory
import io.getstream.chat.android.ai.compose.sample.presentation.conversations.ConversationListViewModel
import io.getstream.chat.android.ai.compose.sample.ui.chat.ChatScreen
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatDrawer
import io.getstream.chat.android.ai.compose.sample.ui.components.ViewModelStore
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.launch

/**
 * Top-level chat app composable.
 * Manages the navigation drawer and coordinates the chat UI components.
 */
@Suppress("LongMethod")
@Composable
public fun AiChatApp(
    chatDependencies: ChatDependencies,
    modifier: Modifier = Modifier,
) {
    val conversationListViewModel = viewModel<ConversationListViewModel>(factory = ConversationListViewModelFactory())
    val conversationListState by conversationListViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val user by ChatClient.instance().clientState.user.collectAsState()

    var selectedConversationId by rememberSaveable { mutableStateOf<String?>(null) }

    /**
     * Revision counter for new chats. Incremented each time "New chat" is clicked.
     * This ensures AnimatedContent detects a state change even when already on a new chat (null -> null).
     */
    var newChatRevision by rememberSaveable { mutableIntStateOf(0) }

    // Hide keyboard when drawer opens
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isOpen) {
            keyboardController?.hide()
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    var drawerSheetWidth by remember { mutableIntStateOf(0) }

    DismissibleNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    drawerSheetWidth = coordinates.size.width
                },
                drawerState = drawerState,
                windowInsets = WindowInsets(),
            ) {
                ChatDrawer(
                    user = user,
                    conversations = conversationListState.conversations,
                    selectedConversationId = selectedConversationId,
                    onNewChatClick = {
                        selectedConversationId = null
                        newChatRevision++
                        scope.launch { drawerState.close() }
                    },
                    onConversationClick = { conversationId ->
                        selectedConversationId = conversationId
                        scope.launch { drawerState.close() }
                    },
                    onUserInfoClick = {
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
    ) {
        Box {
            // Use a combined key for AnimatedContent: conversationId for real chats, revision for new chats
            // This ensures that clicking "New chat" when already on a new chat triggers a state change
            val navigationKey = selectedConversationId ?: "new-chat-$newChatRevision"

            AnimatedContent(
                targetState = navigationKey,
            ) { navigationKey ->
                ViewModelStore(navigationKey) {
                    ChatScreen(
                        modifier = Modifier.fillMaxSize(),
                        conversationId = selectedConversationId,
                        chatDependencies = chatDependencies,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNewChatClick = {
                            selectedConversationId = null
                            newChatRevision++
                        },
                        onChatDeleted = {
                            // Navigate back to new chat after deletion
                            selectedConversationId = null
                            newChatRevision++
                        },
                    )
                }
            }

            // Calculate scrim alpha based on drawer position
            // When drawer is fully open, alpha = 0.5
            // When drawer is closed, alpha = 0
            val drawerOffset = drawerState.currentOffset
            val fraction = (drawerSheetWidth + drawerOffset) / drawerSheetWidth
            val scrimAlpha = DrawerScrimAlpha * fraction

            if (scrimAlpha > 0f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha))
                        .clickable(
                            indication = null,
                            interactionSource = null,
                        ) {
                            scope.launch { drawerState.close() }
                        },
                )
            }
        }
    }
}

private const val DrawerScrimAlpha = 0.5f
