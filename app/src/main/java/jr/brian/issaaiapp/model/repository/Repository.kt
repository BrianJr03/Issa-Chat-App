package jr.brian.issaaiapp.model.repository

import androidx.compose.runtime.MutableState

interface Repository {
    suspend fun getChatGptResponse(
        userPrompt: String,
        system: MutableState<String>,
        isAITypingLabelShowing: MutableState<Boolean>
    ): String
}