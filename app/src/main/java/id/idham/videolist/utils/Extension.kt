package id.idham.videolist.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
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

fun View.fadeVisibility(visibility: Int, duration: Long = 400) {
    val transition: Transition = Fade()
    transition.duration = duration
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
    this.visibility = visibility
}

fun String.getMimeType(): String {
    val format = this.trim().substringAfterLast(".")
    if (format == "mp4") return MimeTypes.APPLICATION_MP4
    return MimeTypes.VIDEO_UNKNOWN
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}