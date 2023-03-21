package jr.brian.issaaiapp.model.repository

import androidx.compose.runtime.MutableState
import jr.brian.issaaiapp.model.remote.ApiService

class RepoImpl : Repository {
   companion object  {
       private val apiService = ApiService
    }
    override suspend fun getChatGptResponse(
        userPrompt: String,
        system: MutableState<String>,
        isAITypingLabelShowing: MutableState<Boolean>
    ): String {
        return apiService.getChatGptResponse(
            userPrompt = userPrompt,
            system = system,
            isAITypingLabelShowing = isAITypingLabelShowing
        )
    }
}