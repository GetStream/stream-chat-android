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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * App-level role definition.
 *
 * @param name Role identifier.
 * @param custom `true` for app-defined roles, `false` for built-ins (`admin`, `user`, etc.).
 * @param scopes Scopes this role has grants in.
 * @param createdAt When the role was created.
 * @param updatedAt Last time the role was updated.
 */
@Immutable
public data class Role(
    val name: String,
    val custom: Boolean = false,
    val scopes: List<String> = emptyList(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
)
