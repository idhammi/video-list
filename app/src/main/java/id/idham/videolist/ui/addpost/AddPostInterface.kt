package id.idham.videolist.ui.addpost

import id.idham.videolist.data.ItemUrl

interface AddPostInterface {
    fun onItemRemoved(itemUrl: ItemUrl)
    fun onItemUpdated(itemUrl: ItemUrl)
}