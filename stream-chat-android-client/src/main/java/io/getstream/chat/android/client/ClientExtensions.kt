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

package io.getstream.chat.android.client

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import kotlin.reflect.KClass

/**
 * Subscribes to client events of type [T].
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeFor(
    listener: ChatEventListener<T>,
): Disposable = this.subscribeFor(
    T::class.java,
    listener = { event -> listener.onEvent(event as T) },
)

/**
 * Subscribes to client events of type [T], in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    listener: ChatEventListener<T>,
): Disposable = this.subscribeFor(
    lifecycleOwner,
    T::class.java,
    listener = { event -> listener.onEvent(event as T) },
)

/**
 * Subscribes to the specific [eventTypes] of the client.
 */
public fun ChatClient.subscribeFor(
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatEventListener<ChatEvent>,
): Disposable = subscribeFor(eventTypes = eventTypes.map { it.java }.toTypedArray(), listener = listener)

/**
 * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public fun ChatClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatEventListener<ChatEvent>,
): Disposable = subscribeFor(lifecycleOwner, eventTypes = eventTypes.map { it.java }.toTypedArray(), listener = listener)

/**
 * Subscribes for the next client event of type [T].
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeForSingle(
    listener: ChatEventListener<T>,
): Disposable = this.subscribeForSingle(T::class.java, listener)
