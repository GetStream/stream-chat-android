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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CompoundComponentFactoryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val baseFactory = mutableStateOf<ChatComponentFactory>(object : ChatComponentFactory {})
    private val key = mutableIntStateOf(0)
    private val callerState = mutableIntStateOf(0)
    private val factoryCalls = mutableListOf<ChatComponentFactory>()

    @Test
    fun `factory is not rebuilt when a value it captures changes`() {
        setContent()

        composeTestRule.runOnIdle { callerState.intValue++ }
        composeTestRule.waitForIdle()

        assertEquals(1, factoryCalls.size)
    }

    @Test
    fun `factory is rebuilt when a key changes`() {
        setContent()

        composeTestRule.runOnIdle { key.intValue++ }
        composeTestRule.waitForIdle()

        assertEquals(2, factoryCalls.size)
    }

    @Test
    fun `factory is rebuilt when the current component factory changes`() {
        setContent()
        val newBaseFactory = object : ChatComponentFactory {}

        composeTestRule.runOnIdle { baseFactory.value = newBaseFactory }
        composeTestRule.waitForIdle()

        assertEquals(2, factoryCalls.size)
        assertSame(newBaseFactory, factoryCalls.last())
    }

    private fun setContent() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalComponentFactory provides baseFactory.value) {
                val callerValue = callerState.intValue
                CompoundComponentFactory(
                    key.intValue,
                    factory = { currentComponentFactory ->
                        factoryCalls += currentComponentFactory
                        CapturingComponentFactory(currentComponentFactory, callerValue)
                    },
                ) {}
            }
        }
    }

    private class CapturingComponentFactory(
        delegate: ChatComponentFactory,
        val capturedValue: Int,
    ) : ChatComponentFactory by delegate
}
