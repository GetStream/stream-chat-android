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
    private lateinit var binding: FragmentComponentBrowserMessageListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentComponentBrowserMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupDeletedMessages()
    }

    private fun setupDeletedMessages() {
        binding.deletedMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserDeletedMessages)
        }
    }
}