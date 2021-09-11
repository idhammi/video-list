package id.idham.videolist.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.idham.videolist.data.ItemUrl
import kotlin.random.Random

class MainViewModel : ViewModel() {

    private val _listUrl = MutableLiveData<List<ItemUrl>>()
    val listUrl: LiveData<List<ItemUrl>>
        get() = _listUrl

    init {
        addMoreUrl(ItemUrl(Random.nextInt(), ""))
    }

    fun addMoreUrl(itemUrl: ItemUrl) {
        val currentList = _listUrl.value
        if (currentList == null) {
            _listUrl.postValue(listOf(itemUrl))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(itemUrl)
            _listUrl.postValue(updatedList)
        }
    }

    fun updateUrl(item: ItemUrl) {
        val currentList = _listUrl.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.find { it.id == item.id }?.url = item.url
            _listUrl.postValue(updatedList)
        }
    }

    fun removeUrl(item: ItemUrl) {
        val currentList = _listUrl.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(item)
            _listUrl.postValue(updatedList)
        }
    }

    fun clearUrl() {
        val currentList = _listUrl.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.clear()
            updatedList.add(ItemUrl(Random.nextInt(), ""))
            _listUrl.postValue(updatedList)
        }
    }

}