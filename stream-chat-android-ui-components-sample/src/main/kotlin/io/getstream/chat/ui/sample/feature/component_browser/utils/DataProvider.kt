package io.getstream.chat.ui.sample.feature.component_browser.utils

import android.net.Uri
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.utils.ReactionType
import java.util.Random

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

internal fun randomChannel(members: List<Member> = emptyList()): Channel {
    return Channel().apply {
        cid = "${('A'..'Z').random()} ${('A'..'Z').random()}"
        name = "Sample Channel"
        this.members = members
    }
}

internal fun randomMember(withImage: Boolean = true): Member {
    return Member(user = randomUser(withImage))
}

internal fun randomMessageWithReactions(reactionsSize: Int, ownReactionsSize: Int): Message {
    if (ownReactionsSize > reactionsSize) {
        throw IllegalArgumentException("Own reactions count must not exceed the total count")
    }
    return randomMessage().apply {
        latestReactions = randomReactions(size = reactionsSize).toMutableList()
        ownReactions = latestReactions.shuffled()
            .take(ownReactionsSize)
            .toMutableList()
    }
}

internal fun randomMessage(): Message {
    return Message().apply {
        text = "Random message"
    }
}

@InternalStreamChatApi
internal fun randomMediaAttachments(count: Int): List<AttachmentMetaData> {
    return List(count) {
        AttachmentMetaData(
            uri = Uri.parse(randomImageUrl()),
            type = "image",
            mimeType = "image/png"
        )
    }
}

@InternalStreamChatApi
internal fun randomFileAttachments(count: Int): List<AttachmentMetaData> {
    return List(count) {
        AttachmentMetaData(
            uri = Uri.parse(randomImageUrl()),
            type = "file",
            mimeType = "application/pdf",
        ).apply {
            size = 100000L
            title = "Sample PDF"
        }
    }
}

internal fun randomCommand(): Command {
    return Command("giphy", "Post a random gif to the channel", "[text]", "fun_set")
}

private fun randomReactions(size: Int): List<Reaction> {
    return List(size) {
        val randomReactionType = ReactionType.values()[Random().nextInt(ReactionType.values().size)]
        Reaction(user = randomUser(), type = randomReactionType.type)
    }
}

private fun randomImageUrl(): String {
    val category = listOf("men", "women").random()
    val index = (0..99).random()
    return "https://randomuser.me/api/portraits/$category/$index.jpg"
}
