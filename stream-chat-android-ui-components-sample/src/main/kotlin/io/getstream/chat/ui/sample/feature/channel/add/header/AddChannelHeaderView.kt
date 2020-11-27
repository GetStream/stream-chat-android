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

class AddChannelHeaderView : FrameLayout, AddChannelHeader {

    override val viewContext: Context
        get() = context
    override var membersInputListener: MembersInputChangedListener = MembersInputChangedListener { }
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
                membersInputListener.onMembersInputChanged(query)
            }
        }
    }

    override fun setMembers(members: List<User>) {
        adapter.submitList(members)
        binding.membersRecyclerView.isVisible = members.isNotEmpty()
    }

    override fun showInput() {
        binding.inputEditText.isVisible = true
        binding.inputEditText.requestFocus()
        Utils.showSoftKeyboard(binding.inputEditText)
    }

    override fun hideInput() {
        binding.inputEditText.isVisible = false
        Utils.hideSoftKeyboard(binding.inputEditText)
    }

    override fun showAddMemberButton() {
        binding.addMemberButton.isVisible = true
    }

    override fun hideAddMemberButton() {
        binding.addMemberButton.isVisible = false
    }

    override fun setAddMemberButtonClickListener(listener: AddMemberButtonClickListener) {
        binding.addMemberButton.setOnClickListener { listener.onButtonClick() }
    }

    override fun setMemberClickListener(listener: MemberClickListener) {
        adapter.memberClickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inputDebouncer.shutdown()
    }

    companion object {
        private const val TYPING_DEBOUNCE_MS = 300L
    }
}
