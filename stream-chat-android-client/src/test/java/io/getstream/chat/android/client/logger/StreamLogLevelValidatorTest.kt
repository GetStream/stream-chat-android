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

package io.getstream.chat.android.client.logger

import io.getstream.log.Priority
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class StreamLogLevelValidatorTest {

    private val TAG = "tag"

    @Test
    fun `test isLoggable for ALL log level`() {
        val validator = StreamLogLevelValidator(ChatLogLevel.ALL)
        assertTrue(validator.isLoggable(Priority.VERBOSE, TAG))
        assertTrue(validator.isLoggable(Priority.DEBUG, TAG))
        assertTrue(validator.isLoggable(Priority.INFO, TAG))
        assertTrue(validator.isLoggable(Priority.WARN, TAG))
        assertTrue(validator.isLoggable(Priority.ERROR, TAG))
        assertTrue(validator.isLoggable(Priority.ASSERT, TAG))
    }

    @Test
    fun `test isLoggable for DEBUG log level`() {
        val validator = StreamLogLevelValidator(ChatLogLevel.DEBUG)
        assertFalse(validator.isLoggable(Priority.VERBOSE, TAG))
        assertTrue(validator.isLoggable(Priority.DEBUG, TAG))
        assertTrue(validator.isLoggable(Priority.INFO, TAG))
        assertTrue(validator.isLoggable(Priority.WARN, TAG))
        assertTrue(validator.isLoggable(Priority.ERROR, TAG))
        assertTrue(validator.isLoggable(Priority.ASSERT, TAG))
    }

    @Test
    fun `test isLoggable for WARN log level`() {
        val validator = StreamLogLevelValidator(ChatLogLevel.WARN)
        assertFalse(validator.isLoggable(Priority.VERBOSE, TAG))
        assertFalse(validator.isLoggable(Priority.DEBUG, TAG))
        assertFalse(validator.isLoggable(Priority.INFO, TAG))
        assertTrue(validator.isLoggable(Priority.WARN, TAG))
        assertTrue(validator.isLoggable(Priority.ERROR, TAG))
        assertTrue(validator.isLoggable(Priority.ASSERT, TAG))
    }

    @Test
    fun `test isLoggable for ERROR log level`() {
        val validator = StreamLogLevelValidator(ChatLogLevel.ERROR)
        assertFalse(validator.isLoggable(Priority.VERBOSE, TAG))
        assertFalse(validator.isLoggable(Priority.DEBUG, TAG))
        assertFalse(validator.isLoggable(Priority.INFO, TAG))
        assertFalse(validator.isLoggable(Priority.WARN, TAG))
        assertTrue(validator.isLoggable(Priority.ERROR, TAG))
        assertTrue(validator.isLoggable(Priority.ASSERT, TAG))
    }

    @Test
    fun `test isLoggable for NOTHING log level`() {
        val validator = StreamLogLevelValidator(ChatLogLevel.NOTHING)
        assertFalse(validator.isLoggable(Priority.VERBOSE, TAG))
        assertFalse(validator.isLoggable(Priority.DEBUG, TAG))
        assertFalse(validator.isLoggable(Priority.INFO, TAG))
        assertFalse(validator.isLoggable(Priority.WARN, TAG))
        assertFalse(validator.isLoggable(Priority.ERROR, TAG))
        assertFalse(validator.isLoggable(Priority.ASSERT, TAG))
    }
}
