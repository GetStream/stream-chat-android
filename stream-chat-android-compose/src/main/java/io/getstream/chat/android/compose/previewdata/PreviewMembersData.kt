package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.client.models.Member

/**
 * Provides sample members that will be used to render previews.
 */
@Suppress("MemberVisibilityCanBePrivate")
internal object PreviewMembersData {

    val member1: Member = Member(user = PreviewUserData.user1)
    val member2: Member = Member(user = PreviewUserData.user2)
    val member3: Member = Member(user = PreviewUserData.user3)
    val member4: Member = Member(user = PreviewUserData.user4)

    val oneMember: List<Member> = listOf(member1)

    val manyMembers: List<Member> = listOf(
        member1,
        member2,
        member3,
        member4
    )
}
