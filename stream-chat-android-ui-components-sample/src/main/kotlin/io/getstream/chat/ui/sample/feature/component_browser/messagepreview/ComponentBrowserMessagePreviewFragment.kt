package io.getstream.chat.ui.sample.feature.component_browser.messagepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessagePreviewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser
import java.util.Date

class ComponentBrowserMessagePreviewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserMessagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserMessagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messagePreviewView.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(2020, 7, 15, 14, 22),
                text = "Hello world, how are you doing?",
            )
        )
    }
}
