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

package io.getstream.chat.android.models

/**
 * The state of initialization process.
 */
public enum class InitializationState {

    /**
     * Initialization is complete. Be aware that it doesn't mean that the SDK is connected. To track
     * the connection state, please use [ClientState.connectionState]
     */
    COMPLETE,

    /**
     * Initialization was requested and should be completed shortly. During this state, the SDK is still
     * not ready to be used.
     */
    INITIALIZING,

    /**
     * The initialization of the SDK was not requested. Use ChatClient.connectUser to start the
     * initialization process.
     */
    NOT_INITIALIZED,
}
