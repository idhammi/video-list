package id.idham.videolist.utils

import android.view.View
import com.google.android.exoplayer2.util.MimeTypes

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun String.getMimeType(): String {
    val format = this.substringAfterLast(".")
    if (format == "mp4") return MimeTypes.APPLICATION_MP4
    return MimeTypes.VIDEO_UNKNOWN
}