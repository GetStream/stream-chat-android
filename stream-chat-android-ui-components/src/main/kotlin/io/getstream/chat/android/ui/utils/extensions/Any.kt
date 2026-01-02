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

package io.getstream.chat.android.ui.common.extensions.internal

internal inline fun <reified AnyT> Any.cast() = this as AnyT

internal inline fun <reified AnyT> Any.safeCast() = this as? AnyT

// Just a way to abstract the elvis operator into a method for chaining
internal fun <AnyT> AnyT?.getOrDefault(default: AnyT): AnyT = this ?: default

internal fun <AnyT> AnyT?.isNotNull() = this != null

internal fun <AnyT> AnyT.singletonList() = listOf(this)
