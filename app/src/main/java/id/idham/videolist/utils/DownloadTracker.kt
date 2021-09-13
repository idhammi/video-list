package id.idham.videolist.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import id.idham.videolist.R
import id.idham.videolist.service.MyDownloadService
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

private const val TAG = "DownloadTracker"

/** Tracks media that has been downloaded.  */
class DownloadTracker(
    context: Context,
    private val httpDataSourceFactory: HttpDataSource.Factory,
    private val downloadManager: DownloadManager
) {
    /**
     * Listens for changes in the tracked downloads.
     */
    interface Listener {
        /**
         * Called when the tracked downloads changed.
         */
        fun onDownloadsChanged(download: Download)

    }

    private val applicationContext: Context = context.applicationContext
    private val listeners: CopyOnWriteArraySet<Listener> = CopyOnWriteArraySet()
    private val downloadIndex: DownloadIndex = downloadManager.downloadIndex
    private var startDownloadDialogHelper: StartDownloadDialogHelper? = null

    val downloads: HashMap<Uri, Download> = HashMap()

    init {
        downloadManager.addListener(DownloadManagerListener())
        loadDownloads()
    }

    fun addListener(listener: Listener) {
        Assertions.checkNotNull(listener)
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isDownloaded(mediaItem: MediaItem): Boolean {
        val download = downloads[mediaItem.playbackProperties?.uri]
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun hasDownload(uri: Uri?): Boolean = downloads.keys.contains(uri)

    fun getDownloadRequest(uri: Uri?): DownloadRequest? {
        uri ?: return null
        val download = downloads[uri]
        return if (download != null && download.state != Download.STATE_FAILED) download.request else null
    }

    fun toggleDownloadDialogHelper(mediaItem: MediaItem) {
        startDownloadDialogHelper?.release()
        startDownloadDialogHelper =
            StartDownloadDialogHelper(getDownloadHelper(mediaItem), mediaItem)
    }

    fun removeDownload(uri: Uri?) {
        val download = downloads[uri]
        download?.let {
            DownloadService.sendRemoveDownload(
                applicationContext, MyDownloadService::class.java, download.request.id, false
            )
        }
    }

    private fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    downloads[download.request.uri] = download
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to query downloads", e)
        }
    }


    private fun getDownloadHelper(mediaItem: MediaItem): DownloadHelper {
        return when (mediaItem.playbackProperties?.mimeType) {
            MimeTypes.APPLICATION_MPD, MimeTypes.APPLICATION_M3U8, MimeTypes.APPLICATION_SS -> {
                DownloadHelper.forMediaItem(
                    applicationContext,
                    mediaItem,
                    DefaultRenderersFactory(applicationContext),
                    httpDataSourceFactory
                )
            }
            else -> DownloadHelper.forMediaItem(applicationContext, mediaItem)
        }
    }

    private inner class DownloadManagerListener : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            downloads[download.request.uri] = download
            for (listener in listeners) {
                listener.onDownloadsChanged(download)
            }
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            downloads.remove(download.request.uri)
            for (listener in listeners) {
                listener.onDownloadsChanged(download)
            }
        }
    }

    // Can't use applicationContext because it'll result in a crash, instead
    // Use context of the activity calling for the AlertDialog
    private inner class StartDownloadDialogHelper(
        private val downloadHelper: DownloadHelper,
        private val mediaItem: MediaItem,
    ) : DownloadHelper.Callback {

        private var trackSelectionDialog: AlertDialog? = null

        init {
            downloadHelper.prepare(this)
        }

        fun release() {
            downloadHelper.release()
            trackSelectionDialog?.dismiss()
        }

        // DownloadHelper.Callback implementation.
        override fun onPrepared(helper: DownloadHelper) {
            if (helper.periodCount == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.")
                startDownload()
                downloadHelper.release()
                return
            }
        }

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            Toast.makeText(applicationContext, R.string.download_start_error, Toast.LENGTH_LONG)
                .show()
        }

        private fun startDownload() {
            startDownload(buildDownloadRequest())
        }

        // Internal methods.
        private fun startDownload(downloadRequest: DownloadRequest = buildDownloadRequest()) {
            DownloadService.sendAddDownload(
                applicationContext,
                MyDownloadService::class.java,
                downloadRequest,
                true
            )
        }

        private fun buildDownloadRequest(): DownloadRequest {
            return downloadHelper.getDownloadRequest(
                mediaItem.mediaMetadata.title.toString(),
                Util.getUtf8Bytes(mediaItem.playbackProperties?.uri.toString())
            )
        }
    }
}
