package jr.brian.issaaiapp.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import dagger.hilt.android.AndroidEntryPoint
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.view.ui.pages.ChatPage
import jr.brian.issaaiapp.view.ui.theme.IssaAIAppTheme
import jr.brian.issaaiapp.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var dao: ChatsDao? = null
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content))
        { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
        setContent {
            IssaAIAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val dataStore = MyDataStore(this)
                    ApiService.ApiKey.userApiKey =
                        dataStore.getApiKey.collectAsState(initial = "").value ?: ""
                    dao?.let { ChatPage(dao = it, dataStore = dataStore) }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewModel.textToSpeech?.shutdown()
    }
}