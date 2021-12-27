package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.User

/**
 * Provides sample users that will be used to render component previews.
 */
internal object PreviewUserData {

    /**
     * A collection of users with names and avatars.
     */

    val user1: User = User().apply {
        id = "jc"
        name = "Jc Miñarro"
        image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128"
    }
    val user2: User = User().apply {
        id = "amit"
        name = "Amit Kumar"
        image = "https://ca.slack-edge.com/T02RM6X6B-U027L4AMGQ3-9ca65ea80b60-128"
    }
    val user3: User = User().apply {
        id = "belal"
        name = "Belal Khan"
        image = "https://ca.slack-edge.com/T02RM6X6B-U02DAP0G2AV-2072330222dc-128"
    }
    val user4: User = User().apply {
        id = "dmitrii"
        name = "Dmitrii Bychkov"
        image = "https://ca.slack-edge.com/T02RM6X6B-U01CDPY6YE8-b74b0739493e-128"
    }
    val user5: User = User().apply {
        id = "filip"
        name = "Filip Babić"
        image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128"
    }

    private val user6: User = User().apply {
        id = "jaewoong"
        name = "Jaewoong Eum"
        image = "https://ca.slack-edge.com/T02RM6X6B-U02HU1XR9LM-626fb91c334e-128"
    }

    /**
     * Users with specific properties.
     */

    val userWithImage = user1

    val userWithOnlineStatus = user2.copy(online = true)

    val userWithoutImage = user6.apply { image = "" }
}
