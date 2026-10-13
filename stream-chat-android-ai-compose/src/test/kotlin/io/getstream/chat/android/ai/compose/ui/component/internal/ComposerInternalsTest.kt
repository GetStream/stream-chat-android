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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.app.Application
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
internal class ComposerInternalsTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a denied microphone permission offers the app settings`() {
        lateinit var onDenied: () -> Unit
        rule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            onDenied = rememberPermissionDeniedHandler(snackbarHostState)
            SnackbarHost(snackbarHostState)
        }

        rule.runOnIdle { onDenied() }
        rule.onNodeWithText("Microphone permission is required to record audio").assertExists()
        rule.onNodeWithText("Settings").performClick()
        rule.waitForIdle()

        val started = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started.action)
        assertEquals("package", started.data?.scheme)
    }

    @Test
    fun `each attachment can be removed`() {
        val first = Uri.parse("content://media/1")
        val second = Uri.parse("content://media/2")
        var uris by mutableStateOf(setOf(first, second))
        val removed = mutableListOf<Uri>()
        rule.setContent {
            AttachmentList(uris = uris, onRemoveAttachment = {
                removed += it
                uris = uris - it
            })
        }

        rule.onAllNodesWithContentDescription("Remove attachment").assertCountEquals(2)
        rule.onAllNodesWithContentDescription("Remove attachment")[0].performClick()

        rule.runOnIdle { assertEquals(listOf(first), removed) }
        rule.onAllNodesWithContentDescription("Remove attachment").assertCountEquals(1)
    }
}
