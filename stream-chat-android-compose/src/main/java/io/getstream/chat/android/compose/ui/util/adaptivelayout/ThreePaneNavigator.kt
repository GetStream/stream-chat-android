/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * The navigator used to navigate between the different destinations in the three-pane layout.
 *
 * @param destinations The list of destinations to start with. Defaults to a single destination in the list pane.
 */
@ExperimentalStreamChatApi
public class ThreePaneNavigator(
    destinations: List<ThreePaneDestination<*>> = listOf(ThreePaneDestination<Unit>(pane = ThreePaneRole.List)),
) {
    @Suppress("SpreadOperator")
    private val _destinations = mutableStateListOf(*destinations.toTypedArray())
    internal val destinations: List<ThreePaneDestination<*>> get() = _destinations.toList()

    /**
     * Navigates to the provided [destination].
     *
     * @param destination The destination to navigate to.
     * @param replace Whether to replace an existing destination with a new one of the same pane *(if it exists)*,
     * or add it to the stack.
     * @param popUpTo The role of the pane to pop up to before navigating to the destination.
     */
    public fun navigateTo(
        destination: ThreePaneDestination<*>,
        replace: Boolean = false,
        popUpTo: ThreePaneRole? = null,
    ) {
        popUpTo?.let(::popUpTo)
        if (replace) {
            replace(destination)
        } else {
            _destinations.add(destination)
        }
    }

    /**
     * Whether the navigator can navigate back to the previous destination.
     */
    public fun canNavigateBack(): Boolean = _destinations.size > 1

    /**
     * Navigates back to the previous destination.
     */
    public fun navigateBack() {
        if (canNavigateBack()) _destinations.removeAt(_destinations.lastIndex)
    }

    internal fun popUpTo(pane: ThreePaneRole) {
        while (canNavigateBack() && _destinations[_destinations.lastIndex].pane != pane) {
            _destinations.removeAt(_destinations.lastIndex)
        }
    }

    /**
     * Replace an existing destination with a new one of the same pane *(if it exists)*.
     */
    private fun replace(destination: ThreePaneDestination<*>) {
        val operator = { existing: ThreePaneDestination<*> ->
            if (existing.pane == destination.pane) {
                destination
            } else {
                existing
            }
        }
        val iterator = _destinations.listIterator()
        while (iterator.hasNext()) {
            iterator.set(operator(iterator.next()))
        }
    }

    internal companion object {
        val Saver: Saver<ThreePaneNavigator, Any> = listSaver(
            save = { navigator ->
                navigator.destinations.mapNotNull { with(ThreePaneDestination.Saver) { save(it) } }
            },
            restore = { state ->
                ThreePaneNavigator(state.mapNotNull(ThreePaneDestination.Saver::restore))
            },
        )
    }
}

/**
 * Represents a destination in the three-pane navigation system.
 *
 * @param T The type of the arguments associated with the destination. **It must be serializable**.
 * @property pane The pane destination of the navigation.
 * @property arguments The optional arguments to pass to the destination.
 */
@ExperimentalStreamChatApi
public data class ThreePaneDestination<out T>(
    val pane: ThreePaneRole,
    val arguments: T? = null,
) {
    internal companion object {
        val Saver: Saver<ThreePaneDestination<*>, Any> = listSaver(
            save = { destination -> listOf(destination.pane, destination.arguments) },
            restore = { state -> ThreePaneDestination(pane = state[0] as ThreePaneRole, arguments = state[1]) },
        )
    }
}

/**
 * Represents the role of a pane in the three-pane layout.
 */
@ExperimentalStreamChatApi
public enum class ThreePaneRole {
    /**
     * The list pane.
     */
    List,

    /**
     * The detail pane.
     */
    Detail,

    /**
     * The info pane.
     */
    Info,
}

/**
 * Remembers a [ThreePaneNavigator] that can be used to navigate between
 * the different destinations in the three-pane layout.
 */
@ExperimentalStreamChatApi
@Composable
public fun rememberThreePaneNavigator(): ThreePaneNavigator =
    rememberSaveable(saver = ThreePaneNavigator.Saver) { ThreePaneNavigator() }
