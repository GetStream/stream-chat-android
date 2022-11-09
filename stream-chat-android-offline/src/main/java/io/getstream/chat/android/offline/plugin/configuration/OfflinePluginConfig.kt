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

package io.getstream.chat.android.offline.plugin.configuration

/**
 * Provides a configuration for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 *
 * @param userPresence Whether the SDK should receive user presence changes.
 */
public data class OfflinePluginConfig @JvmOverloads constructor(
    public val userPresence: Boolean = true,
)
