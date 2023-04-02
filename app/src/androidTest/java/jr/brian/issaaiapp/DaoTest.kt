package jr.brian.issaaiapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import jr.brian.issaaiapp.model.local.AppDatabase
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import com.google.common.truth.Truth

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DaoTest {
    private lateinit var appDB: AppDatabase
    private lateinit var dao: ChatsDao
    private val testChats = mutableListOf<Chat>()

    @Before
    fun init() {
        appDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = appDB.dao()
        for (i in 1..3) {
            testChats.add(
                Chat(
                    fullTimeStamp = "Test stamp $i",
                    text = "Test text $i",
                    senderLabel = "Test $i",
                    dateSent = "Test date $i",
                    timeSent = "Test time $i"
                )
            )
        }
    }

    @Test
    fun testInsertChat() {
        runTest(StandardTestDispatcher()) {
            testChats.forEach { dao.insertChat(it) }
            val storedChats = dao.getChats()
            Truth.assertThat(storedChats).containsExactly(
                testChats[0],
                testChats[1],
                testChats[2]
            )
        }
    }

    @Test
    fun testRemoveChat() {
        runTest(StandardTestDispatcher()) {
            testChats.forEach { dao.insertChat(it) }
            dao.removeChat(testChats[0])
            val storedChats = dao.getChats()
            Truth.assertThat(storedChats).containsExactly(
                testChats[1],
                testChats[2]
            )
        }
    }

    @Test
    fun testRemoveAllChats() {
        runTest(StandardTestDispatcher()) {
            testChats.forEach { dao.insertChat(it) }
            dao.removeAllChats()
            val storedChats = dao.getChats()
            Truth.assertThat(storedChats).isEmpty()
        }
    }

    @After
    fun close() {
        appDB.close()
    }
}