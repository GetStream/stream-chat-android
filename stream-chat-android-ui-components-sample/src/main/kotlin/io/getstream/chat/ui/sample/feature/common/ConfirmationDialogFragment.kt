package io.getstream.chat.ui.sample.feature.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getColorFromRes
import io.getstream.chat.ui.sample.databinding.ConfirmationDialogFragmentBinding

internal class ConfirmationDialogFragment : BottomSheetDialogFragment() {

    var confirmClickListener: ConfirmClickListener? = null

    private val iconResId: Int by lazy { requireArguments().getInt(ARG_ICON_RES_ID) }
    private val iconTintResId: Int by lazy { requireArguments().getInt(ARG_ICON_TINT_RES_ID) }
    private val title: String by lazy { requireArguments().getString(ARG_TITLE)!! }
    private val description: String by lazy { requireArguments().getString(ARG_DESCRIPTION)!! }
    private val confirmText: String by lazy { requireArguments().getString(ARG_CONFIRM_TEXT)!! }

    private var _binding: ConfirmationDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDetach() {
        super.onDetach()
        confirmClickListener = null
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
            confirmButton.text = confirmText
            cancelButton.setOnClickListener { dismiss() }
            confirmButton.setOnClickListener {
                confirmClickListener?.onClick()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun interface ConfirmClickListener {
        fun onClick()
    }

    companion object {
        const val TAG = "ConfirmationDialogFragment"
        private const val ARG_ICON_RES_ID = "icon_res_id"
        private const val ARG_ICON_TINT_RES_ID = "icon_tint_res_id"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_CONFIRM_TEXT = "confirm_text"

        fun newDeleteChannelInstance(context: Context): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.ic_delete_contact,
            iconTintResId = R.color.red,
            title = context.getString(R.string.chat_info_option_delete_conversation),
            description = context.getString(R.string.chat_info_delete_conversation_confirm),
            confirmText = context.getString(R.string.delete),
        )

        fun newLeaveChannelInstance(context: Context, channelName: String): ConfirmationDialogFragment = newInstance(
            iconResId = R.drawable.ic_leave_group,
            iconTintResId = R.color.stream_ui_boulder,
            title = context.getString(R.string.chat_group_info_option_leave),
            description = context.getString(R.string.chat_group_info_leave_confirm, channelName),
            confirmText = context.getString(R.string.leave),
        )

        private fun newInstance(
            @DrawableRes iconResId: Int,
            @ColorRes iconTintResId: Int,
            title: String,
            description: String,
            confirmText: String,
        ): ConfirmationDialogFragment = ConfirmationDialogFragment().apply {
            arguments = bundleOf(
                ARG_ICON_RES_ID to iconResId,
                ARG_ICON_TINT_RES_ID to iconTintResId,
                ARG_TITLE to title,
                ARG_DESCRIPTION to description,
                ARG_CONFIRM_TEXT to confirmText,
            )
        }
    }
}
