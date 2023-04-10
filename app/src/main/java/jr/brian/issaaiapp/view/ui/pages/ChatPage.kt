package jr.brian.issaaiapp.view.ui.pages

import android.app.Activity.RESULT_OK
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import jr.brian.issaaiapp.BuildConfig
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Conversation
import jr.brian.issaaiapp.view.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
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
    storedApiKey: String,
    storedIsAutoSpeakToggled: Boolean,
    storedConvoContext: String,
    storedSenderLabel: String,
    storedConversationName: String
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val bringIntoViewRequester = BringIntoViewRequester()
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
    val isAutoSpeakToggled = remember { mutableStateOf(storedIsAutoSpeakToggled) }
    val isChatGptTyping = remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    val conversations = remember { dao.getConversations().toMutableStateList() }

    MainViewModel.autoSpeak = storedIsAutoSpeakToggled
    conversationalContextText.value = storedConvoContext
    conversationHeaderName.value = storedConversationName
    SenderLabel.HUMAN_SENDER_LABEL = storedSenderLabel

    val chatListState = rememberLazyListState()
    val chats = remember { mutableStateListOf<Chat>() }
    chats.clear()
    chats.addAll(dao.getChatsByConvo(conversationHeaderName.value).toMutableStateList())

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
        } else if (conversationHeaderName.value.isEmpty() || conversationHeaderName.value.isBlank()) {
            Toast.makeText(
                context, "Please enter a conversation name", Toast.LENGTH_LONG
            ).show()
            isConversationsDialogShowing.value = true
        } else {
            if (SenderLabel.HUMAN_SENDER_LABEL.isEmpty() || SenderLabel.HUMAN_SENDER_LABEL.isBlank() || SenderLabel.HUMAN_SENDER_LABEL.lowercase()
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

    HowToUseDialog(isShowing = isHowToUseShowing, primaryColor = primaryColor)

    EmptyPromptDialog(isShowing = isEmptyPromptDialogShowing, primaryColor = primaryColor)

    ConversationsDialog(isShowing = isConversationsDialogShowing,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        conversations = conversations,
        conversationText = conversationText,
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp),
        onSaveClick = {
            if (conversationText.value.isNotBlank() && conversationText.value.isNotEmpty()) {
                val conversation = Conversation(conversationText.value)
                if (!conversations.contains(conversation)) {
                    scope.launch {
                        dataStore.saveCurrentConversationName(conversation.conversationName)
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
                        chatListState.animateScrollToItem(chats.size)
                        scaffoldState.drawerState.close()
                    }
                } else {
                    Toast.makeText(context, "Name already exists", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Please enter a conversation name", Toast.LENGTH_LONG)
                    .show()
            }
        },
        onSelectItem = {
            getConvoChats(
                dao = dao,
                chats = chats,
                conversationText = conversationText,
                conversationHeaderName = conversationHeaderName,
                isConversationsDialogShowing = isConversationsDialogShowing
            )
            scope.launch {
                dataStore.saveCurrentConversationName(it)
                chatListState.animateScrollToItem(chats.size)
                scaffoldState.drawerState.close()
            }
        },
        onDeleteItem = {
            val conversation = Conversation(it)
            conversations.remove(conversation)
            dao.removeConversation(conversation)
            dao.removeAllChatsByConvo(it)
            if (conversationHeaderName.value == it) {
                chats.clear()
                scope.launch {
                    dataStore.saveCurrentConversationName(
                        if (conversations.isNotEmpty()) conversations.last().conversationName else ""
                    )
                }
            }
        })

    ThemeDialog(isShowing = isThemeDialogShowing,
        primaryColor = primaryColor,
        isThemeOneToggled = isThemeOneToggled.value,
        isThemeTwoToggled = isThemeTwoToggled.value,
        isThemeThreeToggled = isThemeThreeToggled.value,
        onThemeOneChange = {
            isThemeOneToggled.value = it
            isThemeTwoToggled.value = it.not()
            isThemeThreeToggled.value = it.not()
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
            primaryColor.value = ThemeThreePrimary
            secondaryColor.value = ThemeThreeSecondary
            scope.launch {
                dataStore.saveThemeChoice(THEME_THREE)
            }
        })

    SettingsDialog(primaryColor = primaryColor,
        secondaryColor = secondaryColor,
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
                context, "Long-press to delete all chats", Toast.LENGTH_LONG
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
                context, "Long-press to clear your API Key", Toast.LENGTH_LONG
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
        })

    Scaffold(modifier = modifier, scaffoldState = scaffoldState, drawerContent = {
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
                "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_NAME}" + "\nDeveloped by BrianJr03",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }

        Divider(color = primaryColor.value)

        Text(
            "Conversational Context",
            color = primaryColor.value,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(value = conversationalContextText.value, onValueChange = { text ->
            conversationalContextText.value = text
            scope.launch {
                dataStore.saveConvoContext(text)
            }
        }, label = {
            Text(
                text = "Enter Conversational Context", style = TextStyle(
                    color = primaryColor.value, fontWeight = FontWeight.Bold
                )
            )
        }, colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = secondaryColor.value,
            unfocusedIndicatorColor = primaryColor.value
        ), modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
        )

        Divider(color = primaryColor.value)

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isConversationsDialogShowing.value = !isConversationsDialogShowing.value
            }) {
            Text(
                "Conversations", color = primaryColor.value, modifier = Modifier.padding(16.dp)
            )
        }

        Divider(color = primaryColor.value)

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isThemeDialogShowing.value = !isThemeDialogShowing.value
            }) {
            Text(
                "Theme", color = primaryColor.value, modifier = Modifier.padding(16.dp)
            )
        }

        Divider(color = primaryColor.value)

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isSettingsDialogShowing.value = !isSettingsDialogShowing.value
            }) {
            Text(
                "Settings", color = primaryColor.value, modifier = Modifier.padding(16.dp)
            )
        }

        Divider(color = primaryColor.value)

        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isHowToUseShowing.value = !isHowToUseShowing.value
            }) {
            Text(
                "How to use", color = primaryColor.value, modifier = Modifier.padding(16.dp)
            )
        }

        Divider(color = primaryColor.value)
    }) {

        Column(
            modifier = Modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(5.dp))

            ChatHeader(
                modifier = Modifier.padding(5.dp),
                conversationName = conversationHeaderName,
                isChatGptTyping = isChatGptTyping,
                primaryColor = primaryColor,
                chats = chats,
                scope = scope,
                listState = chatListState,
            ) {
                scope.launch {
                    with(scaffoldState.drawerState) {
                        focusManager.clearFocus()
                        if (isClosed) open() else close()
                    }
                }
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
                secondaryColor = secondaryColor
            )

            ChatTextFieldRow(promptText = promptText.value,
                sendOnClick = { sendOnClick() },
                textFieldOnValueChange = { text -> promptText.value = text },
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
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
                    .clickable { sendOnClick() },
                micIconModifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .weight(.2f)
                    .size(30.dp)
                    .padding(end = 10.dp)
                    .clickable {
                        speechToText.launch(getSpeechInputIntent(context))
                    })
        }
    }
}