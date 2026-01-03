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

package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import io.getstream.chat.android.models.Device

/**
 * Generator responsible for providing information needed to register the push notifications provider
 */
@Deprecated(
    "This class is no longer used and will be removed in the next major release. " +
        "Use `io.getstream.android.push.PushDeviceGenerator` instead.",
)
public interface PushDeviceGenerator {
    /**
     * Checks if push notification provider is valid for this device
     */
    public fun isValidForThisDevice(context: Context): Boolean

    /**
     * Called when this [PushDeviceGenerator] has been selected to be used.
     */
    public fun onPushDeviceGeneratorSelected()

    /**
     * Asynchronously generates a [Device] and calls [onDeviceGenerated] callback once it's ready
     */
    public fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit)
}
