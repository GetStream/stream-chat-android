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

package io.getstream.chat.android.ui.common.feature.messages.composer.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment

/**
 * Abstraction for persisting and restoring message composer state across process death.
 *
 * The controller interacts with this interface only — no Android framework imports required.
 */
@InternalStreamChatApi
public interface ComposerStateSaver {

    /**
     * Save the attachments from the composer.
     *
     * Implementations should be cheap to call frequently — this is invoked
     * on every change to the attachment list.
     */
    public fun saveAttachments(attachments: List<Attachment>)

    /**
     * Restores the attachments to the composer.
     */
    public fun restoreAttachments(): List<Attachment>?

    /**
     * Clears the stored state.
     */
    public fun clear()
}
