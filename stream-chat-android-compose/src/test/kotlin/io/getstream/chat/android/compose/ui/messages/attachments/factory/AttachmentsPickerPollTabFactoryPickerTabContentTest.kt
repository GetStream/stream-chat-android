/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import org.junit.Rule
import org.junit.Test

internal class AttachmentsPickerPollTabFactoryPickerTabContentTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_4A)

    @Test
    fun `light mode`() {
        snapshot(isInDarkMode = false) {
            WithLocalViewModelStore {
                AttachmentsPickerPollTabFactory().PickerTabContent(
                    onAttachmentPickerAction = {},
                    attachments = listOf(),
                    onAttachmentsChanged = {},
                    onAttachmentItemSelected = {},
                    onAttachmentsSubmitted = {},
                )
            }
        }
    }

    @Test
    fun `dark mode`() {
        snapshot(isInDarkMode = true) {
            WithLocalViewModelStore {
                AttachmentsPickerPollTabFactory().PickerTabContent(
                    onAttachmentPickerAction = {},
                    attachments = listOf(),
                    onAttachmentsChanged = {},
                    onAttachmentItemSelected = {},
                    onAttachmentsSubmitted = {},
                )
            }
        }
    }

    @Composable
    private fun WithLocalViewModelStore(content: @Composable () -> Unit) {
        val viewModelStore = remember { ViewModelStore() }
        val viewModelStoreOwner = remember(viewModelStore) {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore get() = viewModelStore
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                viewModelStore.clear()
            }
        }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            content()
        }
    }
}
