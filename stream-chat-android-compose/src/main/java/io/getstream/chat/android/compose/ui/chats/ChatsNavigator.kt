/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.chats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import io.getstream.chat.android.compose.ui.util.adaptivelayout.DefaultThreePaneNavigator
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneDestination
import io.getstream.chat.android.compose.ui.util.adaptivelayout.ThreePaneRole

/**
 * Represents the navigator that can be used to navigate between the different destination in the chat screen.
 */
public class ChatsNavigator internal constructor(internal val navigator: DefaultThreePaneNavigator) {

    /**
     * Navigates to the info pane with the provided [infoContentMode].
     */
    public fun navigateToInfoPane(infoContentMode: InfoContentMode) {
        navigator.navigateTo(ThreePaneDestination(pane = ThreePaneRole.Info, arguments = infoContentMode))
    }

    /**
     * Navigates back to the previous destination.
     */
    public fun navigateBack() {
        navigator.navigateBack()
    }

    internal companion object {
        val Saver: Saver<ChatsNavigator, Any> = Saver(
            save = { navigator -> with(DefaultThreePaneNavigator.Saver) { save(navigator.navigator) } },
            restore = { state -> DefaultThreePaneNavigator.Saver.restore(state)?.let(::ChatsNavigator) },
        )
    }
}

/**
 * Remembers a [ChatsNavigator] that can be used to navigate between the different destinations in the chat screen.
 */
@Composable
public fun rememberChatsNavigator(): ChatsNavigator = rememberSaveable(saver = ChatsNavigator.Saver) {
    ChatsNavigator(
        navigator = DefaultThreePaneNavigator(
            destinations = listOf(ThreePaneDestination<Unit>(pane = ThreePaneRole.List)),
        ),
    )
}
