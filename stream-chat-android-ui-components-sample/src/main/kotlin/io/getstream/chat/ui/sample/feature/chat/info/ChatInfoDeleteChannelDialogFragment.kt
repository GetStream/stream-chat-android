package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ChatInfoDeleteChannelDialogFragmentBinding

internal class ChatInfoDeleteChannelDialogFragment : BottomSheetDialogFragment() {

    var deleteChannelListener: ChatInfoDeleteChannelListener? = null

    private var _binding: ChatInfoDeleteChannelDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDetach() {
        super.onDetach()
        deleteChannelListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatInfoDeleteChannelDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.confirmButton.setOnClickListener {
            deleteChannelListener?.onDeleteChannel()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun interface ChatInfoDeleteChannelListener {
        fun onDeleteChannel()
    }

    companion object {
        const val TAG = "ChatInfoDeleteChannel"

        fun newInstance(): ChatInfoDeleteChannelDialogFragment {
            return ChatInfoDeleteChannelDialogFragment()
        }
    }
}
