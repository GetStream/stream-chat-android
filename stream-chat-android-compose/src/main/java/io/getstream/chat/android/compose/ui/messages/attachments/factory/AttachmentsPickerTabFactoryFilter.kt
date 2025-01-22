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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.models.Channel

/**
 * Filter to determine if the tab factory should be shown or not.
 */
internal class AttachmentsPickerTabFactoryFilter {

    /**
     * Filters the provided factories based on whether they are allowed for the channel.
     *
     * @param factories The list of factories to filter.
     * @param channel The channel to check against.
     */
    internal fun filterAllowedFactories(
        factories: List<AttachmentsPickerTabFactory>,
        channel: Channel,
    ): List<AttachmentsPickerTabFactory> {
        return factories
            .filter { factory ->
                isAllowed(factory, channel)
            }
            .map { factory ->
                when (factory) {
                    is AttachmentsPickerSystemTabFactory -> adjustSystemFactory(factory, channel)
                    else -> factory
                }
            }
    }

    private fun isAllowed(factory: AttachmentsPickerTabFactory, channel: Channel): Boolean {
        return when (factory.attachmentsPickerMode) {
            is Poll -> channel.config.pollsEnabled
            else -> true
        }
    }

    private fun adjustSystemFactory(
        factory: AttachmentsPickerSystemTabFactory,
        channel: Channel,
    ): AttachmentsPickerSystemTabFactory {
        // Adjust pollEnabled based on the channel config
        val pollsAllowed = channel.config.pollsEnabled
        return AttachmentsPickerSystemTabFactory(
            filesAllowed = factory.filesAllowed,
            mediaAllowed = factory.mediaAllowed,
            captureImageAllowed = factory.captureImageAllowed,
            captureVideoAllowed = factory.captureVideoAllowed,
            pollAllowed = pollsAllowed && factory.pollAllowed,
        )
    }
}
