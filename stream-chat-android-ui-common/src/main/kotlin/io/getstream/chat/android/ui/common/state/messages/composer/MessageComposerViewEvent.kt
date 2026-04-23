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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.ui.common.state.messages.MessageAction

/**
 * One-shot side-effect events emitted by the composer. Consumers typically collect these from
 * [io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController.events]
 * and react with transient UI (e.g. a snackbar).
 */
public interface MessageComposerViewEvent {

    /**
     * Emitted when the user attempts to trigger a command that is not available for the current
     * composer [action] (e.g. tapping a moderation command while in reply mode, or typing `/`
     * while in edit mode).
     *
     * @param action The composer action that blocked the command.
     */
    public data class CommandUnavailable(
        val action: MessageAction,
    ) : MessageComposerViewEvent

    /**
     * Emitted when the user attempts to switch to [action] while an active command is in progress
     * that is not available for the target action. The user must cancel the active command before
     * the action can apply.
     *
     * @param action The composer action whose attempt was rejected.
     */
    public data class CancelCommandRequired(
        val action: MessageAction,
    ) : MessageComposerViewEvent
}
