/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.ui.sample.feature.channel.add.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.utils.Debouncer
import io.getstream.chat.ui.sample.databinding.AddChannelHeaderViewBinding
import io.getstream.chat.ui.sample.databinding.AddChannelMemberItemBinding

class AddChannelHeaderView : FrameLayout, AddChannelHeader {

    override val viewContext: Context
        get() = context
    override var membersInputListener: MembersInputChangedListener = MembersInputChangedListener { }
    private var memberClickListener: MemberClickListener? = null
    private val binding = AddChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val inputDebouncer = Debouncer(TYPING_DEBOUNCE_MS)
    private val query: String
        get() = binding.inputEditText.text.trim().toString()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init()
    }

    private fun init() {
        binding.inputEditText.doAfterTextChanged {
            inputDebouncer.submit {
                membersInputListener.onMembersInputChanged(query)
            }
        }
    }

    override fun setMembers(members: List<User>) {
        binding.membersChipGroup.apply {
            removeAllViews()
            members.forEach { member ->
                addView(
                    MemberItemView(context).apply {
                        render(member)
                    },
                )
            }
            isVisible = members.isNotEmpty()
        }
        if (query.isNotEmpty()) {
            binding.inputEditText.setText("")
        }
    }

    override fun showInput() {
        binding.inputEditText.isVisible = true
        binding.inputEditText.requestFocus()
        Utils.showSoftKeyboard(binding.inputEditText)
    }

    override fun hideInput() {
        if (binding.inputEditText.hasFocus()) {
            Utils.hideSoftKeyboard(binding.inputEditText)
        }
        binding.inputEditText.isVisible = false
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

    override fun setMemberClickListener(listener: MemberClickListener?) {
        memberClickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inputDebouncer.shutdown()
    }

    companion object {
        private const val TYPING_DEBOUNCE_MS = 300L
    }

    private inner class MemberItemView : FrameLayout {

        private val binding = AddChannelMemberItemBinding.inflate(LayoutInflater.from(context), this, true)

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr,
        )

        fun render(member: User) {
            binding.memberContainer.setOnClickListener { memberClickListener?.onMemberClicked(member) }
            binding.memberNameTextView.text = member.name
            binding.userAvatarView.setUser(member)
        }
    }
}
