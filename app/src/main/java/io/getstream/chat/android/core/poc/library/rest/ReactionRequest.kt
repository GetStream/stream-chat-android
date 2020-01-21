package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.Reaction


class ReactionRequest(reaction: Reaction) {

    @SerializedName("reaction")
    val data: Map<String, Any>

    init {
        this.data = reaction.extraData.toMutableMap()
        this.data["type"] = reaction.type
    }
}