/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.core.utils

import io.getstream.chat.android.core.internal.utils.MutableValue
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MutableValueTest {

    @Test
    fun `test mutable value`() {
        val value = MutableValue(0)

        value.set(1)
        value.get() `should be equal to` 1
        value.isModified() `should be equal to` true

        value.set(2)
        value.get() `should be equal to` 2
        value.isModified() `should be equal to` true

        value.modify { 3 }
        value.get() `should be equal to` 3
        value.isModified() `should be equal to` true
    }
}
