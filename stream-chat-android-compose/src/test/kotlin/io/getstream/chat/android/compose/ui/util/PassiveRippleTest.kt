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

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class PassiveRippleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tap inside passiveRipple propagates to outer combinedClickable`() {
        val onParentClick = mock<() -> Unit>()

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag("parent")
                    .size(200.dp)
                    .combinedClickable(
                        interactionSource = remember(::MutableInteractionSource),
                        indication = null,
                        onClick = onParentClick,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .testTag("inner")
                        .size(100.dp)
                        .passiveRipple(),
                )
            }
        }

        composeTestRule.onNodeWithTag("inner", useUnmergedTree = true).performTouchInput {
            down(center)
            up()
        }

        verify(onParentClick).invoke()
    }

    @Test
    fun `long-press inside passiveRipple propagates to outer combinedClickable`() {
        val onParentLongClick = mock<() -> Unit>()

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag("parent")
                    .size(200.dp)
                    .combinedClickable(
                        interactionSource = remember(::MutableInteractionSource),
                        indication = null,
                        onClick = {},
                        onLongClick = onParentLongClick,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .testTag("inner")
                        .size(100.dp)
                        .passiveRipple(),
                )
            }
        }

        composeTestRule.onNodeWithTag("inner", useUnmergedTree = true).performTouchInput {
            longClick()
        }

        verify(onParentLongClick).invoke()
    }

    @Test
    fun `inner consuming clickable does not trigger outer combinedClickable`() {
        val onParentClick = mock<() -> Unit>()
        val onConsumerClick = mock<() -> Unit>()

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag("parent")
                    .size(200.dp)
                    .combinedClickable(
                        interactionSource = remember(::MutableInteractionSource),
                        indication = null,
                        onClick = onParentClick,
                    )
                    .passiveRipple(),
            ) {
                Box(
                    modifier = Modifier
                        .testTag("consumer")
                        .size(100.dp)
                        .combinedClickable(
                            interactionSource = remember(::MutableInteractionSource),
                            indication = null,
                            onClick = onConsumerClick,
                        ),
                )
            }
        }

        composeTestRule.onNodeWithTag("consumer").performTouchInput {
            down(center)
            up()
        }

        verify(onConsumerClick).invoke()
        verify(onParentClick, never()).invoke()
    }

    @Test
    fun `drag out of bounds during gesture does not crash`() {
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag("target")
                    .size(100.dp)
                    .passiveRipple(),
            )
        }

        composeTestRule.onNodeWithTag("target").performTouchInput {
            down(center)
            moveTo(Offset(-200f, -200f))
            up()
        }
    }
}
