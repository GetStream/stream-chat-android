package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User

internal fun Collection<Member>.updateUsers(userMap: Map<String, User>): Collection<Member> = map { member ->
    if (userMap.containsKey(member.getUserId())) {
        member.copy(user = userMap[member.getUserId()] ?: member.user)
    } else {
        member
    }
}
