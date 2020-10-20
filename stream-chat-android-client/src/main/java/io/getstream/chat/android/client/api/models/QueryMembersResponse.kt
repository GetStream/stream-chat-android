package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Member

internal data class QueryMembersResponse(val members: List<Member> = emptyList())
