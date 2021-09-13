package id.idham.videolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemUrlDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ItemUrl)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(items: List<ItemUrl>)

    @Update
    suspend fun update(item: ItemUrl)

    @Delete
    suspend fun delete(item: ItemUrl)

    @Query("SELECT * from itemUrl WHERE id = :id")
    fun getItem(id: Int): Flow<ItemUrl>

    @Query("SELECT * from itemUrl ORDER BY id DESC")
    fun getItems(): Flow<List<ItemUrl>>

}