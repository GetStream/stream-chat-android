package io.getstream.chat.android.compose.ui.chats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

public interface ThreePaneNavigator {
    public fun navigateToInfo(mode: ExtraContentMode)
    public fun navigateBack()
}

internal enum class ThreePaneRole { List, Detail, Info }

internal class DefaultThreePaneNavigator(destinations: List<ThreePaneDestination<*>>) : ThreePaneNavigator {
    private val _destinations = mutableStateListOf(*destinations.toTypedArray())
    val destinations: List<ThreePaneDestination<*>> get() = _destinations

    val current: ThreePaneDestination<*> get() = _destinations.last()

    override fun navigateToInfo(mode: ExtraContentMode) {
        navigateTo(ThreePaneDestination(pane = ThreePaneRole.Info, mode))
    }

    override fun navigateBack() {
        if (_destinations.size > 1) _destinations.removeAt(_destinations.lastIndex)
    }

    fun navigateTo(destination: ThreePaneDestination<*>, popUpTo: ThreePaneRole? = null) {
        popUpTo?.let(::popUpTo)
        _destinations.add(destination)
    }

    fun popUpTo(pane: ThreePaneRole) {
        while (_destinations.size > 1 && _destinations[_destinations.lastIndex].pane != pane) {
            _destinations.removeAt(_destinations.lastIndex)
        }
    }

    companion object {
        val Saver: Saver<DefaultThreePaneNavigator, Any> = listSaver(
            save = { navigator -> navigator.destinations.map { with(ThreePaneDestination.Saver) { save(it) } } },
            restore = { state -> DefaultThreePaneNavigator(state.mapNotNull { it?.let(ThreePaneDestination.Saver::restore) }) }
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
            restore = { state -> ThreePaneDestination(pane = state[0] as ThreePaneRole, arguments = state[1]) }
        )
    }
}

@Composable
public fun rememberNavigator(): ThreePaneNavigator = rememberSaveable(saver = DefaultThreePaneNavigator.Saver) {
    DefaultThreePaneNavigator(destinations = listOf(ThreePaneDestination<Unit>(pane = ThreePaneRole.List)))
}
