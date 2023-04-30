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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.view.ui.pages.ChatPage
import jr.brian.issaaiapp.view.ui.pages.ConvoContextPage
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
                    val isAmoledThemeToggled = remember { mutableStateOf(false) }

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
                            ?: ""

                    val storedCurrentConvo =
                        dataStore.getCurrentConversation.collectAsState(initial = setOf()).value
                            ?: setOf()

                    initTheme(
                        storedThemeChoice = storedThemeChoice,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isThemeOneToggled = isThemeOneToggled,
                        isThemeTwoToggled = isThemeTwoToggled,
                        isThemeThreeToggled = isThemeThreeToggled,
                        isAmoledThemeToggled = isAmoledThemeToggled
                    )

                    dao?.let {
                        AppUI(
                            dao = it,
                            dataStore = dataStore,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            isThemeOneToggled = isThemeOneToggled,
                            isThemeTwoToggled = isThemeTwoToggled,
                            isThemeThreeToggled = isThemeThreeToggled,
                            isAmoledThemeToggled = isAmoledThemeToggled,
                            storedApiKey = storedApiKey,
                            storedIsAutoSpeakToggled = storedIsAutoSpeakToggled,
                            storedConvoContext = storedConvoContext,
                            storedSenderLabel = storedSenderLabel,
                            storedConversationName = storedCurrentConversationName,
                            storedCurrentConversation = storedCurrentConvo
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
        isAmoledThemeToggled: MutableState<Boolean>
    ) {
        when (storedThemeChoice) {
            THEME_ONE -> {
                primaryColor.value = DefaultPrimaryColor
                secondaryColor.value = DefaultSecondaryColor
                isThemeOneToggled.value = true
                isThemeTwoToggled.value = false
                isThemeThreeToggled.value = false
                isAmoledThemeToggled.value = false
            }
            THEME_TWO -> {
                primaryColor.value = ThemeTwoPrimary
                secondaryColor.value = ThemeTwoSecondary
                isThemeTwoToggled.value = true
                isThemeOneToggled.value = false
                isThemeThreeToggled.value = false
                isAmoledThemeToggled.value = false
            }
            THEME_THREE -> {
                primaryColor.value = ThemeThreePrimary
                secondaryColor.value = ThemeThreeSecondary
                isThemeThreeToggled.value = true
                isThemeOneToggled.value = false
                isThemeTwoToggled.value = false
                isAmoledThemeToggled.value = false
            }
            AMOLED_THEME -> {
                primaryColor.value = Color.Black
                secondaryColor.value = Color.White
                isThemeThreeToggled.value = false
                isThemeOneToggled.value = false
                isThemeTwoToggled.value = false
                isAmoledThemeToggled.value = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MainViewModel.textToSpeech?.shutdown()
    }
}

@Composable
fun AppUI(
    dao: ChatsDao,
    dataStore: MyDataStore,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isThemeOneToggled: MutableState<Boolean>,
    isThemeTwoToggled: MutableState<Boolean>,
    isThemeThreeToggled: MutableState<Boolean>,
    isAmoledThemeToggled: MutableState<Boolean>,
    storedApiKey: String,
    storedIsAutoSpeakToggled: Boolean,
    storedConvoContext: String,
    storedSenderLabel: String,
    storedConversationName: String,
    storedCurrentConversation: Set<String>
) {
    val chatPage = "chat_page"
    val convoContextPage = "convo-context-page"
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = chatPage, builder = {
        composable(
            chatPage,
            content = {
                ChatPage(
                    dao = dao,
                    dataStore = dataStore,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    isThemeOneToggled = isThemeOneToggled,
                    isThemeTwoToggled = isThemeTwoToggled,
                    isThemeThreeToggled = isThemeThreeToggled,
                    isAmoledThemeToggled = isAmoledThemeToggled,
                    storedApiKey = storedApiKey,
                    storedIsAutoSpeakToggled = storedIsAutoSpeakToggled,
                    storedConvoContext = storedConvoContext,
                    storedSenderLabel = storedSenderLabel,
                    storedConversationName = storedConversationName,
                    storedCurrentConvo = storedCurrentConversation,
                    launchConvoContextPage = {
                        navController.navigate(convoContextPage) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            })
        composable(
            convoContextPage,
            content = {
                ConvoContextPage(
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    isAmoledThemeToggled = isAmoledThemeToggled,
                    storedConvoContext = storedConvoContext,
                    storedCurrentConvo = storedCurrentConversation,
                    dataStore = dataStore
                )
            }
        )
    })
}