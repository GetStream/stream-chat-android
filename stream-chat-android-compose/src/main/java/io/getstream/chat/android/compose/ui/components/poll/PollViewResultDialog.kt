/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

@file:OptIn(ExperimentalAnimationApi::class)

package io.getstream.chat.android.compose.ui.components.poll

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.common.state.messages.poll.SelectedPoll

/**
 * A dialog that should be shown if a user taps the seeing result of the votes.
 *
 * @param selectedPoll The current poll that contains all the states.
 * @param listViewModel The [MessageListViewModel] used to read state from.
 * @param onDismissRequest Handler for dismissing the dialog.
 * @param onBackPressed Handler for pressing a back button.
 */
@Composable
public fun PollViewResultDialog(
    selectedPoll: SelectedPoll?,
    listViewModel: MessageListViewModel,
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = onDismissRequest,
    ) {
        AnimatedContent(
            targetState = selectedPoll,
            transitionSpec = {
                fadeIn() + slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { fullHeight -> fullHeight / 2 },
                ) with
                    fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(animationSpec = tween(400))
            },
            label = "poll more options dialog",
        ) { currentPoll ->
            if (currentPoll != null) {
                val poll = currentPoll.poll
                val message = currentPoll.message

                BackHandler { onBackPressed.invoke() }
            }
        }
    }
}
