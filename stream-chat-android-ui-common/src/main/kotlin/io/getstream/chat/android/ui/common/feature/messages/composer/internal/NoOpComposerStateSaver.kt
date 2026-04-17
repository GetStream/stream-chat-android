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
 * A [ComposerStateSaver] that does nothing.
 *
 * Used as a fallback when the ViewModel is created without [CreationExtras]
 * (e.g. via the legacy [ViewModelProvider.Factory.create] overload).
 * Composer state will not survive process death in this case.
 */
@InternalStreamChatApi
public object NoOpComposerStateSaver : ComposerStateSaver {
    override fun saveAttachments(attachments: List<Attachment>): Unit = Unit
    override fun restoreAttachments(): List<Attachment>? = null
    override fun clear(): Unit = Unit
}
