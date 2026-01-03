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

package io.getstream.chat.ui.sample.feature.chat.info.group

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.ui.sample.common.hideKeyboard
import io.getstream.chat.ui.sample.common.showKeyboard
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupEditNameViewBinding

class GroupChatEditNameView : FrameLayout {

    private val binding = ChatInfoGroupEditNameViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var nameChangedListener: GroupNameChangedListener? = null

    private var shouldUpdateCurrentName: Boolean = true
    private var currentName: String = ""

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

    fun init() {
        binding.cancelButton.setOnClickListener {
            binding.nameEditText.clearFocus()
        }
        binding.checkButton.setOnClickListener {
            updateChannelName()
        }
        binding.nameEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    updateChannelName()
                    true
                }
                else -> false
            }
        }
        binding.nameEditText.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                binding.actionButtons.isVisible = hasFocus
                if (hasFocus) {
                    shouldUpdateCurrentName = true
                    binding.nameEditText.showKeyboard()
                } else {
                    if (shouldUpdateCurrentName) {
                        binding.nameEditText.setText(currentName)
                    }
                    binding.nameEditText.hideKeyboard()
                }
            }
    }

    fun setChannelName(name: String) {
        binding.nameEditText.setText(name)
        currentName = name
    }

    fun setGroupNameChangedListener(listener: GroupNameChangedListener?) {
        nameChangedListener = listener
    }

    private fun updateChannelName() {
        shouldUpdateCurrentName = false
        nameChangedListener?.onNameChanged(binding.nameEditText.text.trim().toString())
        binding.nameEditText.clearFocus()
    }

    fun interface GroupNameChangedListener {
        fun onNameChanged(name: String)
    }
}
