package jr.brian.issaaiapp.model.local

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery


@Dao
interface ChatsDao {
    // Chat Section
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: Chat)

    @Query("SELECT * FROM chats")
    fun getChats(): List<Chat>

    @Query(
        "SELECT * FROM chats WHERE conversationName " +
                "LIKE :convoName ORDER BY fullTimeStamp DESC LIMIT 6"
    )
    fun getLastSixChats(convoName: String): List<Chat>

    @Delete
    fun removeChat(chat: Chat)

    @Query("DELETE FROM chats")
    fun removeAllChats()

    @Query("DELETE FROM chats WHERE conversationName LIKE :convoName")
    fun removeAllChatsByConvo(convoName: String)

    @RawQuery
    fun getChatsRawQuery(query: SupportSQLiteQuery): List<Chat>

    fun getChatsByConvo(conversationName: String): List<Chat> {
        val query = SimpleSQLiteQuery(
            "SELECT * FROM chats WHERE conversationName LIKE ?;",
            arrayOf(conversationName)
        )
        return getChatsRawQuery(query)
    }

    @Query("UPDATE chats SET conversationName=:newConvoName " +
            "WHERE conversationName LIKE :oldConvoName")
    fun updateAllChatsByConvo(oldConvoName: String, newConvoName: String)

    // Conversation Section
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(conversation: Conversation)

    @Query("UPDATE conversations SET conversationName=:newConvoName " +
            "WHERE conversationName LIKE :oldConvoName")
    fun updateConvo(oldConvoName: String, newConvoName: String)

    @Query("SELECT * FROM conversations")
    fun getConversations(): List<Conversation>

    @Delete
    fun removeConversation(conversation: Conversation)
    // End Conversation Section
}