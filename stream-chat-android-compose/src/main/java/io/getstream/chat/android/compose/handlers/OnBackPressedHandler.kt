package io.getstream.chat.android.compose.handlers

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * Special composable function that intercepts the [OnBackPressedDispatcher].
 *
 * This intercepts the system back action and allows you to react to the click. If you want to build
 * a special back button component you can use the
 * [io.getstream.chat.android.compose.ui.components.BackButton] instead.
 *
 * To use this, simply call [SystemBackPressedHandler] in any of your UI components and pass in the
 * following two parameters:
 *
 * @param isEnabled - If the handler is enabled or not.
 * @param onBackPressed - The action to execute any time the system back button is tapped.
 * */
@Composable
fun SystemBackPressedHandler(
    isEnabled: Boolean,
    onBackPressed: () -> Unit
) {
    val backPressedDispatcher =
        (LocalLifecycleOwner.current as ComponentActivity).onBackPressedDispatcher

    val callback = buildBackPressedCallback(isEnabled, onBackPressed)

    DisposableEffect(backPressedDispatcher) {
        backPressedDispatcher.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}

private fun buildBackPressedCallback(
    isEnabled: Boolean,
    onBackPressed: () -> Unit
): OnBackPressedCallback =
    object : OnBackPressedCallback(isEnabled) {
        override fun handleOnBackPressed() = onBackPressed()
    }