package jr.brian.issaaiapp.model.remote

import androidx.compose.runtime.MutableState
import jr.brian.issaaiapp.util.ChatConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

interface ApiService {
    object ApiKey { var userApiKey = "" }

    companion object {
        suspend fun getChatGptResponse(
            userPrompt: String,
            system: MutableState<String>,
            isAITypingLabelShowing: MutableState<Boolean>
        ) : String {
            var aiResponse: String
            isAITypingLabelShowing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val key = ApiKey.userApiKey
                    val request = ChatBot.ChatCompletionRequest(
                        model = ChatConfig.GPT_3_5_TURBO,
                        systemContent = system.value
                    )
                    val bot = CachedChatBot(key, request)
                    aiResponse = bot.generateResponse(userPrompt)
                    isAITypingLabelShowing.value = false
                }
            } catch (e: SocketTimeoutException) {
                aiResponse = "Connection timed out. Please try again."
                isAITypingLabelShowing.value = false
            } catch (e: java.lang.IllegalArgumentException) {
                aiResponse = "Error: ${e.message}"
                isAITypingLabelShowing.value = false
            }
            return aiResponse
        }
    }
}