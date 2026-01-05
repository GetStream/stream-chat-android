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

package io.getstream.chat.ui.sample.feature.chat.info.group.member

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.use
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getColorFromRes
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberOptionBinding

class GroupChatInfoMemberOptionView : FrameLayout {

    private val binding = ChatInfoGroupMemberOptionBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    fun setOnOptionClickListener(listener: OptionClickListener) {
        binding.root.setOnClickListener {
            listener.onClick()
        }
    }

    fun setOnOptionText(text: CharSequence) {
        binding.titleTextView.text = text
    }

    private fun init(attrs: AttributeSet?) {
        attrs ?: return
        context.obtainStyledAttributes(attrs, R.styleable.GroupChatInfoMemberOptionView).use { typedArray ->
            binding.apply {
                iconImageView.setImageResource(
                    typedArray.getResourceId(R.styleable.GroupChatInfoMemberOptionView_optionIcon, R.drawable.ic_cancel),
                )
                typedArray.getColorStateList(R.styleable.GroupChatInfoMemberOptionView_optionIconTint)
                    ?.let { colorStateList ->
                        iconImageView.imageTintList = colorStateList
                    }
                titleTextView.text = typedArray.getString(R.styleable.GroupChatInfoMemberOptionView_optionText)
                titleTextView.setTextColor(
                    typedArray.getColor(
                        R.styleable.GroupChatInfoMemberOptionView_optionTextColor,
                        context.getColorFromRes(R.color.stream_ui_text_color_primary),
                    ),
                )
            }
        }
    }

    fun interface OptionClickListener {
        fun onClick()
    }
}
