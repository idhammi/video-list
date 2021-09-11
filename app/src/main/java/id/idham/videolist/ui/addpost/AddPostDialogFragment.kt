package id.idham.videolist.ui.addpost

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import id.idham.videolist.R
import id.idham.videolist.data.ItemUrl
import id.idham.videolist.databinding.DialogAddPostBinding
import id.idham.videolist.ui.main.MainViewModel
import id.idham.videolist.utils.gone
import id.idham.videolist.utils.visible
import kotlin.random.Random

class AddPostDialogFragment : DialogFragment(), AddPostInterface {

    companion object {
        const val TAG = "AddPostDialog"
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: DialogAddPostBinding
    private lateinit var adapter: AddPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog?.setCancelable(false)
        }
        val dialogBinding = DialogAddPostBinding.inflate(inflater, container, false)
        binding = dialogBinding
        return dialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding.btnClose.setOnClickListener {
            viewModel.clearUrl()
            dismiss()
        }
        binding.btnAddMoreUrl.setOnClickListener {
            viewModel.addMoreUrl(ItemUrl(Random.nextInt(), ""))
        }
        binding.btnPostAllUrl.setOnClickListener {
            val list = viewModel.listUrl.value
            Log.d("POST", "${list?.size}\n")
            list?.let {
                for (item in list) {
                    Log.d("POST", "$item\n")
                }
            }
        }

        adapter = AddPostAdapter(this)
        binding.rvInsertUrl.adapter = adapter
        binding.rvInsertUrl.setItemViewCacheSize(10)

        viewModel.listUrl.observe(this) { list ->
            list.let {
                setConfigChange(list)
                adapter.submitList(list)
                onUrlFilled(checkUrlValued(list))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = requireActivity().display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = requireActivity().windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }

        val width = displayMetrics.widthPixels
        val sizeInPixel = resources.getDimensionPixelSize(R.dimen._28sdp)

        dialog?.window?.setLayout(width - sizeInPixel, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onItemRemoved(itemUrl: ItemUrl) {
        viewModel.removeUrl(itemUrl)
    }

    override fun onItemUpdated(itemUrl: ItemUrl) {
        viewModel.updateUrl(itemUrl)
    }

    private fun checkUrlValued(list: List<ItemUrl>): Boolean {
        for (item in list) {
            if (item.url.isNotEmpty()) return true
        }
        return false
    }

    private fun onUrlFilled(isFilled: Boolean) {
        if (isFilled) {
            binding.btnPostAllUrl.alpha = 1f
            binding.btnPostAllUrl.isClickable = true
        } else {
            binding.btnPostAllUrl.alpha = .2f
            binding.btnPostAllUrl.isClickable = false
        }
    }

    private fun setConfigChange(list: List<ItemUrl>) {
        if (list.size == 1) {
            if (list.first().url.isNotEmpty()) binding.btnAddMoreUrl.visible()
            else binding.btnAddMoreUrl.gone()
            binding.btnPostAllUrl.text = resources.getString(R.string.action_add_post)
        } else {
            if (list.size < 10) binding.btnAddMoreUrl.visible()
            else binding.btnAddMoreUrl.gone()
            binding.btnPostAllUrl.text = resources.getString(R.string.action_post_all_url)
        }
    }

}