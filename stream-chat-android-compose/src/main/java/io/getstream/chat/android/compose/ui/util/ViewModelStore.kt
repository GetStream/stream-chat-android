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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Provides a fresh [ViewModelStore] for the composable content.
 * The store is cleared when the composable leaves the composition.
 */
@Composable
internal fun ViewModelStore(
    vararg keys: Any?,
    content: @Composable () -> Unit,
) {
    val parentOwner = LocalViewModelStoreOwner.current
    val viewModelStore = remember { ViewModelStore() }
    val viewModelStoreOwner = remember(viewModelStore, parentOwner) {
        StreamViewModelStoreOwner(viewModelStore, parentOwner)
    }

    DisposableEffect(keys) {
        onDispose {
            viewModelStore.clear()
        }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        content()
    }
}

/**
 * A [ViewModelStoreOwner] that uses its own [ViewModelStore] while forwarding
 * [CreationExtras] from the parent owner (typically the Activity).
 * This ensures that ViewModels requiring a [SavedStateHandle][androidx.lifecycle.SavedStateHandle]
 * receive the correct extras (e.g. `SAVED_STATE_REGISTRY_OWNER_KEY`).
 */
private class StreamViewModelStoreOwner(
    override val viewModelStore: ViewModelStore,
    parentOwner: ViewModelStoreOwner?,
) : ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    private val parentFactory = parentOwner as? HasDefaultViewModelProviderFactory

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = parentFactory?.defaultViewModelProviderFactory ?: ViewModelProvider.NewInstanceFactory()

    override val defaultViewModelCreationExtras: CreationExtras
        get() = parentFactory?.defaultViewModelCreationExtras ?: CreationExtras.Empty
}
