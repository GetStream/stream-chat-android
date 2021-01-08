package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.messages.adapter.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListViewHolderBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

abstract class BaseMessagesComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListViewHolderBinding? = null
    protected val binding get() = _binding!!

    protected val currentUser = randomUser()
    protected lateinit var decorators: List<Decorator>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComponentBrowserMessageListViewHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = createAdapter()
        decorators = MessageListItemDecoratorProvider(
            currentUser = currentUser,
            dateFormatter = DateFormatter.from(view.context),
            directMessage = false
        ).decorators
    }

    protected abstract fun createAdapter(): RecyclerView.Adapter<*>
}
