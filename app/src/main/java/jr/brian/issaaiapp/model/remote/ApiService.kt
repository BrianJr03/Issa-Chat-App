package jr.brian.issaaiapp.model.remote

import androidx.compose.runtime.MutableState
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.util.ChatConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface ApiService {
    object ApiKey {
        var userApiKey = ""
    }

    companion object {
        suspend fun getChatGptResponse(
            dao: ChatsDao,
            userPrompt: String,
            system: MutableState<String>,
            isAITypingLabelShowing: MutableState<Boolean>
        ): String {
            var aiResponse: String
            isAITypingLabelShowing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val key = ApiKey.userApiKey
                    val request = ChatBot.ChatCompletionRequest(
                        model = ChatConfig.GPT_3_5_TURBO,
                        systemContent = system.value
                    )
                    val bot = CachedChatBot(
                        key,
                        request,
                        dao.getLastSixChats()
                    )
                    aiResponse = bot.generateResponse(userPrompt)
                    isAITypingLabelShowing.value = false
                }
            } catch (e: SocketTimeoutException) {
                aiResponse = "Connection timed out. Please try again."
                isAITypingLabelShowing.value = false
            } catch (e: java.lang.IllegalArgumentException) {
                aiResponse = "ERROR: ${e.message}"
                isAITypingLabelShowing.value = false
            } catch (e: UnknownHostException) {
                aiResponse = "ERROR: ${e.message}.\n\n" +
                        "This could indicate no/very poor internet connection. " +
                        "Please check your connection and try again."
                isAITypingLabelShowing.value = false
            }
            return aiResponse
        }
    }
}