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

package io.getstream.chat.android.compose.ui.util.adaptivelayout

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ThreePaneNavigatorTest {

    @Test
    fun `navigate to destination`() {
        val navigator = ThreePaneNavigator()
        val destination = ThreePaneDestination<Any>(ThreePaneRole.Detail)

        navigator.navigateTo(destination)

        assertEquals(2, navigator.destinations.size)
        assertEquals(destination, navigator.destinations.last())
    }

    @Test
    fun `navigate to destination with replace`() {
        val navigator = ThreePaneNavigator()
        val initialDestination = ThreePaneDestination<Any>(ThreePaneRole.Detail)
        val newDestination = ThreePaneDestination<Any>(ThreePaneRole.Detail)

        navigator.navigateTo(initialDestination)
        navigator.navigateTo(newDestination, replace = true)

        assertEquals(2, navigator.destinations.size)
        assertEquals(newDestination, navigator.destinations.last())
    }

    @Test
    fun `navigate to destination with pop up`() {
        val navigator = ThreePaneNavigator()
        val detailDestination = ThreePaneDestination<Any>(ThreePaneRole.Detail)
        val infoDestination = ThreePaneDestination<Any>(ThreePaneRole.Info)

        navigator.navigateTo(detailDestination)
        navigator.navigateTo(infoDestination)
        navigator.navigateTo(detailDestination, popUpTo = ThreePaneRole.List)

        assertEquals(2, navigator.destinations.size)
        assertEquals(detailDestination, navigator.destinations.last())
    }

    @Test
    fun `can navigate back`() {
        val navigator = ThreePaneNavigator()

        assertFalse(navigator.canNavigateBack())

        val destination = ThreePaneDestination<Any>(ThreePaneRole.Detail)
        navigator.navigateTo(destination)

        assertTrue(navigator.canNavigateBack())
    }

    @Test
    fun `navigate back`() {
        val navigator = ThreePaneNavigator()
        val destination = ThreePaneDestination<Any>(ThreePaneRole.Detail)

        navigator.navigateTo(destination)
        navigator.navigateBack()

        assertEquals(1, navigator.destinations.size)
        assertEquals(ThreePaneRole.List, navigator.destinations.last().pane)
    }

    @Test
    fun `pop up to`() {
        val navigator = ThreePaneNavigator()
        val detailDestination = ThreePaneDestination<Any>(ThreePaneRole.Detail)
        val infoDestination = ThreePaneDestination<Any>(ThreePaneRole.Info)

        navigator.navigateTo(detailDestination)
        navigator.navigateTo(infoDestination)
        navigator.popUpTo(ThreePaneRole.List)

        assertEquals(1, navigator.destinations.size)
        assertEquals(ThreePaneRole.List, navigator.destinations.last().pane)
    }
}
