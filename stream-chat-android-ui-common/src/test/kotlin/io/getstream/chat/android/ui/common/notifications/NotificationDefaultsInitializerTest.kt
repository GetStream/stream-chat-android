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
import io.getstream.chat.android.client.notifications.handler.NotificationDefaults
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.mock

internal class NotificationDefaultsInitializerTest {

    @AfterEach
    fun tearDown() {
        NotificationDefaults.userIconBuilderFactory = null
    }

    @Test
    fun `create registers the Coil user icon builder`() {
        NotificationDefaultsInitializer().create(mock())

        val builder = NotificationDefaults.userIconBuilderFactory?.invoke(mock<Context>())

        assertInstanceOf<StreamCoilUserIconBuilder>(builder)
    }
}
