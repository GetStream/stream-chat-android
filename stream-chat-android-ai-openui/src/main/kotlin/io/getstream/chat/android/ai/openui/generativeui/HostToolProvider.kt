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

package io.getstream.chat.android.ai.openui.generativeui

import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ToolProvider

/**
 * Runs the tools of `Query()` and `Mutation()` with the host's tools, on the device. A tool the
 * host has none of, or that fails, throws `AIUIToolException`, which the query reports.
 */
internal class HostToolProvider(private val host: AIUIHost) : ToolProvider {
    override suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue =
        openUIValue(host.runTool(name, arguments.plainValue))
}
