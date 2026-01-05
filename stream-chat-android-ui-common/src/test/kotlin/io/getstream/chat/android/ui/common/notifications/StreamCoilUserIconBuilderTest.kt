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
import org.mockito.kotlin.mock
import kotlin.reflect.full.primaryConstructor

internal class StreamCoilUserIconBuilderTest {

    private val FULL_CLASS_NAME = "io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder"

    @Test
    fun `Verify StreamCoilUserIconBuilder can be created by reflection`() {
        Class.forName(FULL_CLASS_NAME)
            .kotlin.primaryConstructor
            ?.call(mock<Context>()) as StreamCoilUserIconBuilder
    }
}
