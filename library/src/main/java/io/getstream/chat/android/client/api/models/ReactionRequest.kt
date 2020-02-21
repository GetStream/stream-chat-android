package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Reaction


class ReactionRequest(reaction: Reaction) {

    val reaction: Map<String, Any>

    init {
        this.reaction = reaction.extraData.toMutableMap()
        this.reaction["type"] = reaction.type
    }
}