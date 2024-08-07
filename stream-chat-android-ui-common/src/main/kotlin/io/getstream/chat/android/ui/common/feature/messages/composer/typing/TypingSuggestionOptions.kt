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

package io.getstream.chat.android.ui.common.feature.messages.composer.typing

/**
 * The options to configure the [TypingSuggester].
 *
 * @param symbol The symbol that typing suggester will use to recognise a suggestion.
 * @param shouldTriggerOnlyAtStart Whether the suggester should only be recognising at the start of the input.
 * @param minimumRequiredCharacters The minimum required characters for the suggester to start recognising a suggestion.
 */
internal data class TypingSuggestionOptions(
    val symbol: String,
    val shouldTriggerOnlyAtStart: Boolean = false,
    val minimumRequiredCharacters: Int = 0,
)
