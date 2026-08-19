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

package io.getstream.chat.android.e2e.test.robots

import io.getstream.chat.android.e2e.test.mockserver.MockServer
import junit.framework.TestCase.fail
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

public class BackendRobot(
    private val mockServer: MockServer,
) {

    public fun generateChannels(
        channelsCount: Int,
        messagesCount: Int = 0,
        repliesCount: Int = 0,
        messagesText: String? = null,
        repliesText: String? = null,
        channelNames: List<String> = emptyList(),
        withDirectMessageChannel: Boolean = false,
    ): BackendRobot {
        waitForMockServerToStart()
        val messagesTextQueryParam = if (messagesText != null) "messages_text=$messagesText&" else ""
        val repliesTextQueryParam = if (repliesText != null) "replies_text=$repliesText&" else ""
        val channelNamesQueryParam = if (channelNames.isNotEmpty()) {
            "channel_names=${URLEncoder.encode(channelNames.joinToString(","), "UTF-8")}&"
        } else {
            ""
        }
        val dmQueryParam = if (withDirectMessageChannel) "dm=true&" else ""
        mockServer.postRequest(
            "mock?" +
                messagesTextQueryParam +
                repliesTextQueryParam +
                channelNamesQueryParam +
                dmQueryParam +
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

    public fun setCooldown(enabled: Boolean, duration: Int): BackendRobot {
        waitForMockServerToStart()
        mockServer.postRequest("config/cooldown?enabled=$enabled&duration=$duration")
        return this
    }

    /**
     * Truncates the currently open channel on the server side. The app under test receives
     * the `channel.truncated` websocket event.
     *
     * @param withMessage When `true`, the truncation also delivers a "Channel truncated" system message.
     */
    public fun truncateChannel(withMessage: Boolean): BackendRobot {
        mockServer.postRequest("truncate_channel?with_message=$withMessage")
        return this
    }

    /**
     * Enables or disables read events on every channel. Call before the channel is opened.
     *
     * @param enabled Whether the channel config reports `read_events`.
     */
    public fun setReadEvents(enabled: Boolean): BackendRobot {
        waitForMockServerToStart()
        mockServer.postRequest("config/read_events?value=$enabled")
        return this
    }

    /**
     * Adds a member to the currently open channel on the server side. The app under test
     * receives the `member.added` and `channel.updated` websocket events.
     *
     * @param userId The id of the user to add.
     */
    public fun addMember(userId: String): BackendRobot {
        mockServer.postRequest("add_member?user_id=$userId")
        return this
    }

    /**
     * Removes a member from the currently open channel on the server side. The app under test
     * receives the `member.removed` and `channel.updated` websocket events.
     *
     * @param userId The id of the user to remove.
     */
    public fun removeMember(userId: String): BackendRobot {
        mockServer.postRequest("remove_member?user_id=$userId")
        return this
    }

    /**
     * Creates a reminder for the last message of the currently open channel on the server side.
     * The app under test has no way to create a reminder, so tests seed it here.
     *
     * @param remindAtSeconds How far in the future the reminder is due, in seconds. Pass `null`
     * for a reminder that is saved for later, which has no due date.
     */
    public fun createReminder(remindAtSeconds: Int? = null): BackendRobot {
        val endpoint = if (remindAtSeconds == null) {
            "create_reminder"
        } else {
            "create_reminder?remind_at=$remindAtSeconds"
        }
        mockServer.postRequest(endpoint)
        return this
    }

    public fun revokeToken(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/revoke_token?duration=$duration")
    }

    public fun invalidateToken(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/invalidate_token?duration=$duration")
    }

    public fun invalidateTokenDate(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/invalidate_token_date?duration=$duration")
    }

    public fun invalidateTokenSignature(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/invalidate_token_signature?duration=$duration")
    }

    public fun breakTokenGeneration(duration: Int = 5) {
        waitForMockServerToStart()
        mockServer.postRequest("jwt/break_token_generation?duration=$duration")
    }

    /**
     * Waits until the app under test closes its WebSocket connection to the mock server.
     * The SDK keeps the socket open for a short period after the app goes to background,
     * so call this before triggering server-side events that must not be delivered live.
     *
     * @param timeoutMillis How long to wait for the disconnect before failing the test.
     */
    public fun waitForWebSocketDisconnection(timeoutMillis: Long = 10_000) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            val status = mockServer.getRequest("ws/status")?.string()
            if (status != null && !JSONObject(status).getBoolean("connected")) {
                return
            }
            Thread.sleep(500)
        }
        fail("WebSocket was not disconnected within $timeoutMillis ms")
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
            } catch (_: Exception) {
                Thread.sleep(500)
            }
        }
        fail("MockServer did not start within 5 seconds")
    }
}
