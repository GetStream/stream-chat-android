package io.getstream.chat.android.ui.channel.add

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.MessageListView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.channel.add.AddChannelView.EndReachedListener
import io.getstream.chat.android.ui.databinding.StreamAddChannelEmptyMessageListViewBinding
import io.getstream.chat.android.ui.databinding.StreamAddChannelEmptyUsersViewBinding
import io.getstream.chat.android.ui.databinding.StreamAddChannelViewBinding
import io.getstream.chat.android.ui.textinput.MessageInputView

public class AddChannelView : FrameLayout {

    private val binding = StreamAddChannelViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val controller =
        AddChannelViewController(
            binding.headerView,
            binding.usersTitle,
            binding.usersRecyclerView,
            binding.createGroupContainer
        )
    private var loadingView: View = defaultLoadingView()
    private var emptyStateView: View = defaultEmptyStateView()
    public var endReachedListener: EndReachedListener = EndReachedListener { }
    private val endReachedScrollListener = EndReachedScrollListener()
    private var paginationEnabled: Boolean = false

    public val messageListView: MessageListView
        get() = binding.messageListView

    public val messageInputView: MessageInputView
        get() = binding.messageInputView

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        binding.usersRecyclerView.addOnScrollListener(endReachedScrollListener)
        loadingView.apply {
            isVisible = false
            addView(this, defaultChildLayoutParams())
        }
        emptyStateView.apply {
            isVisible = false
            addView(this, defaultChildLayoutParams())
        }
        binding.messageListView.setEmptyStateView(
            StreamAddChannelEmptyMessageListViewBinding.inflate(
                LayoutInflater.from(
                    context
                )
            ).root
        )
    }

    public fun setUsers(users: List<User>) {
        controller.setUsers(users)
    }

    public fun addMoreUsers(users: List<User>) {
        controller.addMoreUsers(users)
    }

    public fun setOnCreateGroupButtonListener(listener: CreateGroupButtonClickListener) {
        binding.createGroupButton.setOnClickListener { listener.onButtonClick() }
    }

    public fun hideLoadingView() {
        loadingView.isVisible = false
    }

    public fun showLoadingView() {
        loadingView.isVisible = true
    }

    public fun hideEmptyStateView() {
        emptyStateView.isVisible = false
    }

    public fun showEmptyStateView() {
        emptyStateView.isVisible = true
    }

    public fun showUsersRecyclerView() {
        binding.usersRecyclerView.isVisible = true
    }

    public fun hideUsersRecyclerView() {
        binding.usersRecyclerView.isVisible = false
    }

    public fun showMessageListView() {
        binding.messageListView.isVisible = true
    }

    public fun hideMessageListView() {
        binding.messageListView.isVisible = false
    }

    public fun setPaginationEnabled(enabled: Boolean) {
        paginationEnabled = enabled
    }

    public fun setMembersChangedListener(listener: MembersChangedListener) {
        controller.membersChangedListener = listener
    }

    public fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) {
        controller.addMemberButtonClickListener = listener
    }

    public fun interface EndReachedListener {
        public fun onEndReached()
    }

    public fun interface CreateGroupButtonClickListener {
        public fun onButtonClick()
    }

    public fun interface MembersChangedListener {
        public fun onMembersChanged(members: List<User>)
    }

    public fun interface AddMemberButtonClickListener {
        public fun onAddMemberButtonClicked()
    }

    private fun defaultLoadingView(): View = ProgressBar(context)

    private fun defaultEmptyStateView(): View =
        StreamAddChannelEmptyUsersViewBinding.inflate(LayoutInflater.from(context)).root

    private fun defaultChildLayoutParams() =
        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)

    private inner class EndReachedScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
                val reachedTheEnd = recyclerView.adapter!!.itemCount - 1 == lastVisiblePosition
                if (reachedTheEnd && paginationEnabled) {
                    endReachedListener.onEndReached()
                }
            }
        }
    }
}
