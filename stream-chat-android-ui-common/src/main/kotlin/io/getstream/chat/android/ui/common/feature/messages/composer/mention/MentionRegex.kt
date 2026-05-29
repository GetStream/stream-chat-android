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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Builds the regex used to locate a mention token (`@<display>`) inside message text.
 *
 * The boundaries use Unicode-aware lookaround, because Java's default word-boundary classes only
 * recognize ASCII `[A-Za-z0-9_]`. The lookbehind `(?<![\p{L}\p{N}_])` also naturally handles the
 * start-of-string case.
 *
 * @param display The literal display text of the mention (without the leading `@`).
 */
@InternalStreamChatApi
public fun mentionRegex(display: String): Regex =
    Regex("(?<![\\p{L}\\p{N}_])@${Regex.escape(display)}(?![\\p{L}\\p{N}_])")
