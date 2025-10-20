/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.common

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.models.Member
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getColorFromRes
import io.getstream.chat.ui.sample.databinding.ConfirmationDialogFragmentBinding

internal class ConfirmationDialogFragment : BottomSheetDialogFragment() {

    var confirmClickListener: ConfirmClickListener? = null
    var cancelClickListener: CancelClickListener? = null
    var onDismissListener: ((dialog: DialogInterface) -> Unit)? = null

    private val iconResId: Int by lazy { requireArguments().getInt(ARG_ICON_RES_ID) }
    private val iconTintResId: Int by lazy { requireArguments().getInt(ARG_ICON_TINT_RES_ID) }
    private val title: String by lazy { requireArguments().getString(ARG_TITLE)!! }
    private val description: String by lazy { requireArguments().getString(ARG_DESCRIPTION)!! }
    private val confirmText: String by lazy { requireArguments().getString(ARG_CONFIRM_TEXT)!! }
    private val cancelText: String by lazy { requireArguments().getString(ARG_CANCEL_TEXT)!! }
    private val hasConfirmButton: Boolean by lazy { requireArguments().getBoolean(ARG_HAS_CONFIRM_BUTTON, true) }

    private var _binding: ConfirmationDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDetach() {
        super.onDetach()
        confirmClickListener = null
        cancelClickListener = null
        onDismissListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ConfirmationDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            iconImageView.apply {
                setImageResource(iconResId)
                setColorFilter(context.getColorFromRes(iconTintResId))
            }
            titleTextView.text = title
            descriptionTextView.text = description
            cancelButton.text = cancelText
            cancelButton.setOnClickListener {
                cancelClickListener?.onClick()
                dismiss()
            }
            if (hasConfirmButton) {
                confirmButton.apply {
                    isVisible = true
                    text = confirmText
                    setOnClickListener {
                        confirmClickListener?.onClick()
                        dismiss()
                    }
                }
            } else {
                confirmButton.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCancel(dialog: DialogInterface) {
        onDismissListener?.invoke(dialog)
    }

    fun interface ConfirmClickListener {
        fun onClick()
    }

    fun interface CancelClickListener {
        fun onClick()
    }

    companion object {
        const val TAG = "ConfirmationDialogFragment"
        private const val ARG_ICON_RES_ID = "icon_res_id"
        private const val ARG_ICON_TINT_RES_ID = "icon_tint_res_id"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_CONFIRM_TEXT = "confirm_text"
        private const val ARG_CANCEL_TEXT = "cancel_text"
        private const val ARG_HAS_CONFIRM_BUTTON = "has_confirm_button"

        fun newDeleteMessageInstance(context: Context): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.ic_delete,
            iconTintResId = R.color.red,
            title = context.getString(R.string.stream_ui_message_list_delete_confirmation_title),
            description = context.getString(R.string.stream_ui_message_list_delete_confirmation_message),
            confirmText = context.getString(R.string.stream_ui_message_list_delete_confirmation_positive_button),
            cancelText = context.getString(R.string.stream_ui_message_list_delete_confirmation_negative_button),
        )

        fun newDeleteChannelInstance(context: Context, isGroupChannel: Boolean): ConfirmationDialogFragment =
            newInstance(
                iconResId = R.drawable.ic_delete,
                iconTintResId = R.color.red,
                title = if (isGroupChannel) {
                    context.getString(R.string.stream_ui_channel_info_option_delete_group)
                } else {
                    context.getString(R.string.stream_ui_channel_info_option_delete_conversation)
                },
                description = if (isGroupChannel) {
                    context.getString(R.string.stream_ui_channel_info_delete_group_modal_message)
                } else {
                    context.getString(R.string.stream_ui_channel_info_delete_conversation_modal_message)
                },
                confirmText = context.getString(R.string.delete),
                cancelText = context.getString(R.string.cancel),
            )

        fun newLeaveChannelInstance(context: Context, isGroupChannel: Boolean): ConfirmationDialogFragment =
            newInstance(
                iconResId = R.drawable.ic_leave_group,
                iconTintResId = R.color.red,
                title = if (isGroupChannel) {
                    context.getString(R.string.stream_ui_channel_info_option_leave_group)
                } else {
                    context.getString(R.string.stream_ui_channel_info_option_leave_conversation)
                },
                description = if (isGroupChannel) {
                    context.getString(R.string.stream_ui_channel_info_leave_group_modal_message)
                } else {
                    context.getString(R.string.stream_ui_channel_info_leave_conversation_modal_message)
                },
                confirmText = context.getString(R.string.leave),
                cancelText = context.getString(R.string.cancel),
            )

        fun newHideChannelInstance(context: Context, isGroupChannel: Boolean): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.stream_ic_hide,
            iconTintResId = R.color.stream_ui_grey,
            title = if (isGroupChannel) {
                context.getString(R.string.stream_ui_channel_info_option_hide_group)
            } else {
                context.getString(R.string.stream_ui_channel_info_option_hide_conversation)
            },
            description = if (isGroupChannel) {
                context.getString(R.string.stream_ui_channel_info_hide_group_modal_message)
            } else {
                context.getString(R.string.stream_ui_channel_info_hide_conversation_modal_message)
            },
            confirmText = context.getString(R.string.clear_history),
            cancelText = context.getString(R.string.keep_history),
        )

        fun newRemoveMemberInstance(context: Context, member: Member): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.ic_delete,
            iconTintResId = R.color.red,
            title = context.getString(R.string.stream_ui_channel_info_member_modal_option_remove_member),
            description = context.getString(
                R.string.stream_ui_channel_info_remove_member_modal_message,
                member.user.name,
            ),
            confirmText = context.getString(R.string.remove),
            cancelText = context.getString(R.string.cancel),
        )

        fun newFlagMessageInstance(context: Context): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.stream_ui_ic_flag,
            iconTintResId = R.color.red,
            title = context.getString(R.string.stream_ui_message_list_flag_confirmation_title),
            description = context.getString(R.string.stream_ui_message_list_flag_confirmation_message),
            confirmText = context.getString(R.string.stream_ui_message_list_flag_confirmation_positive_button),
            cancelText = context.getString(R.string.cancel),
        )

        fun newMessageFlaggedInstance(context: Context): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.stream_ui_ic_flag,
            iconTintResId = R.color.red,
            title = context.getString(R.string.message_flagged_title),
            description = context.getString(R.string.message_flagged_description),
            confirmText = context.getString(R.string.ok),
            cancelText = context.getString(R.string.ok),
            hasConfirmButton = false,
        )

        fun newDeleteMessageForMeInstance(context: Context): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.ic_delete,
            iconTintResId = R.color.red,
            title = "Delete for Me",
            description = "Are you sure you want to delete this message for you?",
            confirmText = context.getString(R.string.stream_ui_message_list_delete_confirmation_positive_button),
            cancelText = context.getString(R.string.cancel),
        )

        fun newInstance(
            @DrawableRes iconResId: Int,
            @ColorRes iconTintResId: Int,
            title: String,
            description: String,
            confirmText: String,
            cancelText: String,
            hasConfirmButton: Boolean = true,
        ): ConfirmationDialogFragment = ConfirmationDialogFragment().apply {
            arguments = bundleOf(
                ARG_ICON_RES_ID to iconResId,
                ARG_ICON_TINT_RES_ID to iconTintResId,
                ARG_TITLE to title,
                ARG_DESCRIPTION to description,
                ARG_CONFIRM_TEXT to confirmText,
                ARG_CANCEL_TEXT to cancelText,
                ARG_HAS_CONFIRM_BUTTON to hasConfirmButton,
            )
        }
    }
}
