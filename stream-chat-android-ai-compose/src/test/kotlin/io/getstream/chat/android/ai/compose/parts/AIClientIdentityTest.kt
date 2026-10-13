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

package io.getstream.chat.android.ai.compose.parts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIClientIdentityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `the install id stays the same`() {
        val first = AIClientIdentity.installId(context)

        assertTrue(first.startsWith("android-"))
        assertEquals(first, AIClientIdentity.installId(context))
    }

    @Test
    fun `the install id is kept out of backups`() {
        val id = AIClientIdentity.installId(context)

        val stored = context.noBackupFilesDir.walk().filter { it.isFile }.map { it.readText() }.toList()
        assertTrue(id in stored)
    }
}
