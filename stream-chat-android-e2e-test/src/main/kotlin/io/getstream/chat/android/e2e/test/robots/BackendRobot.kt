package io.getstream.chat.android.e2e.test.robots

import io.getstream.chat.android.compose.uiautomator.mockServer
import io.getstream.chat.android.compose.uiautomator.sleep

public class BackendRobot {

    public fun generateChannels(
        channelsCount: Int,
        messagesCount: Int = 0,
        repliesCount: Int = 0
    ): BackendRobot {
        sleep(2000)
        mockServer.postRequest("mock?" +
            "channels=${channelsCount}&" +
            "messages=${messagesCount}&" +
            "replies=${repliesCount}"
        )
        return this
    }
}