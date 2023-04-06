package jr.brian.issaaiapp.model.local

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery


@Dao
interface ChatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: Chat)

    @Query("SELECT * FROM chats")
    fun getChats(): List<Chat>

    @Query("SELECT * FROM chats ORDER BY fullTimeStamp DESC LIMIT 6")
    fun getLastTwoChats(): List<Chat>

    @Delete
    fun removeChat(chat: Chat)

    @Query("DELETE FROM chats")
    fun removeAllChats()

    @RawQuery
    fun getChatsRawQuery(query: SupportSQLiteQuery): List<Chat>

    fun getChatsByConvo(conversationName: String): List<Chat> {
        val query = SimpleSQLiteQuery(
            "SELECT * FROM chats WHERE conversation LIKE ?;",
            arrayOf(conversationName)
        )
        return getChatsRawQuery(query)
    }
}