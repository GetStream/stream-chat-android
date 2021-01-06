package io.getstream.chat.android.ui.images.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiImageMenuDialogBinding

internal class ImagesMenuDialogFragment : BottomSheetDialogFragment() {

    private var _binding: StreamUiImageMenuDialogBinding? = null
    private val binding get() = _binding!!

    private val title: String by lazy { requireArguments().getString(ARG_TITLE)!! }
    private val images: List<String> by lazy { requireArguments().getStringArray(ARG_IMAGES)!!.toList() }

    private var imageClickListener: (Int) -> Unit = {}
    private val adapter by lazy { ImagesMenuAdapter(imageClickListener) }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StreamUiImageMenuDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            closeButton.setOnClickListener {
                this@ImagesMenuDialogFragment.dismiss()
            }

            title.text = this@ImagesMenuDialogFragment.title

            imagesMenu.layoutManager = GridLayoutManager(context, SPAN_COUNT, RecyclerView.VERTICAL, false)

            imagesMenu.adapter = adapter
            adapter.submitList(images)

            imagesMenu.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, SPACING, false))
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    fun setImageClickListener(listener: (Int) -> Unit) {
        imageClickListener = listener
    }

    internal companion object {
        private const val SPAN_COUNT = 3
        private const val SPACING: Int = 2

        private const val ARG_TITLE = "title"
        private const val ARG_IMAGES = "images"

        fun newInstance(title: String, imageList: List<String>): ImagesMenuDialogFragment {
            return ImagesMenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArray(ARG_IMAGES, imageList.toTypedArray())
                }
            }
        }
    }
}
