package io.getstream.chat.ui.sample.feature.component_browser.textinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.textinput.MessageInputFieldView
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageInputFieldViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomCommand
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomFileAttachments
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMediaAttachments
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

class ComponentBrowserMessageInputFieldViewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserMessageInputFieldViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentComponentBrowserMessageInputFieldViewBinding
            .inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @InternalStreamChatApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messageInputFieldView.mode = MessageInputFieldView.Mode.MessageMode
        binding.messageInputFieldViewEdit.mode = MessageInputFieldView.Mode.EditMessageMode(randomMessage())
        binding.messageInputFieldViewCommand.mode = MessageInputFieldView.Mode.CommandMode(randomCommand())
        binding.messageInputFieldViewFile.mode = MessageInputFieldView.Mode.FileAttachmentMode(randomFileAttachments(3))
        binding.messageInputFieldViewMedia.mode = MessageInputFieldView.Mode.MediaAttachmentMode(randomMediaAttachments(3))
    }
}
