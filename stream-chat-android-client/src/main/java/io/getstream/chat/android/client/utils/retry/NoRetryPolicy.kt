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

package io.getstream.chat.android.client.utils.retry

import io.getstream.chat.android.client.errors.ChatError

/**
 * Default retry policy that won't retry any calls.
 */
internal class NoRetryPolicy : RetryPolicy {
    /**
     * Shouldn't retry any calls.
     *
     * @return false
     */
    override fun shouldRetry(attempt: Int, error: ChatError): Boolean = false

    /**
     * Should never be called as the policy doesn't allow retrying.
     */
    override fun retryTimeout(attempt: Int, error: ChatError): Int = 0
}
