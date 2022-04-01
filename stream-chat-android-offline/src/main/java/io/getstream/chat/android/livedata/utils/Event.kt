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
 
package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.offline.utils.Event as OfflineEvent

/**
 * Used as a wrapper for data that represents an event.
 */
public open class Event<out T> internal constructor(private val offlineEvent: OfflineEvent<T>) {

    public constructor(content: T) : this(OfflineEvent(content))

    @Suppress("MemberVisibilityCanBePrivate")
    public val hasBeenHandled: Boolean
        get() = offlineEvent.hasBeenHandled

    /**
     * Returns the content and prevents its use again.
     */
    public fun getContentIfNotHandled(): T? = offlineEvent.getContentIfNotHandled()

    /**
     * Returns the content, even if it's already been handled.
     */
    public fun peekContent(): T = offlineEvent.peekContent()
}
