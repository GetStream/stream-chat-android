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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import io.getstream.chat.android.ai.compose.ui.component.internal.DefaultComposerInputContent
import io.getstream.chat.android.ai.compose.ui.component.internal.DefaultComposerInputTrailingContent
import io.getstream.chat.android.ai.compose.ui.component.internal.DefaultComposerLeadingContent

/**
 * Factory that creates the components rendered inside the AI chat UI.
 *
 * Every slot has a default implementation, so you only override the ones you want to change.
 * Provide a custom factory through [LocalChatAiComponentFactory], usually with
 * [CompoundChatAiComponentFactory], to customize the components within a part of the
 * composition. When no factory is provided, components fall back to
 * [DefaultChatAiComponentFactory], so they work without any setup.
 */
@Suppress("ComplexInterface") // One slot per customizable part; it grows with the components.
public interface ChatAiComponentFactory {

    /**
     * The content rendered to the left of the input field in [ChatComposer].
     *
     * The default renders a button that opens the system photo picker.
     *
     * @param params The parameters for the leading content.
     */
    @Composable
    public fun RowScope.ComposerLeadingContent(params: ComposerLeadingContentParams) {
        DefaultComposerLeadingContent(params)
    }

    /**
     * The input field of [ChatComposer], including the speech-to-text and send/stop controls.
     *
     * The default renders the text field with [ComposerInputTrailingContent] (the voice button)
     * and the send/stop button.
     *
     * @param params The parameters for the input content.
     */
    @Composable
    public fun RowScope.ComposerInputContent(params: ComposerInputContentParams) {
        DefaultComposerInputContent(modifier = Modifier.weight(1f), params = params)
    }

    /**
     * The content rendered inside the input field of [ChatComposer], after the text and before the
     * send/stop button.
     *
     * The default renders a [SpeechToTextButton] that dictates into the field while no response
     * is generating. Override it to show something else there, or render nothing to leave
     * dictation out.
     *
     * @param params The parameters for the input trailing content.
     */
    @Composable
    public fun RowScope.ComposerInputTrailingContent(params: ComposerInputTrailingContentParams) {
        DefaultComposerInputTrailingContent(params)
    }

    /**
     * The content rendered to the right of the input field in [ChatComposer].
     *
     * Empty by default. Override to add custom trailing controls.
     *
     * @param params The parameters for the trailing content.
     */
    @Suppress("EmptyFunctionBlock")
    @Composable
    public fun RowScope.ComposerTrailingContent(params: ComposerTrailingContentParams) {
    }

    /**
     * The label rendered before the indicator in [AITypingIndicator].
     *
     * Empty by default. Override to provide a label applied wherever [AITypingIndicator] is used.
     *
     * @param params The parameters for the label content.
     */
    @Suppress("EmptyFunctionBlock")
    @Composable
    public fun AITypingIndicatorLabel(params: AITypingIndicatorLabelParams) {
    }

    /**
     * The animated indicator rendered in [AITypingIndicator].
     *
     * The default renders three animated dots.
     *
     * @param params The parameters for the indicator content.
     */
    @Composable
    public fun AITypingIndicatorIndicator(params: AITypingIndicatorIndicatorParams) {
        AnimatedDots(modifier = params.modifier)
    }

    /**
     * The idle content of [SpeechToTextButton], shown when not recording.
     *
     * The default renders a microphone icon button.
     *
     * @param params The parameters for the idle content.
     */
    @Composable
    public fun SpeechToTextButtonIdleContent(params: SpeechToTextButtonIdleContentParams) {
        DefaultIdleContent(onClick = params.onClick)
    }

    /**
     * The recording content of [SpeechToTextButton], shown while recording.
     *
     * The default renders animated bars that respond to the audio level.
     *
     * @param params The parameters for the recording content.
     */
    @Composable
    public fun SpeechToTextButtonRecordingContent(params: SpeechToTextButtonRecordingContentParams) {
        DefaultRecordingContent(onClick = params.onClick, rmsdB = params.rmsdB)
    }

    /**
     * A chip rendered in [AISuggestions].
     *
     * The default renders a Material 3 [androidx.compose.material3.SuggestionChip] that shows the
     * text on up to 2 lines. The row gives every chip the same height (the minimum height it
     * receives) and limits its width to [AISuggestions]'s `itemMaxWidth`.
     *
     * @param params The parameters for the chip.
     */
    @Composable
    public fun AISuggestionsChip(params: AISuggestionsChipParams) {
        DefaultAISuggestionsChip(params)
    }
}

/**
 * The default [ChatAiComponentFactory] used when no custom factory is provided.
 */
internal object DefaultChatAiComponentFactory : ChatAiComponentFactory

/**
 * The composition local that provides the current [ChatAiComponentFactory].
 *
 * Defaults to [DefaultChatAiComponentFactory] so components work without any setup.
 */
public val LocalChatAiComponentFactory: ProvidableCompositionLocal<ChatAiComponentFactory> =
    compositionLocalOf { DefaultChatAiComponentFactory }
