package com.softvision.unittestingstudygroup.exercise5

import android.content.Context
import androidx.room.*

@Entity(tableName = "albums")
data class DatabaseAlbum(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
)

fun List<Album>.asDatabaseModel(): List<DatabaseAlbum> {
    return map {
        DatabaseAlbum(it.userId, it.id, it.title)
    }
}

fun List<DatabaseAlbum>.asDomainModel(): List<Album> {
    return map {
        Album(it.userId, it.id, it.title)
    }
}

@Dao
interface AlbumDao {
    @Query("SELECT * from albums order by title asc")
    suspend fun getAlbumsSorted(): List<DatabaseAlbum>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<DatabaseAlbum>)

    @Query("SELECT * from albums where user_id = :userId")
    suspend fun getAlbumByUserId(userId: Int): List<DatabaseAlbum>
}

@Database(exportSchema = false, version = 1, entities = [DatabaseAlbum::class])
abstract class AlbumDatabase : RoomDatabase() {
    companion object {
        private const val DB_FILE = "albums_db"

        fun getInstance(context: Context): AlbumDatabase = Room.databaseBuilder(
            context,
            AlbumDatabase::class.java,
            DB_FILE
        ).fallbackToDestructiveMigration().build()
    }

    abstract fun getAlbumDao(): AlbumDao
}