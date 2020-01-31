package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Mute
import io.getstream.chat.android.client.User


class MuteUserResponse {
    lateinit var mute: Mute
    lateinit var own_user: User
}
