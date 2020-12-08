package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.ui.sample.databinding.ChatInfoDeleteChannelDialogFragmentBinding

internal class ChatInfoDeleteChannelDialogFragment : BottomSheetDialogFragment() {

    private var listener: ChatInfoDeleteChannelListener? = null

    private var _binding: ChatInfoDeleteChannelDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatInfoDeleteChannelDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.confirmButton.setOnClickListener {
            listener?.onDeleteChannel()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setDeleteChannelListener(listener: ChatInfoDeleteChannelListener) {
        this.listener = listener
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
