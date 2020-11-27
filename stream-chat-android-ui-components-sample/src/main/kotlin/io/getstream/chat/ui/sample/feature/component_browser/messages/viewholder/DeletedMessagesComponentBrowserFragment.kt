package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentDeletedMessageComponentBrowserBinding

class DeletedMessagesComponentBrowserFragment : Fragment() {

    private lateinit var binding: FragmentDeletedMessageComponentBrowserBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDeletedMessageComponentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }
}