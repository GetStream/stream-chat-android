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

package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.feature.messages.translations.MessageOriginalTranslationsStore
import io.getstream.chat.android.ui.common.utils.extensions.displayedName
import io.getstream.chat.android.ui.common.utils.extensions.displayedText
import io.getstream.chat.android.ui.helper.CurrentUserProvider

internal fun Poll.getTranslatedName(
    showOriginalText: Boolean = false,
    currentUserProvider: CurrentUserProvider = ChatUI.currentUserProvider,
): String = displayedName(currentUserProvider.getCurrentUser(), ChatUI.autoTranslationEnabled && !showOriginalText)

internal fun Option.getTranslatedText(
    showOriginalText: Boolean = false,
    currentUserProvider: CurrentUserProvider = ChatUI.currentUserProvider,
): String = displayedText(currentUserProvider.getCurrentUser(), ChatUI.autoTranslationEnabled && !showOriginalText)

internal fun Answer.getTranslatedText(
    showOriginalText: Boolean = false,
    currentUserProvider: CurrentUserProvider = ChatUI.currentUserProvider,
): String = displayedText(currentUserProvider.getCurrentUser(), ChatUI.autoTranslationEnabled && !showOriginalText)

/**
 * Whether [messageId] in [cid] is switched to its original text.
 */
internal fun isShowingOriginalText(cid: String, messageId: String): Boolean =
    MessageOriginalTranslationsStore.forChannel(cid).shouldShowOriginalText(messageId)
