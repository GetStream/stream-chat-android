/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.suggestion.list

/**
 * A listener meant to be used for [SuggestionListController].
 */
public interface SuggestionListControllerListener {

    /**
     * Called when [SuggestionListUi] changes visibility.
     */
    public fun onSuggestionListUiVisibilityChanged(isVisible: Boolean)

    /**
     * Evaluates the input to see if it contains commands.
     */
    public fun containsCommands(doesContain: Boolean)

    /**
     * Evaluates the input to see if it contains mentions.
     */
    public fun containsMentions(doesContain: Boolean)
}

/**
 * The default implementation of [SuggestionListControllerListener].
 * Used to change the enabled state of the attachments button.
 *
 * @param onInputStateChanged Used to react to the input state change to enable or disable attachments.
 */
internal class DefaultSuggestionListControllerListener(private val onInputStateChanged: (shouldEnableAttachments: Boolean) -> Unit) :
    SuggestionListControllerListener {

    /**
     * Shows if the suggestion list popup is visible.
     */
    private var isSuggestionListUiVisible: Boolean = false
        set(value) {
            field = value
            onInputStateChanged(!isSuggestionListUiVisible && !inputContainsCommands)
        }

    /**
     * Shows if the input text contains commands.
     */
    private var inputContainsCommands: Boolean = false
        set(value) {
            field = value
            onInputStateChanged(!isSuggestionListUiVisible && !inputContainsCommands)
        }

    /**
     * Called when [SuggestionListUi] changes visibility.
     */
    override fun onSuggestionListUiVisibilityChanged(isVisible: Boolean) {
        isSuggestionListUiVisible = isVisible
    }

    /**
     * Evaluates the input to see if it contains commands.
     */
    override fun containsCommands(doesContain: Boolean) {
        inputContainsCommands = doesContain
    }

    /**
     * Evaluates the input to see if it contains mentions.
     */
    override fun containsMentions(doesContain: Boolean) {}
}
