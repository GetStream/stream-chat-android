package io.getstream.chat.ui.sample.feature.component_browser.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListBinding

class MessageListComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComponentBrowserMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.deletedMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserDeletedMessages)
        }
        binding.dateDivider.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserDateDividerFragment)
        }
        binding.plainTextMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserPlainTextMessages)
        }
        binding.onlyMediaAttachments.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserOnlyMediaAttachmentsMessages)
        }
    }
}
