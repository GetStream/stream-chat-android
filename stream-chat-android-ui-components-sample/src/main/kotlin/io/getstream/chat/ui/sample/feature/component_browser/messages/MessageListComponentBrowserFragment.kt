package io.getstream.chat.ui.sample.feature.component_browser.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListBinding

class MessageListComponentBrowserFragment : Fragment() {
    private lateinit var binding: FragmentComponentBrowserMessageListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentComponentBrowserMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }
}