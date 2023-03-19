package jr.brian.issaaiapp.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import jr.brian.issaaiapp.BuildConfig
import jr.brian.issaaiapp.model.remote.CachedChatBot
import jr.brian.issaaiapp.model.remote.ChatBot
import jr.brian.issaaiapp.util.ChatConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class MainViewModel : ViewModel() {
    private val _response  = MutableStateFlow<String?>(null)
    val response = _response.asStateFlow()

    suspend fun getAIResponse(
        userPrompt: String,
        system: MutableState<String>,
        isAITypingLabelShowing: MutableState<Boolean>
    ) {
        var aiResponse: String
        if (system.value.isEmpty()) {
            system.value = "Be as ${ChatConfig.aiAdjectives.random()} as possible."
        }
        isAITypingLabelShowing.value = true
        try {
            withContext(Dispatchers.IO) {
                val key = BuildConfig.API_KEY
                val request = ChatBot.ChatCompletionRequest(ChatConfig.GPT_3_5_TURBO, system.value)
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

        with(_response) {
            value = null
            emit(aiResponse)
        }
    }
}