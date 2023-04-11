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

    @Query("SELECT * FROM chats WHERE conversationName " +
            "LIKE :convoName ORDER BY fullTimeStamp DESC LIMIT 6")
    fun getLastSixChats(convoName: String): List<Chat>

    @Delete
    fun removeChat(chat: Chat)

    @Query("DELETE FROM chats")
    fun removeAllChats()

    @RawQuery
    fun getChatsRawQuery(query: SupportSQLiteQuery): List<Chat>

    fun getChatsByConvo(conversationName: String): List<Chat> {
        val query = SimpleSQLiteQuery(
            "SELECT * FROM chats WHERE conversationName LIKE ?;",
            arrayOf(conversationName)
        )
        return getChatsRawQuery(query)
    }

    @Query("DELETE FROM chats WHERE conversationName LIKE :convoName")
    fun removeAllChatsByConvo(convoName: String)
    // End Chat Section

    // Conversation Section
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations")
    fun getConversations(): List<Conversation>

    @Delete
    fun removeConversation(conversation: Conversation)
    // End Conversation Section
}