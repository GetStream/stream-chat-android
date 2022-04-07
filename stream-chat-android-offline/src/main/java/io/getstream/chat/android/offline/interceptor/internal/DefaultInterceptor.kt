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
 
package io.getstream.chat.android.offline.interceptor.internal

import io.getstream.chat.android.client.experimental.interceptor.Interceptor
import io.getstream.chat.android.client.experimental.interceptor.SendMessageInterceptor

/**
 * Implementation of [Interceptor] that brings support for intercepting API requests. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param sendMessageInterceptor [SendMessageInterceptor]
 */
internal class DefaultInterceptor(sendMessageInterceptor: SendMessageInterceptor) :
    Interceptor,
    SendMessageInterceptor by sendMessageInterceptor
