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

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest as QueryChannelRequestDto

/**
 * Prevents simultaneous requests of the same requests.
 */
@Suppress("UNCHECKED_CAST")
internal class DistinctChatApi(
    private val delegate: ChatApi,
) : ChatApi by delegate {

    private val ongoingCalls = ConcurrentHashMap<Int, OngoingCall<out Any>>()

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val uniqueKey = ChannelQueryKey.from(channelType, channelId, query).hashCode()
        StreamLog.d(TAG) { "[queryChannel] channelType: $channelType, channelId: $channelId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannel(channelType, channelId, query)
        }
    }

    private fun <T : Any> getOrCreate(
        uniqueKey: Int, callBuilder: () -> Call<T>,
    ): Call<T> {
        return ongoingCalls[uniqueKey] as? OngoingCall<T>
            ?: OngoingCall(callBuilder(), uniqueKey) {
                ongoingCalls.remove(uniqueKey)
            }.also {
                ongoingCalls[uniqueKey] = it
            }
    }

    private companion object {
        private const val TAG = "Chat:DistinctApi"
    }
}

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
private class OngoingCall<T : Any>(
    private val delegate: Call<T>,
    private val uniqueKey: Int,
    private val onFinished: () -> Unit,
) : Call<T> {

    init {
        StreamLog.i(TAG) { "<init> uniqueKey: $uniqueKey" }
    }

    private val subscribers = arrayListOf<Call.Callback<T>>()
    private val isRunning = AtomicBoolean()

    override fun execute(): Result<T> {
        return runBlocking {
            StreamLog.d(TAG) { "[execute] uniqueKey: $uniqueKey" }
            suspendCoroutine { continuation ->
                enqueue { result ->
                    StreamLog.v(TAG) { "[execute] completed($uniqueKey)" }
                    continuation.resume(result)
                }
            }
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        StreamLog.d(TAG) { "[enqueue] callback($$uniqueKey): $callback" }
        synchronized(subscribers) {
            subscribers.add(callback)
        }
        if (isRunning.compareAndSet(false, true)) {
            delegate.enqueue { result ->
                synchronized(subscribers) {
                    StreamLog.v(TAG) { "[enqueue] completed($uniqueKey): ${subscribers.size}" }
                    subscribers.onResult(result)
                    subscribers.clear()
                }
                onFinished()
                isRunning.set(false)
            }
        }
    }

    override fun cancel() {
        return try {
            StreamLog.d(TAG) { "[enqueue] uniqueKey: $uniqueKey" }
            delegate.cancel()
            synchronized(subscribers) {
                subscribers.clear()
            }
        } finally {
            onFinished()
            isRunning.set(false)
        }
    }

    private fun Collection<Call.Callback<T>>.onResult(result: Result<T>) = forEach { callback ->
        callback.onResult(result)
    }

    private companion object {
        private const val TAG = "Chat:OngoingCall"
    }
}

private data class ChannelQueryKey(
    val channelType: String,
    val channelId: String,
    val queryKey: QueryChannelRequestDto,
) {

    companion object {
        fun from(
            channelType: String,
            channelId: String,
            query: QueryChannelRequest,
        ): ChannelQueryKey {
            return ChannelQueryKey(
                channelType = channelType,
                channelId = channelId,
                queryKey = QueryChannelRequestDto(
                    state = query.state,
                    watch = query.watch,
                    presence = query.presence,
                    messages = query.messages,
                    watchers = query.watchers,
                    members = query.members,
                    data = query.data,
                )
            )
        }
    }
}
