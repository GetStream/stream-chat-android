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

package io.getstream.chat.android.e2e.test.robots

import io.getstream.chat.android.compose.uiautomator.sleep
import io.getstream.chat.android.e2e.test.mockserver.MockServer
import java.net.HttpURLConnection
import java.net.URL

public class BackendRobot(
    private val mockServer: MockServer,
) {

    public fun generateChannels(
        channelsCount: Int,
        messagesCount: Int = 0,
        repliesCount: Int = 0,
        messagesText: String? = null,
        repliesText: String? = null,
    ): BackendRobot {
        waitForMockServerToStart()
        val messagesTextQueryParam = if (messagesText != null) "messages_text=$messagesText&" else ""
        val repliesTextQueryParam = if (repliesText != null) "replies_text=$repliesText&" else ""
        mockServer.postRequest(
            "mock?" +
                messagesTextQueryParam +
                repliesTextQueryParam +
                "channels=$channelsCount&" +
                "messages=$messagesCount&" +
                "replies=$repliesCount",
        )
        return this
    }

    public fun failNewMessages(): BackendRobot {
        mockServer.postRequest("fail_messages")
        return this
    }

    public fun freezeNewMessages(): BackendRobot {
        mockServer.postRequest("freeze_messages")
        return this
    }

    public fun revokeJwt(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/revoke?duration=$duration")
    }

    public fun breakJwt(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/break?duration=$duration")
    }

    private fun waitForMockServerToStart() {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 5000) {
            try {
                val connection = URL("${mockServer.url}/ping").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 1000
                connection.readTimeout = 1000

                if (connection.responseCode == 200) {
                    return
                }
            } catch (e: Exception) {
                Thread.sleep(500)
            }
        }
        throw RuntimeException("MockServer did not start within 5 seconds")
    }
}
