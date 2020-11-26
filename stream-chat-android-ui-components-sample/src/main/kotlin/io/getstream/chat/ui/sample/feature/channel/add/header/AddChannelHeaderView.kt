package io.getstream.chat.ui.sample.feature.channel.add.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.utils.Debouncer
import io.getstream.chat.ui.sample.databinding.AddChannelHeaderViewBinding

class AddChannelHeaderView : FrameLayout {

    var membersInputListener: Listener? = null
    private val binding = AddChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = AddChannelMembersAdapter()
    private val inputDebouncer = Debouncer(TYPING_DEBOUNCE_MS)
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
            inputDebouncer.submit {
                membersInputListener?.onInputChanged(query)
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
        Utils.showSoftKeyboard(binding.inputEditText)
    }

    fun hideInput() {
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

    fun setMemberClickListener(listener: AddChannelMembersAdapter.MemberClickListener) {
        adapter.memberClickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inputDebouncer.shutdown()
    }

    interface Listener {
        fun onInputChanged(query: String)
    }

    fun interface AddMemberButtonClickListener {
        fun onButtonClick()
    }

    companion object {
        private const val TYPING_DEBOUNCE_MS = 300L
    }
}
