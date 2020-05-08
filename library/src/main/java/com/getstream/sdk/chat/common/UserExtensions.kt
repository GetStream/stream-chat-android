package com.getstream.sdk.chat.common

import io.getstream.chat.android.client.models.User

var User.image: String
    get() = extraData["image"] as String
    set(value) {
        extraData["image"] = value
    }

var User.name: String
    get() = extraData["name"] as String
    set(value) {
        extraData["name"] = value
    }