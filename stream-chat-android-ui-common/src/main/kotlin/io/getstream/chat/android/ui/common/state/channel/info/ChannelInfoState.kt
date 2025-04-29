package io.getstream.chat.android.ui.common.state.channel.info

import io.getstream.chat.android.models.User

public data class ChannelInfoState(
    val content: Content = Content.Loading,
) {

    public sealed interface Content {
        public data object Loading : Content

        public data class Success(
            val expandedMembers: List<Member>,
            val collapsedMembers: List<Member>,
            val areMembersExpandable: Boolean,
            val areMembersExpanded: Boolean,
        ) : Content

        public data class Error(val message: String) : Content
    }

    public data class Member(
        val user: User,
        val role: Role,
    )

    public sealed interface Role {
        public data object Owner : Role
        public data object Moderator : Role
        public data object Member : Role
        public data class Other(val value: String) : Role
    }
}
