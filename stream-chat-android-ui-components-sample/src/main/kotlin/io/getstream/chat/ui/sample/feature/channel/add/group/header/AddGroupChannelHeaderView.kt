package io.getstream.chat.ui.sample.feature.channel.add.group.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.User
import io.getstream.chat.ui.sample.databinding.AddGroupChannelHeaderViewBinding
import io.getstream.chat.ui.sample.feature.channel.add.header.AddChannelHeader
import io.getstream.chat.ui.sample.feature.channel.add.header.AddMemberButtonClickListener
import io.getstream.chat.ui.sample.feature.channel.add.header.MemberClickListener
import io.getstream.chat.ui.sample.feature.channel.add.header.MembersInputChangedListener

class AddGroupChannelHeaderView : FrameLayout, AddChannelHeader {

    override val viewContext: Context
        get() = context
    override var membersInputListener: MembersInputChangedListener = MembersInputChangedListener { }
    private val binding = AddGroupChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = AddGroupChannelMembersAdapter()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        binding.membersRecyclerView.adapter = adapter
        binding.usersSearchView.setDebouncedInputChangedListener {
            membersInputListener.onMembersInputChanged(it)
        }
    }

    override fun setMembers(members: List<User>) {
        adapter.submitList(members)
        binding.membersRecyclerView.isVisible = members.isNotEmpty()
    }

    override fun showInput() = Unit

    override fun hideInput() = Unit

    override fun showAddMemberButton() = Unit

    override fun hideAddMemberButton() = Unit

    override fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) = Unit

    override fun setMemberClickListener(listener: MemberClickListener) {
        adapter.memberClickListener = listener
    }
}
