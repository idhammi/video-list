package id.idham.videolist.ui.addpost

import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.idham.videolist.data.ItemUrl
import id.idham.videolist.databinding.ItemAddUrlBinding
import id.idham.videolist.utils.gone
import id.idham.videolist.utils.visible

class AddPostAdapter(private val postInterface: AddPostInterface) :
    ListAdapter<ItemUrl, AddPostAdapter.ItemViewHolder>(DiffCallback) {

    class ItemViewHolder(
        private var binding: ItemAddUrlBinding,
        private val postInterface: AddPostInterface
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemUrl, itemCount: Int) {
            if (itemCount == 1) binding.btnDelete.gone()
            else binding.btnDelete.visible()

            binding.etInsertUrl.setText(item.url)

            binding.etInsertUrl.doOnTextChanged { text, _, _, _ ->
                if (text!!.isNotEmpty()) {
                    postInterface.onItemUpdated(ItemUrl(item.id, text.toString()))
                    binding.btnPaste.gone()
                } else {
                    postInterface.onItemUpdated(ItemUrl(item.id, ""))
                    binding.btnPaste.visible()
                }
            }

            binding.btnDelete.setOnClickListener {
                binding.etInsertUrl.clearFocus()
                postInterface.onItemRemoved(item)
            }

            binding.btnPaste.setOnClickListener {
                val clipboard =
                    it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard.hasPrimaryClip()) {
                    val clip = clipboard.primaryClip?.getItemAt(0)
                    binding.etInsertUrl.setText(clip?.text)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ItemUrl>() {
        override fun areItemsTheSame(oldItem: ItemUrl, newItem: ItemUrl): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ItemUrl, newItem: ItemUrl): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemAddUrlBinding.inflate(LayoutInflater.from(parent.context)), postInterface
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, itemCount)
    }

}