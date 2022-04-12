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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetryCall
import io.getstream.chat.android.client.utils.retry.CallRetryService
import io.getstream.chat.android.client.utils.retry.RetryPolicy
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope

/**
 * Wraps the original call with [RetryCall] wrapper.
 * Allows to retry the original call based on [io.getstream.chat.android.client.utils.retry.RetryPolicy]
 *
 * @param scope Coroutine scope where the call should be run.
 * @param retryPolicy A policy used for retrying the call.
 */
@InternalStreamChatApi
// TODO: Make internal after migrating ChatDomain
public fun <T : Any> Call<T>.retry(scope: CoroutineScope, retryPolicy: RetryPolicy): Call<T> =
    RetryCall(this, scope, CallRetryService(retryPolicy))
