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

package io.getstream.chat.android.ui.common.feature.messages.composer.typing

/**
 * Finds typing suggestions in a given input text.
 */
internal class TypingSuggester(private val options: TypingSuggestionOptions) {

    /**
     * Returns a typing suggestion if the input text matches the typing suggestion options.
     *
     * @param text The input text.
     * @param caretLocation The caret location in the input text.
     *
     * @return A typing suggestion if the input text matches the typing suggestion options, null otherwise.
     */
    internal fun typingSuggestion(text: CharSequence, caretLocation: Int = text.length): TypingSuggestion? {
        // Find the first symbol location before the input caret
        val firstSymbolBeforeCaret = text.lastIndexOf(options.symbol, startIndex = caretLocation - 1)

        // If the symbol does not exist, no typing suggestion found
        if (firstSymbolBeforeCaret == -1) {
            return null
        }

        // Only show typing suggestions after a space, or at the start of the input
        // valid examples: "@user", "Hello @user"
        // invalid examples: "Hello@user"
        val isValidPosition = firstSymbolBeforeCaret == 0 || text[firstSymbolBeforeCaret - 1].isWhitespace()
        if (!isValidPosition) {
            return null
        }

        // If suggestion is only at the start of the input, should not have text before symbol
        if (options.shouldTriggerOnlyAtStart && firstSymbolBeforeCaret != 0) {
            return null
        }

        // The suggestion range. Protect against invalid ranges.
        val suggestionStart = firstSymbolBeforeCaret + options.symbol.length
        if (caretLocation < suggestionStart) {
            return null
        }

        val suggestionText = text.substring(suggestionStart, caretLocation)
        if (suggestionText.length < options.minimumRequiredCharacters) {
            return null
        }

        return TypingSuggestion(suggestionText, suggestionStart until caretLocation)
    }
}

/**
 * Represents a typing suggestion.
 */
internal data class TypingSuggestion(
    val text: String,
    val locationRange: IntRange,
)
