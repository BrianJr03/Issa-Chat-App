package jr.brian.issaaiapp.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jr.brian.issaaiapp.model.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _response = MutableStateFlow<String?>(null)
    val response = _response.asStateFlow()
    suspend fun getChatGptResponse(
        userPrompt: String,
        system: MutableState<String>,
        isAITypingLabelShowing: MutableState<Boolean>
    ) {
        _response.emit(repository.getChatGptResponse(userPrompt, system, isAITypingLabelShowing))
    }
}