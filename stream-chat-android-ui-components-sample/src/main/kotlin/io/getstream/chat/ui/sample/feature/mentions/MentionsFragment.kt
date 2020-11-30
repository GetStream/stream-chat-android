package io.getstream.chat.ui.sample.feature.mentions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.ui.mentions.MentionsListViewModel
import io.getstream.chat.android.ui.mentions.bindView
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentMentionsBinding

class MentionsFragment : Fragment() {

    private val viewModel: MentionsListViewModel by viewModels()

    private var _binding: FragmentMentionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupOnClickListeners()

        viewModel.bindView(binding.mentionsListView, viewLifecycleOwner)
        binding.mentionsListView.setMentionSelectedListener {
            // TODO add navigation
            showToast("Selected mention ${it.id}")
        }
    }

    private fun setupOnClickListeners() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                finish()
            }
        }
    }
}
