package jr.brian.issaaiapp.view.ui.pages

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.util.*
import jr.brian.issaaiapp.view.ui.components.*
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage(dao: ChatsDao, dataStore: MyDataStore, viewModel: MainViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    val storedApiKey = dataStore.getApiKey.collectAsState(initial = "").value ?: ""
    val storedIsAutoConvoContextToggled =
        dataStore.getIsAutoConvoContextToggled.collectAsState(initial = false).value ?: false
    val storedIsAutoGreetToggled =
        dataStore.getIsAutoGreetToggled.collectAsState(initial = false).value ?: false
    var promptText by remember { mutableStateOf("") }
    var apiKeyText by remember { mutableStateOf("") }
    val conversationalContextText = remember {
        mutableStateOf(ChatConfig.conversationalContext.random())
    }

    val isErrorDialogShowing = remember { mutableStateOf(false) }
    val isSettingsDialogShowing = remember { mutableStateOf(false) }
    val isHowToUseShowing = remember { mutableStateOf(false) }
    val isAutoConvoContextToggled = remember { mutableStateOf(storedIsAutoConvoContextToggled) }
    val isAutoGreetToggled = remember { mutableStateOf(storedIsAutoGreetToggled) }
    val isChatGptTyping = remember { mutableStateOf(false) }
    val isConversationalContextShowing = remember { mutableStateOf(false) }
    val hasBeenGreeted = remember { mutableStateOf(false) }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")
    val timeSent: String = LocalDateTime.now().format(dateTimeFormatter)
    val dateSent: String = LocalDateTime.now().format(dateFormatter)

    val chatListState = rememberLazyListState()
    val chats = remember { dao.getChats().toMutableStateList() }

    if (storedIsAutoGreetToggled) {
        autoGreet(
            chats = chats,
            hasBeenGreeted = hasBeenGreeted,
            dateSent = dateSent,
            timeSent = timeSent,
            dao = dao
        )
    }

    LaunchedEffect(key1 = 1, block = {
        chatListState.scrollToItem(chats.size)
    })

    if (storedIsAutoConvoContextToggled) {
        conversationalContextText.value = ""
    }

    val sendOnClick = {
        focusManager.clearFocus()
        if (storedApiKey.isEmpty()) {
            isSettingsDialogShowing.value = true
            Toast.makeText(
                context,
                "API Key is required",
                Toast.LENGTH_LONG
            ).show()
        } else if (promptText.isEmpty()) {
            isErrorDialogShowing.value = true
        } else {
            val prompt = promptText
            promptText = ""
            scope.launch {
                val myChat = Chat(
                    fullTimeStamp = LocalDateTime.now().toString(),
                    text = prompt,
                    senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                    dateSent = dateSent,
                    timeSent = timeSent
                )
                chats.add(myChat)
                dao.insertChat(myChat)
                chatListState.animateScrollToItem(chats.size)
                viewModel.getChatGptResponse(
                    userPrompt = prompt,
                    system = conversationalContextText,
                    isAITypingLabelShowing = isChatGptTyping
                )
                val chatGptChat = Chat(
                    fullTimeStamp = LocalDateTime.now().toString(),
                    text = viewModel.response.value ?: "No response. Please try again.",
                    senderLabel = SenderLabel.CHATGPT_SENDER_LABEL,
                    dateSent = dateSent,
                    timeSent = timeSent
                )
                chats.add(chatGptChat)
                dao.insertChat(chatGptChat)
                chatListState.animateScrollToItem(chats.size)
            }
        }
    }

    HowToUseDialog(isShowing = isHowToUseShowing)
    EmptyTextFieldDialog(isShowing = isErrorDialogShowing)

    SettingsDialog(
        apiKey = apiKeyText.ifEmpty { storedApiKey },
        textFieldOnValueChange = { text ->
            apiKeyText = text
            scope.launch { dataStore.saveApiKey(apiKeyText) }
            ApiService.ApiKey.userApiKey = apiKeyText
        },
        isShowing = isSettingsDialogShowing,
        showChatsDeletionWarning = {
            Toast.makeText(
                context,
                "Long-press to delete all chats",
                Toast.LENGTH_LONG
            ).show()
        },
        onClearApiKey = {
            scope.launch {
                dataStore.saveApiKey("")
                apiKeyText = ""
                ApiService.ApiKey.userApiKey = ""
            }
        },
        showClearApiKeyWarning = {
            Toast.makeText(
                context,
                "Long-press to clear your API Key",
                Toast.LENGTH_LONG
            ).show()
        },
        onDeleteAllChats = {
            chats.clear()
            dao.removeAllChats()
            Toast.makeText(context, "Chats deleted!", Toast.LENGTH_LONG).show()
        },
        modifier = Modifier,
        textFieldModifier = Modifier,
        isAutoConvoContextToggled = storedIsAutoConvoContextToggled,
        isAutoGreetToggled = storedIsAutoGreetToggled,
        onAutoConvoCheckedChange = {
            isAutoConvoContextToggled.value = it
            scope.launch { dataStore.saveIsAutoConvoContextToggled(isAutoConvoContextToggled.value) }
        },
        onAutoGreetCheckedChange = {
            isAutoGreetToggled.value = it
            scope.launch { dataStore.saveIsAutoGreetToggles(isAutoGreetToggled.value) }
        }
    )

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Text(
                "Issa AI App v${jr.brian.issaaiapp.BuildConfig.VERSION_NAME}" +
                        "\nDeveloped by BrianJr03",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )

            Divider(color = MaterialTheme.colors.primary)

            Text("Conversational Context", modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = conversationalContextText.value,
                onValueChange = { text -> conversationalContextText.value = text },
                label = {
                    Text(
                        text = "Enter Conversational Context",
                        style = TextStyle(
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = MaterialTheme.colors.secondary,
                    unfocusedIndicatorColor = MaterialTheme.colors.primary
                ),
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
            )

            Divider(color = MaterialTheme.colors.secondary)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isHowToUseShowing.value = !isHowToUseShowing.value
                }) {
                Text(
                    "How to use", modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = MaterialTheme.colors.secondary)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isSettingsDialogShowing.value = !isSettingsDialogShowing.value
                }) {
                Text(
                    "Settings", modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = MaterialTheme.colors.primary)
        }
    ) {

        Column(
            modifier = Modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(15.dp))

            ChatHeader(
                modifier = Modifier,
                isChatGptTyping = isChatGptTyping,
                onMenuClick = {
                    scope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            )

            ChatSection(
                modifier = Modifier.weight(.90f),
                chats = chats,
                listState = chatListState,
                viewModel = viewModel
            )

            ChatTextFieldRows(
                promptText = promptText,
                sendOnClick = { sendOnClick() },
                textFieldOnValueChange = { text -> promptText = text },
                isConvoContextFieldShowing = isConversationalContextShowing,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .bringIntoViewRequester(bringIntoViewRequester),
                textFieldModifier = Modifier
                    .weight(.8f)
                    .padding(start = 15.dp)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                sendIconModifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .weight(.2f)
                    .size(30.dp)
                    .clickable { sendOnClick() }
            )
        }
    }
}