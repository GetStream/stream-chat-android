package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Mute
import io.getstream.chat.android.core.poc.library.User


class MuteUserResponse {
    lateinit var mute: Mute
    lateinit var own_user: User
}
