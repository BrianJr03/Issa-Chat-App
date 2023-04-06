package jr.brian.issaaiapp.model.repository

import androidx.compose.runtime.MutableState
import jr.brian.issaaiapp.model.local.ChatsDao

interface Repository {
    suspend fun getChatGptResponse(
        dao: ChatsDao,
        userPrompt: String,
        system: MutableState<String>,
        isAITypingLabelShowing: MutableState<Boolean>
    ): String
}