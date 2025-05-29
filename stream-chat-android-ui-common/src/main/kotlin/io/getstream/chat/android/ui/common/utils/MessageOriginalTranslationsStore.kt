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

package io.getstream.chat.android.ui.common.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A store for managing the visibility of original translations of messages.
 *
 * This store keeps track of which messages have their original text translations shown or hidden.
 * It provides methods to toggle, show, hide, and check the visibility of original text translations.
 */
public object MessageOriginalTranslationsStore {

    /**
     * A [StateFlow] that holds the set of message IDs for which the original text translation is shown.
     * This allows observing changes to the visibility of original translations in a reactive manner.
     */
    private val _originalTextMessageIds = MutableStateFlow<Set<String>>(emptySet())
    public val originalTextMessageIds: StateFlow<Set<String>> = _originalTextMessageIds

    /**
     * Checks if the original text translation for a given message ID should be shown.
     *
     * @param messageId The ID of the message to check.
     * @return `true` if the original text translation should be shown, `false` otherwise.
     */
    public fun shouldShowOriginalText(messageId: String): Boolean {
        return _originalTextMessageIds.value.contains(messageId)
    }

    /**
     * Shows the original text for a given translated message ID.
     *
     * @param messageId The ID of the message for which to show the original text translation.
     */
    public fun showOriginalText(messageId: String) {
        _originalTextMessageIds.value = _originalTextMessageIds.value + messageId
    }

    /**
     * Hides the original text and shows the auto-translated text for a given message ID.
     *
     * @param messageId The ID of the message for which to hide the original text translation.
     */
    public fun hideOriginalText(messageId: String) {
        _originalTextMessageIds.value = _originalTextMessageIds.value - messageId
    }

    /**
     * Toggles the visibility of the original text translation for a given message ID.
     * If the original text is currently shown, it will be hidden, and vice versa.
     *
     * @param messageId The ID of the message for which to toggle the original text translation visibility.
     */
    public fun toggleOriginalText(messageId: String) {
        if (shouldShowOriginalText(messageId)) {
            hideOriginalText(messageId)
        } else {
            showOriginalText(messageId)
        }
    }

    /**
     * Clears all stored original text message IDs, effectively hiding all original texts.
     */
    public fun clear() {
        _originalTextMessageIds.value = emptySet()
    }
}
