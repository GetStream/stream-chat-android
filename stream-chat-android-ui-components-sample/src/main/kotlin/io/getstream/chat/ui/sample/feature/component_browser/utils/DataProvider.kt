package io.getstream.chat.ui.sample.feature.component_browser.utils

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name

private fun randomImageUrl(): String {
    val category = listOf("men", "women").random()
    val index = (0..99).random()
    return "https://randomuser.me/api/portraits/$category/$index.jpg"
}

internal fun randomUser(withImage: Boolean = true, isOnline: Boolean = true): User {
    return User().apply {
        id = "${('A'..'Z').random()}${('A'..'Z').random()}"
        name = "${('A'..'Z').random()} ${('A'..'Z').random()}"

        if (withImage) {
            image = randomImageUrl()
        }
        if (isOnline) {
            online = true
        }
    }
}

internal fun randomUsers(size: Int = 30): List<User> {
    return 0.until(size).map { randomUser() }
}

internal fun randomChannel(): Channel {
    return Channel().apply {
        name = "Sample Channel"
    }
}
