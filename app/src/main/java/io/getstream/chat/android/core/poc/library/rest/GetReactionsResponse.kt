package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Reaction


data class GetReactionsResponse(val reactions: List<Reaction> = emptyList())
