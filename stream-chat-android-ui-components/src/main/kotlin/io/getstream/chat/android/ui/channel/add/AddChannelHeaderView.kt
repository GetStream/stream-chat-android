package io.getstream.chat.android.ui.channel.add

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamAddChannelHeaderViewBinding

internal class AddChannelHeaderView : FrameLayout {

    var membersInputListener: Listener? = null
    private val binding = StreamAddChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = AddChannelMembersAdapter()
    private val query: String
        get() = binding.inputEditText.text.trim().toString()

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
        binding.membersRecyclerView.adapter = adapter
        binding.inputEditText.doAfterTextChanged {
            membersInputListener?.onInputChanged(query)
        }
    }

    fun setMembers(members: List<User>) {
        adapter.submitList(members)
        binding.membersRecyclerView.isVisible = members.isNotEmpty()
    }

    fun showInput() {
        binding.inputEditText.isVisible = true
        binding.inputEditText.requestFocus()
        Utils.showSoftKeyboard(binding.inputEditText)
    }

    fun hideInput() {
        binding.inputEditText.setText("")
        binding.inputEditText.isVisible = false
        Utils.hideSoftKeyboard(binding.inputEditText)
    }

    fun showAddMemberButton() {
        binding.addMemberButton.isVisible = true
    }

    fun hideAddMemberButton() {
        binding.addMemberButton.isVisible = false
    }

    fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) {
        binding.addMemberButton.setOnClickListener { listener.onButtonClick() }
    }

    interface Listener {
        fun onInputChanged(query: String)
    }

    fun interface AddMemberButtonClickListener {
        fun onButtonClick()
    }
}
