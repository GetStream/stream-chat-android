/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.logger

import io.getstream.log.Priority
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

internal class StreamLoggerHandlerTest {

    private val chatLoggerHandler = mock<ChatLoggerHandler>()
    private val streamLoggerHandler = StreamLoggerHandler(chatLoggerHandler)

    private val tag = "tag"
    private val message = "message"

    @Test
    fun `When logging with VERBOSE priority Should call logV on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.VERBOSE, tag, message, null)
        verify(chatLoggerHandler).logV(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with DEBUG priority Should call logD on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.DEBUG, tag, message, null)
        verify(chatLoggerHandler).logD(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with INFO priority Should call logI on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.INFO, tag, message, null)
        verify(chatLoggerHandler).logI(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with WARN priority Should call logW on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.WARN, tag, message, null)
        verify(chatLoggerHandler).logW(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with ERROR priority and throwable null Should call logE on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.ERROR, tag, message, null)
        verify(chatLoggerHandler).logE(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with ERROR priority and throwable not null Should call logE on ChatLoggerHandler`() {
        val throwable = Throwable()
        streamLoggerHandler.log(Priority.ERROR, tag, message, throwable)
        verify(chatLoggerHandler).logE(tag, message, throwable)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with ASSERT priority and throwable null Should call logE on ChatLoggerHandler`() {
        streamLoggerHandler.log(Priority.ASSERT, tag, message, null)
        verify(chatLoggerHandler).logE(tag, message)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with ASSERT priority and throwable not null Should call logE on ChatLoggerHandler`() {
        val throwable = Throwable()
        streamLoggerHandler.log(Priority.ASSERT, tag, message, throwable)
        verify(chatLoggerHandler).logE(tag, message, throwable)
        verifyNoMoreInteractions(chatLoggerHandler)
    }

    @Test
    fun `When logging with null handler Should not call any function`() {
        val streamLoggerHandler = StreamLoggerHandler(null)
        streamLoggerHandler.log(Priority.VERBOSE, tag, message, null)
        verifyNoMoreInteractions(chatLoggerHandler)
    }
}
