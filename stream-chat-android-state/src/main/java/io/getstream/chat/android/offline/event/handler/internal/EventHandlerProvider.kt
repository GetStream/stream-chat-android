/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.event.handler.internal

import io.getstream.logging.StreamLog

/**
 * Provider of EventHandlerImpl. This should be used is services and Workers.
 */
internal object EventHandlerProvider {

    private var _eventHandler: EventHandler? = null
    private val logger = StreamLog.getLogger("Chat:EventHandlerProvider")

    /**
     * The [EventHandler]
     */
    internal var eventHandler: EventHandler
        get() = _eventHandler
            ?: throw IllegalStateException(
                "EventHandler was not set in the EventHandlerProvider. " +
                    "Looks like there's a initialisation problem"
            )
        set(value) {
            if (_eventHandler != null) {
                logger.e {
                    "EventHandlerProvider was initialized twice. " +
                        "We shouldn't happen, you may be initializing " +
                        "the SDK twice. Stop listening the old EventHandler..."
                }
                _eventHandler?.stopListening()
            }

            _eventHandler = value
        }
}
