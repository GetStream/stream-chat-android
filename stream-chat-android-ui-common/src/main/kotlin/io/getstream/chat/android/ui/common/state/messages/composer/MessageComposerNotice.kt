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
 * Transient feedback surfaced by the composer. Emitted into [MessageComposerState.notices] and
 * consumed by the UI layer, which typically renders each notice as a snackbar and removes it
 * via [io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController.dismissNotice]
 * once rendered.
 *
 * In v8 this hierarchy may absorb [ValidationError] so the composer exposes a single notice stream
 * for both blocking validation and informational feedback.
 */
public interface MessageComposerNotice {

    /**
     * Emitted when the user attempts to select or trigger a command that is not available for
     * the current composer [action] (e.g. tapping a moderation command while in reply mode,
     * or typing `/` while in edit mode).
     *
     * @param action The composer action that blocked the command.
     */
    public data class CommandUnavailable(
        val action: MessageAction,
    ) : MessageComposerNotice
}
