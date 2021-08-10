package io.getstream.chat.ui.sample.feature.pinned

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.ui.pinned.list.viewmodel.PinnedMessageListViewModel
import io.getstream.chat.android.ui.pinned.list.viewmodel.PinnedMessageListViewModelFactory
import io.getstream.chat.android.ui.pinned.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentPinnedMessageListBinding
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections

class PinnedMessageListFragment : Fragment() {

    private val args: PinnedMessageListFragmentArgs by navArgs()

    private val viewModel: PinnedMessageListViewModel by viewModels { PinnedMessageListViewModelFactory(args.cid) }

    private var _binding: FragmentPinnedMessageListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPinnedMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar(binding.toolbar)

        viewModel.bindView(binding.pinnedMessageListView, viewLifecycleOwner)
        binding.pinnedMessageListView.setPinnedMessageSelectedListener { message ->
            requireActivity().findNavController(R.id.hostFragmentContainer)
                .navigateSafely(HomeFragmentDirections.actionOpenChat(message.cid, message.id))
        }
    }
}
