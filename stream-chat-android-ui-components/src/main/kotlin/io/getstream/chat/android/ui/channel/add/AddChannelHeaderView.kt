package io.getstream.chat.android.ui.channel.add

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamAddChannelHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.hideKeyboard
import io.getstream.chat.android.ui.utils.extensions.showKeyboard

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
        // Just for testing purposes - will be removed
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    membersInputListener?.onMemberAdded(query)
                    binding.inputEditText.setText("")
                    binding.inputEditText.hideKeyboard()
                    true
                }
                else -> false
            }
        }
    }

    fun setMembers(members: List<User>) {
        adapter.submitList(members)
        binding.membersRecyclerView.isVisible = members.isNotEmpty()
    }

    fun showInput() {
        binding.inputEditText.isVisible = true
        binding.inputEditText.requestFocus()
        binding.inputEditText.showKeyboard()
    }

    fun hideInput() {
        binding.inputEditText.isVisible = false
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

        // Just for testing purposes - will be removed
        fun onMemberAdded(query: String)
    }

    fun interface AddMemberButtonClickListener {
        fun onButtonClick()
    }
}
