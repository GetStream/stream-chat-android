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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class AccessibilityUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setTouchExplorationEnabled(enabled: Boolean) {
        val manager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService<AccessibilityManager>()!!
        Shadows.shadowOf(manager).setTouchExplorationEnabled(enabled)
    }

    @Test
    fun `rememberIsTouchExplorationEnabled is true when touch exploration is enabled`() {
        setTouchExplorationEnabled(true)
        var enabled = false
        composeTestRule.setContent { enabled = rememberIsTouchExplorationEnabled() }
        composeTestRule.runOnIdle { assertTrue(enabled) }
    }

    @Test
    fun `rememberIsTouchExplorationEnabled is false when touch exploration is disabled`() {
        setTouchExplorationEnabled(false)
        var enabled = true
        composeTestRule.setContent { enabled = rememberIsTouchExplorationEnabled() }
        composeTestRule.runOnIdle { assertFalse(enabled) }
    }

    @Test
    fun `semanticsNodeIdForTestTag resolves the id of the tagged node`() {
        lateinit var view: View
        composeTestRule.setContent {
            view = LocalView.current
            TaggedNode(tag = "present")
        }
        composeTestRule.runOnIdle {
            assertNotNull(view.semanticsNodeIdForTestTag("present"))
        }
    }

    @Test
    fun `semanticsNodeIdForTestTag returns null when no node has the tag`() {
        lateinit var view: View
        composeTestRule.setContent {
            view = LocalView.current
            TaggedNode(tag = "present")
        }
        composeTestRule.runOnIdle {
            assertNull(view.semanticsNodeIdForTestTag("absent"))
        }
    }

    @Test
    fun `requestAccessibilityFocusForTestTag fails safe on a non-Compose view`() {
        val plainView = View(ApplicationProvider.getApplicationContext())
        assertFalse(plainView.requestAccessibilityFocusForTestTag("present"))
    }
}

@Composable
private fun TaggedNode(tag: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag(tag)
            .semantics { contentDescription = "tagged" },
    )
}
