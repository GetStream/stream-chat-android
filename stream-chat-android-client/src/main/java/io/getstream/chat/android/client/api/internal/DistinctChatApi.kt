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
import java.util.concurrent.atomic.AtomicReference
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

    private val distinctCalls = ConcurrentHashMap<Int, DistinctCall<out Any>>()

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val uniqueKey = ChannelQueryKey.from(channelType, channelId, query).hashCode()
        StreamLog.d(TAG) { "[queryChannel] channelType: $channelType, channelId: $channelId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannel(channelType, channelId, query)
        }
    }

    private fun <T : Any> getOrCreate(
        uniqueKey: Int,
        callBuilder: () -> Call<T>,
    ): Call<T> {
        return distinctCalls[uniqueKey] as? DistinctCall<T>
            ?: DistinctCall(callBuilder, uniqueKey) {
                distinctCalls.remove(uniqueKey)
            }.also {
                distinctCalls[uniqueKey] = it
            }
    }

    private companion object {
        private const val TAG = "Chat:DistinctApi"
    }
}

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
private class DistinctCall<T : Any>(
    private val callBuilder: () -> Call<T>,
    private val uniqueKey: Int,
    private val onFinished: () -> Unit,
) : Call<T> {

    init {
        StreamLog.i(TAG) { "<init> uniqueKey: $uniqueKey" }
    }

    private val delegate = AtomicReference<Call<T>>()
    private val isRunning = AtomicBoolean()
    private val subscribers = arrayListOf<Call.Callback<T>>()

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
            delegate.set(
                callBuilder().apply {
                    enqueue { result ->
                        try {
                            synchronized(subscribers) {
                                StreamLog.v(TAG) { "[enqueue] completed($uniqueKey): ${subscribers.size}" }
                                subscribers.onResultCatching(result)
                            }
                        } finally {
                            doFinally()
                        }
                    }
                }
            )
        }
    }

    override fun cancel() {
        try {
            StreamLog.d(TAG) { "[cancel] uniqueKey: $uniqueKey" }
            delegate.get()?.cancel()
        } finally {
            doFinally()
        }
    }

    private fun doFinally() {
        synchronized(subscribers) {
            subscribers.clear()
        }
        isRunning.set(false)
        delegate.set(null)
        onFinished()
    }

    private fun Collection<Call.Callback<T>>.onResultCatching(result: Result<T>) = forEach { callback ->
        try {
            callback.onResult(result)
        } catch (_: Throwable) {
            /* no-op */
        }
    }

    private companion object {
        private const val TAG = "Chat:DistinctCall"
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
