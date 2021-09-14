package id.idham.videolist.ui.main

import androidx.lifecycle.*
import id.idham.videolist.data.ItemUrl
import id.idham.videolist.data.ItemUrlDao
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(private val itemUrlDao: ItemUrlDao) : ViewModel() {

    private val _listUrl = MutableLiveData<List<ItemUrl>>()
    val listUrl: LiveData<List<ItemUrl>>
        get() = _listUrl

    val allItems = itemUrlDao.getItems().asLiveData()

    var isItemsAvailable = MutableLiveData(false)

    init {
        addMoreUrl(ItemUrl(Random.nextInt(), ""))
    }

    fun postUrl() {
        val list = _listUrl.value
        list?.let { newList ->
            val addedList = arrayListOf<ItemUrl>()
            for (item in newList) {
                if (item.url.isNotEmpty()) addedList.add(ItemUrl(url = item.url))
            }
            viewModelScope.launch {
                itemUrlDao.insert(addedList)
            }
        }
    }

    fun deletePostedItem(item: ItemUrl) {
        viewModelScope.launch {
            itemUrlDao.delete(item)
        }
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

class MainViewModelFactory(private val itemUrlDao: ItemUrlDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(itemUrlDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}