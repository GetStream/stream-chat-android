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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import io.getstream.chat.android.compose.R
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PollSwitchInputErrorTest {

    private val context: Context = mock()

    @Test
    fun `empty value returns null`() {
        val sut = PollSwitchInput(value = "", keyboardType = KeyboardType.Number)

        val result = sut.errorOrNull(context = context, input = "")

        assertNull(result)
    }

    @Test
    fun `non-number keyboard type returns null`() {
        val sut = PollSwitchInput(value = "", keyboardType = KeyboardType.Text)

        val result = sut.errorOrNull(context = context, input = "abc")

        assertNull(result)
    }

    @Test
    fun `number within range returns null`() {
        val pollSwitchInput = PollSwitchInput(
            value = "",
            minValue = 1,
            maxValue = 10,
            keyboardType = KeyboardType.Number,
        )

        val result = pollSwitchInput.errorOrNull(context = context, input = "5")

        assertNull(result)
    }

    @Test
    fun `number below range returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1, 10))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1,
            maxValue = 10,
            keyboardType = KeyboardType.Number,
        )

        val result = sut.errorOrNull(context = context, input = "0")

        assertTrue(result is PollOptionNumberExceed)
    }

    @Test
    fun `number above range returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1, 10))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1,
            maxValue = 10,
            keyboardType = KeyboardType.Number,
        )

        val result = sut.errorOrNull(context = context, input = "11")

        assertTrue(result is PollOptionNumberExceed)
    }

    @Test
    fun `invalid number format returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1, 10))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1,
            maxValue = 10,
            keyboardType = KeyboardType.Number,
        )

        val result = sut.errorOrNull(context = context, input = "abc")

        assertTrue(result is PollOptionNumberExceed)
    }

    @Test
    fun `decimal within range returns null`() {
        val sut = PollSwitchInput(
            value = "",
            minValue = 1f,
            maxValue = 10f,
            keyboardType = KeyboardType.Decimal,
        )

        val result = sut.errorOrNull(context = context, input = "5.5")

        assertNull(result)
    }

    @Test
    fun `decimal below range returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1f, 10f))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1f,
            maxValue = 10f,
            keyboardType = KeyboardType.Decimal,
        )

        val result = sut.errorOrNull(context = context, input = "0")

        assertTrue(result is PollOptionNumberExceed)
    }

    @Test
    fun `decimal above range returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1f, 10f))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1f,
            maxValue = 10f,
            keyboardType = KeyboardType.Decimal,
        )

        val result = sut.errorOrNull(context = context, input = "10.5")

        assertTrue(result is PollOptionNumberExceed)
    }

    @Test
    fun `invalid decimal format returns error`() {
        whenever(context.getString(R.string.stream_ui_poll_multiple_answers_error, 1f, 10f))
            .thenReturn("Error")

        val sut = PollSwitchInput(
            value = "",
            minValue = 1f,
            maxValue = 10f,
            keyboardType = KeyboardType.Decimal,
        )

        val result = sut.errorOrNull(context = context, input = "abc")

        assertTrue(result is PollOptionNumberExceed)
    }
}
