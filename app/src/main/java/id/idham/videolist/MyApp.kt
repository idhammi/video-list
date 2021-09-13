package id.idham.videolist

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import id.idham.videolist.data.ItemUrlRoomDatabase

class MyApp : Application() {

    val database: ItemUrlRoomDatabase by lazy { ItemUrlRoomDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

}