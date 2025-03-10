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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

internal enum class ThreePaneRole { List, Detail, Info }

/**
 * The navigator used to navigate between the different destinations in the three-pane layout.
 *
 * @param destinations The list of destinations to start with. Defaults to a single destination in the list pane.
 */
internal class ThreePaneNavigator(
    destinations: List<ThreePaneDestination<*>> = listOf(ThreePaneDestination<Unit>(pane = ThreePaneRole.List))
) {
    private val _destinations = mutableStateListOf(*destinations.toTypedArray())
    val destinations: List<ThreePaneDestination<*>> get() = _destinations

    val current: ThreePaneDestination<*> get() = _destinations.last()

    fun navigateTo(destination: ThreePaneDestination<*>, popUpTo: ThreePaneRole? = null) {
        popUpTo?.let(::popUpTo)
        _destinations.add(destination)
    }

    fun navigateBack() {
        if (_destinations.size > 1) _destinations.removeAt(_destinations.lastIndex)
    }

    fun popUpTo(pane: ThreePaneRole) {
        while (_destinations.size > 1 && _destinations[_destinations.lastIndex].pane != pane) {
            _destinations.removeAt(_destinations.lastIndex)
        }
    }

    companion object {
        val Saver: Saver<ThreePaneNavigator, Any> = listSaver(
            save = { navigator ->
                navigator.destinations.map { with(ThreePaneDestination.Saver) { save(it) } }
            },
            restore = { state ->
                ThreePaneNavigator(state.mapNotNull { it?.let(ThreePaneDestination.Saver::restore) })
            },
        )
    }
}

/**
 * Represents a destination in the three-pane navigation system.
 *
 * @param T The type of the arguments associated with the destination. It must be serializable.
 * @property pane The pane destination of the navigation.
 * @property arguments The optional arguments to pass to the destination.
 */
internal data class ThreePaneDestination<out T>(
    val pane: ThreePaneRole,
    val arguments: T? = null,
) {
    companion object {
        val Saver: Saver<ThreePaneDestination<*>, Any> = listSaver(
            save = { destination -> listOf(destination.pane, destination.arguments) },
            restore = { state -> ThreePaneDestination(pane = state[0] as ThreePaneRole, arguments = state[1]) },
        )
    }
}
