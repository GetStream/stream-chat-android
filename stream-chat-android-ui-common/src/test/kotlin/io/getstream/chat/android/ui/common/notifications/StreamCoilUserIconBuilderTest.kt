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

package io.getstream.chat.android.ui.common.notifications

import android.content.Context
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.mock

internal class StreamCoilUserIconBuilderTest {

    @Test
    fun `StreamCoilUserIconBuilder can be built the way NotificationHandlerFactory looks it up`() {
        val builder = Class.forName("io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder")
            .getConstructor(Context::class.java)
            .newInstance(mock<Context>())

        assertInstanceOf<StreamCoilUserIconBuilder>(builder)
    }
}
