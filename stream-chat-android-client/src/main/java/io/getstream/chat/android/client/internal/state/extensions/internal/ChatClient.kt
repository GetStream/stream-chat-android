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

package io.getstream.chat.android.client.internal.state.extensions.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.internal.ChatClientStateCalls
import kotlinx.coroutines.CoroutineScope

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
internal val ChatClient.logic: LogicRegistry
    get() = resolveDependency<StatePlugin, LogicRegistry>()

/**
 * Intermediate class to request ChatClient class as states
 *
 * @return [ChatClientStateCalls]
 */
internal fun ChatClient.requestsAsState(scope: CoroutineScope): ChatClientStateCalls =
    ChatClientStateCalls(this, scope)
