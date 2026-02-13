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

package io.getstream.chat.android.compose.ui.theme

/**
 * Central behavioral configuration for the Chat SDK, accessible through `ChatTheme.config`.
 *
 * @param composer Configuration for the message composer behavior.
 */
public data class ChatConfig(
    val composer: ComposerConfig = ComposerConfig(),
)

/**
 * Behavioral configuration for the message composer.
 *
 * @param audioRecordingEnabled Whether the audio recording feature is enabled.
 * @param audioRecordingSendOnComplete If `true`, sends the recording on "Complete" button click.
 * If `false`, attaches it for manual sending.
 */
public data class ComposerConfig(
    val audioRecordingEnabled: Boolean = false,
    val audioRecordingSendOnComplete: Boolean = true,
)
