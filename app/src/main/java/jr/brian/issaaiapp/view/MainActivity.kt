package jr.brian.issaaiapp.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import dagger.hilt.android.AndroidEntryPoint
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.view.ui.pages.ChatPage
import jr.brian.issaaiapp.view.ui.theme.*
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

        val dataStore = MyDataStore(this)

        setContent {
            IssaAIAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val primaryColor = remember { mutableStateOf(DefaultPrimaryColor) }
                    val secondaryColor = remember { mutableStateOf(DefaultSecondaryColor) }

                    val isThemeOneToggled = remember { mutableStateOf(false) }
                    val isThemeTwoToggled = remember { mutableStateOf(false) }
                    val isThemeThreeToggled = remember { mutableStateOf(false) }

                    val storedApiKey = dataStore.getApiKey.collectAsState(initial = "").value ?: ""
                    ApiService.ApiKey.userApiKey = storedApiKey

                    val storedThemeChoice =
                        dataStore.getThemeChoice.collectAsState(initial = THEME_ONE).value
                            ?: THEME_ONE

                    val storedIsAutoSpeakToggled =
                        dataStore.getIsAutoSpeakToggled.collectAsState(initial = false).value
                            ?: false

                    val storedConvoContext =
                        dataStore.getConvoContext.collectAsState(initial = "").value ?: ""

                    val storedSenderLabel =
                        dataStore.getHumanSenderLabel.collectAsState(initial = "").value
                            ?: SenderLabel.DEFAULT_HUMAN_LABEL

                    val storedCurrentConversationName =
                        dataStore.getCurrentConversationName.collectAsState(initial = "").value
                            ?: "Conversation 1"

                    initTheme(
                        storedThemeChoice = storedThemeChoice,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isThemeOneToggled = isThemeOneToggled,
                        isThemeTwoToggled = isThemeTwoToggled,
                        isThemeThreeToggled = isThemeThreeToggled
                    )

                    dao?.let {
                        ChatPage(
                            dao = it,
                            dataStore = dataStore,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            isThemeOneToggled = isThemeOneToggled,
                            isThemeTwoToggled = isThemeTwoToggled,
                            isThemeThreeToggled = isThemeThreeToggled,
                            storedApiKey = storedApiKey,
                            storedIsAutoSpeakToggled = storedIsAutoSpeakToggled,
                            storedConvoContext = storedConvoContext,
                            storedSenderLabel = storedSenderLabel,
                            storedCurrentConversationName = storedCurrentConversationName
                        )
                    }
                }
            }
        }
    }

    private fun initTheme(
        storedThemeChoice: String,
        primaryColor: MutableState<Color>,
        secondaryColor: MutableState<Color>,
        isThemeOneToggled: MutableState<Boolean>,
        isThemeTwoToggled: MutableState<Boolean>,
        isThemeThreeToggled: MutableState<Boolean>,
    ) {
        when (storedThemeChoice) {
            THEME_ONE -> {
                primaryColor.value = DefaultPrimaryColor
                secondaryColor.value = DefaultSecondaryColor
                isThemeOneToggled.value = true
                isThemeTwoToggled.value = false
                isThemeThreeToggled.value = false
            }
            THEME_TWO -> {
                primaryColor.value = ThemeTwoPrimary
                secondaryColor.value = ThemeTwoSecondary
                isThemeTwoToggled.value = true
                isThemeOneToggled.value = false
                isThemeThreeToggled.value = false
            }
            THEME_THREE -> {
                primaryColor.value = ThemeThreePrimary
                secondaryColor.value = ThemeThreeSecondary
                isThemeThreeToggled.value = true
                isThemeOneToggled.value = false
                isThemeTwoToggled.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewModel.textToSpeech?.shutdown()
    }
}