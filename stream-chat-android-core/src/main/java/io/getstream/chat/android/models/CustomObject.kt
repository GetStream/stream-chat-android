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

package io.getstream.chat.android.models

/**
 * Marks a given object as an object which contains custom data (extraData).
 */
public sealed interface CustomObject {

    /**
     * The custom key-value data associated with this object.
     */
    public val extraData: Map<String, Any>

    /**
     * Returns the value associated with the given [key], or [default] if the key is not present in the [extraData].
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T> getExtraValue(key: String, default: T): T = if (extraData.containsKey(key)) {
        extraData[key] as T
    } else {
        default
    }
}
