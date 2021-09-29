package id.idham.videolist.ui.custom

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import id.idham.videolist.utils.*

class VideoRecyclerView : RecyclerView, DownloadTracker.Listener {

    companion object {
        val TAG = VideoRecyclerView::class.simpleName
    }

    // ui
    private var thumbnail: ImageView? = null
    private var progressBar: ConstraintLayout? = null
    private var viewHolderParent: View? = null
    private var frameLayout: FrameLayout? = null
    private lateinit var videoSurfaceView: PlayerView
    private var videoPlayer: SimpleExoPlayer? = null

    private var listHolder = arrayListOf<VideoRecyclerAdapter.VideoViewHolder>()

    // vars
    private var isVideoViewAdded = false
    private var isListScrolled = false
    private var isListOnTop = true
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        val display =
            (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

        videoSurfaceView = PlayerView(context)
        videoSurfaceView.videoSurfaceView
        videoSurfaceView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        // 2. Create the player
        videoPlayer = SimpleExoPlayer.Builder(context).build()
        // Bind the player to the view.
        videoSurfaceView.useController = false
        videoSurfaceView.player = videoPlayer
        videoPlayer?.volume = 1f
        videoPlayer?.repeatMode = Player.REPEAT_MODE_ONE

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (thumbnail != null) { // show the old thumbnail
                        thumbnail?.visible()
                    }

                    isListScrolled = true
                    isListOnTop = !recyclerView.canScrollVertically(-1)

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }
        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                initVideoData(view)
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })
        videoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        progressBar?.gone()
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {
                    }
                }
            }
        })

        DownloadUtil.getDownloadTracker(context).addListener(this)
    }

    fun initVideoData(view: View) {
        // video is already playing so return
        if (viewHolderParent != null && viewHolderParent == view) {
            return
        }

        if (!::videoSurfaceView.isInitialized) {
            return
        }

        val holder = view.tag as VideoRecyclerAdapter.VideoViewHolder? ?: return

        // item is not a video
        if (holder.videoPreview == null) {
            return
        }

        listHolder.add(holder)

        thumbnail = holder.imageView
        progressBar = holder.lytProgress
        viewHolderParent = holder.itemView
        frameLayout = holder.videoContainer
        videoSurfaceView.player = videoPlayer

        downloadVideo(holder.videoPreview!!, holder)
    }

    private fun downloadVideo(media: MediaItem, holder: VideoRecyclerAdapter.VideoViewHolder) {
        if (!DownloadUtil.getDownloadTracker(context).isDownloaded(media)) {
            holder.lytProgress.visible()
            val item = media.buildUpon().build()
            if (!DownloadUtil.getDownloadTracker(context)
                    .hasDownload(item.playbackProperties?.uri)
            ) {
                DownloadUtil.getDownloadTracker(context).toggleDownloadDialogHelper(item)
            }
        } else {
            if (!isListScrolled) {
                playVideoAtPosition(0)
            }
        }
    }

    private fun playVideoAtPosition(currentPosition: Int) {
        val child = getChildAt(currentPosition) ?: return

        val holder = child.tag as VideoRecyclerAdapter.VideoViewHolder?
        if (holder == null) {
            playPosition = -1
            return
        }

        // item is not a video
        if (holder.videoPreview == null) {
            videoPlayer?.stop()
            return
        }

        thumbnail = holder.imageView
        progressBar = holder.lytProgress
        viewHolderParent = holder.itemView
        frameLayout = holder.videoContainer
        videoSurfaceView.player = videoPlayer

        playVideoFromCache(holder.videoPreview!!)
    }

    private fun playVideoFromCache(mediaItem: MediaItem) {
        if (DownloadUtil.getDownloadTracker(context).isDownloaded(mediaItem)) {
            val videoSource =
                ProgressiveMediaSource.Factory(DownloadUtil.getReadOnlyDataSourceFactory(context))
                    .createMediaSource(mediaItem)
            videoPlayer?.setMediaSource(videoSource)
            videoPlayer?.prepare()
            videoPlayer?.playWhenReady = true
        } else {
            videoPlayer?.stop()
        }
    }

    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int

        if (!isEndOfList) {
            val startPosition =
                (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            var endPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }

            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)

                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = adapter!!.itemCount - 1
        }

        Log.d(TAG, "playVideo: target position: $targetPosition")

        // video is already playing so return
        if (targetPosition == playPosition) {
            return
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition

        if (!::videoSurfaceView.isInitialized) {
            return
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.invisible()
        removeVideoView(videoSurfaceView)

        val currentPosition =
            targetPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

        playVideoAtPosition(currentPosition)
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at =
            playPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

        val child = getChildAt(at) ?: return 0

        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {
        val parent = videoView?.parent as ViewGroup?
        val index = parent?.indexOfChild(videoView)
        if (index != null && index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addVideoView() {
        frameLayout!!.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView.requestFocus()
        videoSurfaceView.fadeVisibility(View.VISIBLE)
        videoSurfaceView.alpha = 1f
        //thumbnail?.visibility = View.GONE
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            progressBar?.invisible()
            videoSurfaceView.invisible()
            thumbnail?.visible()
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }
        resetVideoView()
        viewHolderParent = null
    }

    fun createPlayer() {
        if (videoPlayer == null) {
            init(context)
        }
    }

    override fun onDownloadsChanged(download: Download) {
        when (download.state) {
            Download.STATE_DOWNLOADING -> {
            }
            Download.STATE_QUEUED, Download.STATE_STOPPED -> {
            }
            Download.STATE_COMPLETED -> {
                if (adapter!!.itemCount == 1) {
                    playVideoAtPosition(0)
                }
                for ((index, holder) in listHolder.withIndex()) {
                    holder.videoPreview?.let { mediaItem ->
                        val uri = mediaItem.playbackProperties?.uri
                        if (download.request.uri == uri) {
                            holder.lytProgress.gone()
                            if (isListOnTop && index == listHolder.size - 1) {
                                playVideoAtPosition(0)
                            }
                        }
                    }
                }
            }
            Download.STATE_REMOVING -> {
            }
            Download.STATE_FAILED, Download.STATE_RESTARTING -> {
            }
        }
    }

}