package io.getstream.chat.ui.sample.feature.channel.add

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
import io.getstream.chat.android.ui.textinput.MessageInputView
import io.getstream.chat.ui.sample.databinding.AddChannelEmptyMessageListViewBinding
import io.getstream.chat.ui.sample.databinding.AddChannelEmptyUsersViewBinding
import io.getstream.chat.ui.sample.databinding.AddChannelViewBinding
import io.getstream.chat.ui.sample.feature.channel.add.AddChannelView.EndReachedListener

class AddChannelView : FrameLayout {

    private val binding = AddChannelViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val controller =
        AddChannelViewController(
            binding.headerView,
            binding.usersTitle,
            binding.usersRecyclerView,
            binding.createGroupContainer
        )
    private var loadingView: View = defaultLoadingView()
    private var emptyStateView: View = defaultEmptyStateView()
    var endReachedListener: EndReachedListener = EndReachedListener { }
    private val endReachedScrollListener = EndReachedScrollListener()
    private var paginationEnabled: Boolean = false

    val messageListView: MessageListView
        get() = binding.messageListView

    val messageInputView: MessageInputView
        get() = binding.messageInputView

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
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
            AddChannelEmptyMessageListViewBinding.inflate(
                LayoutInflater.from(
                    context
                )
            ).root
        )
    }

    fun setUsers(users: List<User>) {
        controller.setUsers(users)
    }

    fun addMoreUsers(users: List<User>) {
        controller.addMoreUsers(users)
    }

    fun setOnCreateGroupButtonListener(listener: CreateGroupButtonClickListener) {
        binding.createGroupButton.setOnClickListener { listener.onButtonClick() }
    }

    fun hideLoadingView() {
        loadingView.isVisible = false
    }

    fun showLoadingView() {
        loadingView.isVisible = true
    }

    fun hideEmptyStateView() {
        emptyStateView.isVisible = false
    }

    fun showEmptyStateView() {
        emptyStateView.isVisible = true
    }

    fun showUsersRecyclerView() {
        binding.usersRecyclerView.isVisible = true
    }

    fun hideUsersRecyclerView() {
        binding.usersRecyclerView.isVisible = false
    }

    fun showMessageListView() {
        binding.messageListView.isVisible = true
    }

    fun hideMessageListView() {
        binding.messageListView.isVisible = false
    }

    fun setPaginationEnabled(enabled: Boolean) {
        paginationEnabled = enabled
    }

    fun setMembersChangedListener(listener: MembersChangedListener) {
        controller.membersChangedListener = listener
    }

    fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) {
        controller.addMemberButtonClickListener = listener
    }

    fun interface EndReachedListener {
        fun onEndReached()
    }

    fun interface CreateGroupButtonClickListener {
        fun onButtonClick()
    }

    fun interface MembersChangedListener {
        fun onMembersChanged(members: List<User>)
    }

    fun interface AddMemberButtonClickListener {
        fun onAddMemberButtonClicked()
    }

    private fun defaultLoadingView(): View = ProgressBar(context)

    private fun defaultEmptyStateView(): View =
        AddChannelEmptyUsersViewBinding.inflate(LayoutInflater.from(context)).root

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
