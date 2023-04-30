package jr.brian.issaaiapp.view.ui.pages

import android.app.Activity.RESULT_OK
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.local.MyDataStore
import jr.brian.issaaiapp.model.remote.ApiService
import jr.brian.issaaiapp.util.*
import jr.brian.issaaiapp.view.ui.components.*
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import jr.brian.issaaiapp.BuildConfig
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Conversation
import jr.brian.issaaiapp.view.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ChatPage(
    dao: ChatsDao,
    dataStore: MyDataStore,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
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
    storedCurrentConvo: Set<String>,
    launchConvoContextPage: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val focusManager = LocalFocusManager.current

    val promptText = remember { mutableStateOf("") }
    val apiKeyText = remember { mutableStateOf("") }
    val humanSenderLabelText = remember { mutableStateOf("") }
    val conversationalContextText = remember { mutableStateOf("") }
    val conversationHeaderName = remember { mutableStateOf("") }
    val conversationText = remember { mutableStateOf("") }

    val isEmptyPromptDialogShowing = remember { mutableStateOf(false) }
    val isSettingsDialogShowing = remember { mutableStateOf(false) }
    val isThemeDialogShowing = remember { mutableStateOf(false) }
    val isConversationsDialogShowing = remember { mutableStateOf(false) }
    val isHowToUseShowing = remember { mutableStateOf(false) }
    val isExportDialogShowing = remember { mutableStateOf(false) }
    val isAutoSpeakToggled = remember { mutableStateOf(storedIsAutoSpeakToggled) }
    val isChatGptTyping = remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    val conversations = remember { dao.getConversations().toMutableStateList() }

    val scrollState = rememberScrollState()

    MainViewModel.autoSpeak = storedIsAutoSpeakToggled
    conversationalContextText.value =
        if (storedCurrentConvo.isEmpty()) "" else storedCurrentConvo.last()
    conversationHeaderName.value =
        if (storedCurrentConvo.isEmpty()) "" else storedCurrentConvo.first()
    SenderLabel.HUMAN_SENDER_LABEL = storedSenderLabel

    val chatListState = rememberLazyListState()
    val chats = remember { mutableStateListOf<Chat>() }
    chats.clear()
    chats.addAll(dao.getChatsByConvo(conversationHeaderName.value).toMutableStateList())

    LaunchedEffect(key1 = 1, block = {
        delay(ChatConfig.SCROLL_ANIMATION_DELAY)
        chatListState.animateScrollToItem(chats.size)
    })

    val speechToText = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            return@rememberLauncherForActivityResult
        }
        focusManager.clearFocus()
        val results = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        promptText.value =
            "${promptText.value}${if (promptText.value.isEmpty()) "" else " "}" + results?.get(0)
    }

    val sendOnClick = {
        if (storedApiKey.isEmpty()) {
            isSettingsDialogShowing.value = true
            Toast.makeText(
                context, "API Key is required", Toast.LENGTH_LONG
            ).show()
        } else if (promptText.value.isEmpty() || promptText.value.isBlank()) {
            isEmptyPromptDialogShowing.value = true
        } else if (conversationHeaderName.value.isEmpty()
            || conversationHeaderName.value.isBlank()
        ) {
            Toast.makeText(
                context, "Please enter a conversation name", Toast.LENGTH_LONG
            ).show()
            isConversationsDialogShowing.value = true
        } else {
            if (SenderLabel.HUMAN_SENDER_LABEL.isEmpty()
                || SenderLabel.HUMAN_SENDER_LABEL.isBlank()
                || SenderLabel.HUMAN_SENDER_LABEL.lowercase()
                    .trim() == SenderLabel.CHATGPT_SENDER_LABEL.lowercase()
            ) {
                Toast.makeText(
                    context, "Please provide a different sender label", Toast.LENGTH_LONG
                ).show()
                isSettingsDialogShowing.value = true
            } else {
                val prompt = promptText.value
                promptText.value = ""
                scope.launch {
                    val myChat = Chat(
                        fullTimeStamp = LocalDateTime.now().toString(),
                        text = prompt,
                        senderLabel = SenderLabel.HUMAN_SENDER_LABEL,
                        dateSent = LocalDateTime.now().format(dateFormatter),
                        timeSent = LocalDateTime.now().format(timeFormatter),
                        conversationName = conversationHeaderName.value
                    )
                    chats.add(myChat)
                    dao.insertChat(myChat)
                    chatListState.animateScrollToItem(chats.size)
                    viewModel.getChatGptResponse(
                        context = context,
                        dao = dao,
                        userPrompt = prompt,
                        conversationName = conversationHeaderName.value,
                        system = conversationalContextText,
                        isAITypingLabelShowing = isChatGptTyping
                    )
                    val chatGptChat = Chat(
                        fullTimeStamp = LocalDateTime.now().toString(),
                        text = viewModel.response.value ?: "No response. Please try again.",
                        senderLabel = SenderLabel.CHATGPT_SENDER_LABEL,
                        dateSent = LocalDateTime.now().format(dateFormatter),
                        timeSent = LocalDateTime.now().format(timeFormatter),
                        conversationName = conversationHeaderName.value
                    )
                    chats.add(chatGptChat)
                    dao.insertChat(chatGptChat)
                    chatListState.animateScrollToItem(chats.size)
                }
            }
        }
    }

    HowToUseDialog(
        isShowing = isHowToUseShowing,
        primaryColor = primaryColor
    )

    EmptyPromptDialog(
        isShowing = isEmptyPromptDialogShowing,
        primaryColor = primaryColor
    )

    ExportDialog(
        isShowing = isExportDialogShowing,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isAmoledThemeToggled = isAmoledThemeToggled,
        dao = dao,
        conversations = conversations
    )

    ConversationsDialog(
        isShowing = isConversationsDialogShowing,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isAmoledThemeToggled = isAmoledThemeToggled,
        conversations = conversations,
        conversationText = conversationText,
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        onSaveClick = {
            if (conversationText.value.isNotBlank() && conversationText.value.isNotEmpty()) {
                val conversation =
                    Conversation(conversationText.value.trim(), "test")
                if (!conversations.contains(conversation)) {
                    scope.launch {
                        dataStore.saveCurrentConversationName(conversation.conversationName)
                        dataStore.saveCurrentConversation(conversation)
                    }
                    conversations.add(conversation)
                    dao.insertConversation(conversation)
                    getConvoChats(
                        dao = dao,
                        chats = chats,
                        conversationText = conversationText,
                        conversationHeaderName = conversationHeaderName,
                        isConversationsDialogShowing = isConversationsDialogShowing
                    )
                    scope.launch {
                        scaffoldState.drawerState.close()
                        delay(ChatConfig.SCROLL_ANIMATION_DELAY)
                        chatListState.animateScrollToItem(chats.size)
                    }
                } else {
                    Toast.makeText(context, "Name already exists", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Please enter a conversation name", Toast.LENGTH_LONG)
                    .show()
            }
        },
        onSelectItem = { name, ctx ->
            val conversation = Conversation(name, ctx)
            getConvoChats(
                dao = dao,
                chats = chats,
                conversationText = conversationText,
                conversationHeaderName = conversationHeaderName,
                isConversationsDialogShowing = isConversationsDialogShowing
            )
            scope.launch {
                dataStore.saveCurrentConversationName(name)
                dataStore.saveCurrentConversation(conversation)
                scaffoldState.drawerState.close()
                delay(ChatConfig.SCROLL_ANIMATION_DELAY)
                chatListState.animateScrollToItem(chats.size)
            }
        },
        onDeleteItem = { name, ctx ->
            val conversation = Conversation(name, ctx)
            conversations.remove(conversation)
            dao.removeConversation(conversation)
            dao.removeAllChatsByConvo(name)
            if (conversationHeaderName.value == name) {
                chats.clear()
                scope.launch {
                    dataStore.saveCurrentConversationName(
                        if (conversations.isNotEmpty()) conversations.last().conversationName else ""
                    )
                    dataStore.saveCurrentConversation(
                        if (conversations.isNotEmpty()) conversations.last() else Conversation(
                            "",
                            ""
                        )
                    )
                }
            }
        }
    )

    ThemeDialog(
        isShowing = isThemeDialogShowing,
        primaryColor = primaryColor,
        isThemeOneToggled = isThemeOneToggled.value,
        isThemeTwoToggled = isThemeTwoToggled.value,
        isThemeThreeToggled = isThemeThreeToggled.value,
        isAmoledThemeToggled = isAmoledThemeToggled.value,
        onThemeOneChange = {
            isThemeOneToggled.value = it
            isThemeTwoToggled.value = it.not()
            isThemeThreeToggled.value = it.not()
            isAmoledThemeToggled.value = it.not()
            primaryColor.value = DefaultPrimaryColor
            secondaryColor.value = DefaultSecondaryColor
            scope.launch {
                dataStore.saveThemeChoice(THEME_ONE)
            }
        },
        onThemeTwoChange = {
            isThemeTwoToggled.value = it
            isThemeOneToggled.value = it.not()
            isThemeThreeToggled.value = it.not()
            isAmoledThemeToggled.value = it.not()
            primaryColor.value = ThemeTwoPrimary
            secondaryColor.value = ThemeTwoSecondary
            scope.launch {
                dataStore.saveThemeChoice(THEME_TWO)
            }
        },
        onThemeThreeChange = {
            isThemeThreeToggled.value = it
            isThemeOneToggled.value = it.not()
            isThemeTwoToggled.value = it.not()
            isAmoledThemeToggled.value = it.not()
            primaryColor.value = ThemeThreePrimary
            secondaryColor.value = ThemeThreeSecondary
            scope.launch {
                dataStore.saveThemeChoice(THEME_THREE)
            }
        }
    ) {
        isAmoledThemeToggled.value = it
        isThemeOneToggled.value = it.not()
        isThemeTwoToggled.value = it.not()
        isThemeThreeToggled.value = it.not()
        primaryColor.value = Color.Black
        secondaryColor.value = Color.White
        scope.launch {
            dataStore.saveThemeChoice(AMOLED_THEME)
        }
    }

    SettingsDialog(
        primaryColor = primaryColor,
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
        isAmoledThemeToggled = isAmoledThemeToggled,
        showChatsDeletionWarning = {
            Toast.makeText(
                context, "Long-press to confirm", Toast.LENGTH_LONG
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
                context, "Long-press to confirm", Toast.LENGTH_LONG
            ).show()
        },
        onResetAllChats = {
            chats.clear()
            dao.removeAllChats()
            isSettingsDialogShowing.value = false
            Toast.makeText(context, "All Chats deleted!", Toast.LENGTH_LONG).show()
        },
        isAutoSpeakToggled = storedIsAutoSpeakToggled
    ) {
        isAutoSpeakToggled.value = it
        scope.launch {
            dataStore.saveIsAutoSpeakToggled(isAutoSpeakToggled.value)
        }
    }

    val scaffoldBgColor = if (isAmoledThemeToggled.value) Color.Black
    else MaterialTheme.colors.background

    val drawerContentColor = if (isAmoledThemeToggled.value) Color.White
    else primaryColor.value

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        backgroundColor = scaffoldBgColor,
        drawerBackgroundColor = scaffoldBgColor,
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
                    "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_NAME}"
                            + "\nDeveloped by BrianJr03",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isThemeDialogShowing.value = !isThemeDialogShowing.value
                }) {
                Text(
                    "Theme", color = drawerContentColor, modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isSettingsDialogShowing.value = !isSettingsDialogShowing.value
                }) {
                Text(
                    "Settings", color = drawerContentColor, modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isHowToUseShowing.value = !isHowToUseShowing.value
                }) {
                Text(
                    "How to use", color = drawerContentColor, modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isConversationsDialogShowing.value = !isConversationsDialogShowing.value
                }) {
                Text(
                    "Conversations",
                    color = drawerContentColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExportDialogShowing.value = !isExportDialogShowing.value
                }) {
                Text(
                    "Export Conversation",
                    color = drawerContentColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launchConvoContextPage()
                }) {
                Text(
                    "Conversational Context",
                    color = drawerContentColor,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Divider(color = drawerContentColor)
        }) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scrollable(scrollState, orientation = Orientation.Vertical)
                .padding(it)
                .navigationBarsPadding()
        )
        {
            Spacer(Modifier.height(5.dp))

            ChatHeader(
                conversationName = conversationHeaderName,
                isChatGptTyping = isChatGptTyping,
                isAmoledThemeToggled = isAmoledThemeToggled,
                primaryColor = primaryColor,
                chats = chats,
                scope = scope,
                listState = chatListState,
                modifier = Modifier.padding(5.dp),
                onMenuClick = {
                    scope.launch {
                        with(scaffoldState.drawerState) {
                            focusManager.clearFocus()
                            if (isClosed) open() else close()
                        }
                    }
                }
            ) {
                chats.clear()
                dao.removeAllChatsByConversation(conversationHeaderName.value)
                isSettingsDialogShowing.value = false
                Toast.makeText(
                    context,
                    "Conversation has been reset.",
                    Toast.LENGTH_LONG
                ).show()
            }

            ChatSection(
                modifier = Modifier
                    .weight(.90f)
                    .clickable(
                        interactionSource = interactionSource, indication = null
                    ) {
                        focusManager.clearFocus()
                    },
                dao = dao,
                chats = chats,
                listState = chatListState,
                scaffoldState = scaffoldState,
                viewModel = viewModel,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                isAmoledThemeToggled = isAmoledThemeToggled
            )

            ChatTextFieldRow(
                promptText = promptText.value,
                textFieldOnValueChange = { text -> promptText.value = text },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                isAmoledThemeToggled = isAmoledThemeToggled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    },
                sendIconModifier = Modifier
                    .size(30.dp)
                    .clickable { sendOnClick() },
                micIconModifier = Modifier
                    .size(25.dp)
                    .clickable {
                        speechToText.launch(getSpeechInputIntent(context))
                    }
            )
        }
    }
}