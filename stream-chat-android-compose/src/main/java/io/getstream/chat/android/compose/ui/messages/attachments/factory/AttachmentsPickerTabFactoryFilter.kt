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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.compose.util.extensions.isPollEnabled
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * Filter to determine if the tab factory should be shown or not.
 */
internal class AttachmentsPickerTabFactoryFilter {

    /**
     * Filters the provided factories based on whether they are allowed for the channel.
     *
     * @param factories The list of factories to filter.
     * @param channel The channel to check against.
     * @param messageMode The current message mode, used to determine if the default "Polls" tab is enabled.
     */
    internal fun filterAllowedFactories(
        factories: List<AttachmentsPickerTabFactory>,
        channel: Channel,
        messageMode: MessageMode,
    ): List<AttachmentsPickerTabFactory> {
        return factories
            .filter { factory ->
                factory.isEnabledForMessageMode(messageMode) && factory.isPickerTabEnabled(channel)
            }
            .map { factory ->
                when (factory) {
                    is AttachmentsPickerSystemTabFactory -> adjustSystemFactory(factory, channel, messageMode)
                    else -> factory
                }
            }
    }

    private fun adjustSystemFactory(
        factory: AttachmentsPickerSystemTabFactory,
        channel: Channel,
        messageMode: MessageMode,
    ): AttachmentsPickerSystemTabFactory {
        // Adjust pollAllowed based on the:
        // 1. Current message mode (only in Normal mode)
        // 2. Channel config (are polls enabled for the channel and the user has rights)
        // 3. Factory config (is the polls tab enabled in the factory config)
        val config = factory.config.copy(
            pollAllowed = messageMode is MessageMode.Normal && channel.isPollEnabled() && factory.config.pollAllowed,
        )
        return AttachmentsPickerSystemTabFactory(config)
    }

    private fun AttachmentsPickerTabFactory.isEnabledForMessageMode(mode: MessageMode): Boolean {
        return when (this) {
            // The default "Polls" tab is only shown in Normal mode
            is AttachmentsPickerPollTabFactory -> mode is MessageMode.Normal
            else -> true
        }
    }
}
