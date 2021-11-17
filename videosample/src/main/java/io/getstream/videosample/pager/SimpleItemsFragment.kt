package io.getstream.videosample.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.videosample.R
import io.getstream.videosample.recycler.ItemsAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [SimpleItemsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SimpleItemsFragment : Fragment() {

    private val adapter = ItemsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_items, container, false)
    }

    private val list = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.itemsRV).apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            setHasFixedSize(false)
            setItemViewCacheSize(20)
        }

        recycler.adapter = adapter

        view.findViewById<Button>(R.id.addItemsBtn).setOnClickListener {
            repeat(5) { list.add("Hi - ${list.size}") }

            val newList = mutableListOf<String>().apply {
                addAll(list)
            }

            adapter.submitList(newList)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SimpleItemsFragment()
    }
}
