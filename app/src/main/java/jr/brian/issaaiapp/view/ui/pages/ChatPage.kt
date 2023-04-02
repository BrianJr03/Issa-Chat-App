package jr.brian.issaaiapp.view.ui.pages

import android.app.Activity.RESULT_OK
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import jr.brian.issaaiapp.BuildConfig
import jr.brian.issaaiapp.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatPage(dao: ChatsDao, dataStore: MyDataStore, viewModel: MainViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val bringIntoViewRequester = BringIntoViewRequester()
    val focusManager = LocalFocusManager.current

    val storedApiKey = dataStore.getApiKey.collectAsState(initial = "").value ?: ""
    val storedIsAutoSpeakToggled =
        dataStore.getIsAutoSpeakToggled.collectAsState(initial = false).value ?: false
    val storedConvoContext = dataStore.getConvoContext.collectAsState(initial = "").value ?: ""
    val storedSenderLabel = dataStore.getHumanSenderLabel.collectAsState(initial = "").value
        ?: SenderLabel.DEFAULT_HUMAN_LABEL
    val promptText = remember { mutableStateOf("") }
    val apiKeyText = remember { mutableStateOf("") }
    val humanSenderLabelText = remember { mutableStateOf("") }
    val conversationalContextText = remember { mutableStateOf("") }

    val isEmptyPromptDialogShowing = remember { mutableStateOf(false) }
    val isSettingsDialogShowing = remember { mutableStateOf(false) }
    val isHowToUseShowing = remember { mutableStateOf(false) }
    val isAutoSpeakToggled = remember { mutableStateOf(storedIsAutoSpeakToggled) }
    val isChatGptTyping = remember { mutableStateOf(false) }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("MM.dd.yy")
    val timeSent: String = LocalDateTime.now().format(dateTimeFormatter)
    val dateSent: String = LocalDateTime.now().format(dateFormatter)

    val chatListState = rememberLazyListState()
    val chats = remember { dao.getChats().toMutableStateList() }

    val interactionSource = remember { MutableInteractionSource() }

    MainViewModel.autoSpeak = storedIsAutoSpeakToggled
    conversationalContextText.value = storedConvoContext

    LaunchedEffect(key1 = 1, block = {
        chatListState.scrollToItem(chats.size)
    })

    val speechToText = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        focusManager.clearFocus()
        val results = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        promptText.value = "${promptText.value} ${results?.get(0)}"
    }

    val sendOnClick = {
        if (storedApiKey.isEmpty()) {
            isSettingsDialogShowing.value = true
            Toast.makeText(
                context,
                "API Key is required",
                Toast.LENGTH_LONG
            ).show()
        } else if (promptText.value.isEmpty() || promptText.value.isBlank()) {
            isEmptyPromptDialogShowing.value = true
        } else {
            val prompt = promptText.value
            promptText.value = ""
            scope.launch {
                if (humanSenderLabelText.value.lowercase().trim() ==
                    SenderLabel.CHATGPT_SENDER_LABEL.lowercase() ||
                    humanSenderLabelText.value.isBlank() ||
                    humanSenderLabelText.value.isEmpty()
                ) {
                    val defaultLabel = SenderLabel.DEFAULT_HUMAN_LABEL
                    SenderLabel.HUMAN_SENDER_LABEL = defaultLabel
                    humanSenderLabelText.value = defaultLabel
                    dataStore.saveHumanSenderLabel(defaultLabel)
                }
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
                    context = context,
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
    EmptyPromptDialog(isShowing = isEmptyPromptDialogShowing)

    SettingsDialog(
        apiKey = apiKeyText.value.ifEmpty { storedApiKey },
        apiKeyOnValueChange = { text ->
            apiKeyText.value = text
            scope.launch { dataStore.saveApiKey(apiKeyText.value) }
            ApiService.ApiKey.userApiKey = apiKeyText.value
        },
        humanSenderLabel = humanSenderLabelText.value.ifEmpty { storedSenderLabel },
        senderLabelOnValueChange = { text ->
            humanSenderLabelText.value = text
            scope.launch { dataStore.saveHumanSenderLabel(humanSenderLabelText.value) }
            SenderLabel.HUMAN_SENDER_LABEL = humanSenderLabelText.value
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
                apiKeyText.value = ""
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
            isSettingsDialogShowing.value = false
            Toast.makeText(context, "Chats deleted!", Toast.LENGTH_LONG).show()
        },
        isAutoSpeakToggled = storedIsAutoSpeakToggled,
        onAutoSpeakCheckedChange = {
            isAutoSpeakToggled.value = it
            scope.launch {
                dataStore.saveIsAutoSpeakToggles(isAutoSpeakToggled.value)
            }
        }
    )

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = "App icon",
                    Modifier
                        .padding(start = 10.dp)
                        .size(35.dp)
                        .fillMaxWidth()
                )

                Text(
                    "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_NAME}" +
                            "\nDeveloped by BrianJr03",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = MaterialTheme.colors.primary)

            Text(
                "Conversational Context",
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(16.dp)
            )

            OutlinedTextField(
                value = conversationalContextText.value,
                onValueChange = { text ->
                    conversationalContextText.value = text
                    scope.launch {
                        dataStore.saveConvoContext(text)
                    }
                },
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
                    "How to use",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = MaterialTheme.colors.secondary)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isSettingsDialogShowing.value = !isSettingsDialogShowing.value
                }) {
                Text(
                    "Settings",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(16.dp)
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
                        with(scaffoldState.drawerState) {
                            focusManager.clearFocus()
                            if (isClosed) open() else close()
                        }
                    }
                }
            )

            ChatSection(
                modifier = Modifier
                    .weight(.90f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                dao = dao,
                chats = chats,
                listState = chatListState,
                scaffoldState = scaffoldState,
                viewModel = viewModel
            )

            ChatTextFieldRow(
                promptText = promptText.value,
                sendOnClick = { sendOnClick() },
                textFieldOnValueChange = { text -> promptText.value = text },
                modifier = Modifier
                    .padding(start = 5.dp)
                    .bringIntoViewRequester(bringIntoViewRequester),
                textFieldModifier = Modifier
                    .weight(.7f)
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
                    .combinedClickable(onClick = {
                        sendOnClick()
                    }, onLongClick = {
                        speechToText.launch(getSpeechInputIntent(context))
                    }),
                micIconModifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .weight(.2f)
                    .size(30.dp)
                    .padding(end = 10.dp)
                    .clickable {
                        speechToText.launch(getSpeechInputIntent(context))
                    }
            )
        }
    }
}