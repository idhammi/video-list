package id.idham.videolist.ui.custom

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import id.idham.videolist.R
import id.idham.videolist.data.ItemUrl
import id.idham.videolist.utils.getMimeType
import id.idham.videolist.utils.gone
import id.idham.videolist.utils.visible

interface ItemListener {
    fun onItemLoadFailed(itemUrl: ItemUrl)
}

class VideoRecyclerAdapter(private val listener: ItemListener) :
    ListAdapter<ItemUrl, VideoRecyclerAdapter.VideoViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<ItemUrl>() {
        override fun areItemsTheSame(oldItem: ItemUrl, newItem: ItemUrl): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ItemUrl, newItem: ItemUrl): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, listener)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val parent: View = itemView

        val videoContainer: FrameLayout = itemView.findViewById(R.id.video_container)
        val imageView: ImageView = itemView.findViewById(R.id.video_thumbnail)
        val lytProgress: ConstraintLayout = itemView.findViewById(R.id.lyt_progress)
        var videoPreview: MediaItem? = null
        private val tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        private var isImageItem = false

        fun bind(data: ItemUrl, listener: ItemListener) {
            // Initialize for ExoplayerRecyclerView
            parent.tag = this

            if (data.url.getMimeType() == MimeTypes.APPLICATION_MP4) {
                videoPreview = MediaItem.Builder()
                    .setUri(data.url)
                    .setMimeType(MimeTypes.APPLICATION_MP4)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(data.url).build())
                    .build()
                tvLoading.setText(R.string.downloading_video)
                isImageItem = false
            } else {
                videoPreview = null
                tvLoading.setText(R.string.downloading_image)
                lytProgress.visible()
                isImageItem = true
            }

            // load image or video thumbnail
            Glide.with(itemView)
                .load(data.url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        listener.onItemLoadFailed(data)
                        if (isImageItem) lytProgress.gone()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (isImageItem) lytProgress.gone()
                        return false
                    }
                })
                .into(imageView)
        }

        companion object {
            fun from(parent: ViewGroup): VideoViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return VideoViewHolder(inflater.inflate(R.layout.item_feed, parent, false))
            }
        }
    }

}