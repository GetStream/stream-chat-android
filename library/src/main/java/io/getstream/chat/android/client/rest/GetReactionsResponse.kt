package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Reaction


data class GetReactionsResponse(val reactions: List<Reaction> = emptyList())
