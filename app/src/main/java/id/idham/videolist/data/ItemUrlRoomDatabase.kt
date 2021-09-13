package id.idham.videolist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemUrl::class], version = 1, exportSchema = false)
abstract class ItemUrlRoomDatabase : RoomDatabase() {

    abstract fun itemUrlDao(): ItemUrlDao

    companion object {
        @Volatile
        private var INSTANCE: ItemUrlRoomDatabase? = null
        fun getDatabase(context: Context): ItemUrlRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemUrlRoomDatabase::class.java,
                    "item_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }

}