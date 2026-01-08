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

package io.getstream.chat.android.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class UserTransformerTest {

    @Test
    fun testNoOpUserTransformer() {
        // given
        val user = User(id = "uid1", name = "username")
        // when
        val transformed = NoOpUserTransformer.transform(user)
        // then
        transformed `should be equal to` user
    }

    @Test
    fun testCustomUserTransformed() {
        // given
        val user = User(id = "uid1", name = "username")
        val customTransformer = UserTransformer { it.copy(name = "transformed") }
        // when
        val transformed = customTransformer.transform(user)
        // then
        val expected = User(id = "uid1", name = "transformed")
        transformed `should be equal to` expected
    }
}
