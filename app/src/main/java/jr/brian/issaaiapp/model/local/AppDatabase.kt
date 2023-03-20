package jr.brian.issaaiapp.model.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Chat::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ChatsDao
}