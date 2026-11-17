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

package io.getstream.chat.android.compose.ui.scheduled

import io.getstream.chat.android.randomScheduledMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ScheduledMessageEditStateTest {

    private val edit = ScheduledMessageEdit()
    private val message = randomScheduledMessage()

    @Test
    fun `editing the message already edited keeps its edit`() {
        edit.start(message)
        val session = edit.session!!
        edit.setSaving(session.token, isSaving = true)

        edit.start(message)

        assertEquals(session.copy(isSaving = true), edit.session)
    }

    @Test
    fun `an edit that was ended can't end or change the next edit of the same message`() {
        edit.start(message)
        val first = edit.session!!.token
        edit.end(first)
        edit.start(message)
        val second = edit.session!!

        edit.setSaving(first, isSaving = true)
        edit.end(first)

        assertEquals(second, edit.session)
        assertFalse(edit.session!!.isSaving)
    }

    @Test
    fun `the current edit is saved, filled and ended by its token`() {
        edit.start(message)
        val token = edit.session!!.token

        edit.setSaving(token, isSaving = true)
        edit.onFilled(token)
        assertTrue(edit.session!!.isSaving)
        assertTrue(edit.session!!.isFilled)

        edit.end(token)
        assertNull(edit.session)
    }
}
