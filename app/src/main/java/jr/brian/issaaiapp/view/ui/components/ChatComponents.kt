package jr.brian.issaaiapp.view.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import dev.jeziellago.compose.markdowntext.MarkdownText
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.senderAndTimeStyle
import jr.brian.issaaiapp.view.ui.theme.CardinalRed
import jr.brian.issaaiapp.view.ui.theme.TextWhite
import jr.brian.issaaiapp.view.ui.util.ScaleAndAlphaArgs
import jr.brian.issaaiapp.view.ui.util.calculateDelayAndEasing
import jr.brian.issaaiapp.view.ui.util.copyToastMsgs
import jr.brian.issaaiapp.view.ui.util.scaleAndAlpha
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
    Displays a Lottie animation of a loading indicator.

    @param [isChatGptTyping] A [MutableState] indicating if the ChatGPT is typing or not.

    @param [modifier] Optional [Modifier] to apply to the component.
 */
@Composable
private fun LottieLoading(
    isChatGptTyping: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val isPlaying by remember { mutableStateOf(isChatGptTyping.value) }
    val speed by remember { mutableStateOf(1f) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = false
    )
    LottieAnimation(
        composition,
        { progress },
        modifier = modifier
    )
}

/**
    Composable function that displays a menu icon.

    @param [primaryColor] the primary color to use for the icon tint.

    @param [isAmoledThemeToggled] a boolean state that determines whether the AMOLED theme
    is toggled.

    @param [modifier] optional [Modifier] to modify the layout or behavior of the icon.
 */
@Composable
private fun MenuIcon(
    primaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val color = if (isAmoledThemeToggled.value) Color.White else primaryColor.value
    Icon(
        painter = painterResource(id = R.drawable.baseline_menu_40),
        tint = color,
        contentDescription = "Menu Icon",
        modifier = modifier
    )
}

/**
    Composable function that represents the header of the chat screen.

    @param [conversationName] the name of the current conversation.

    @param [isChatGptTyping] a mutable state that represents whether ChatGPT is currently typing.

    @param [isAmoledThemeToggled] a mutable state that represents whether the AMOLED theme is
    toggled.

    @param [primaryColor] a mutable state that represents the primary color of the app.

    @param [chats] a mutable list of chats in the current conversation.

    @param [scope] the coroutine scope to be used for animations and other asynchronous operations.

    @param [listState] a lazy list state to be used to manipulate the scrolling of the chat list.

    @param [modifier] optional modifier for the entire composable.

    @param [headerTextModifier] optional modifier for the conversation name text.

    @param [onMenuClick] a lambda function to be executed when the menu icon is clicked.

    @param [onResetAllChats] a lambda function to be executed when the reset conversation button
    is clicked.
 */
@Composable
fun ChatHeader(
    conversationName: MutableState<String>,
    isChatGptTyping: MutableState<Boolean>,
    isAmoledThemeToggled: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    chats: MutableList<Chat>,
    scope: CoroutineScope,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    headerTextModifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onResetAllChats: () -> Unit
) {
    val isDeleteDialogShowing = remember { mutableStateOf(false) }

    DeleteDialog(
        title = "Reset this Conversation?",
        isShowing = isDeleteDialogShowing,
        primaryColor = primaryColor
    ) {
        onResetAllChats()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        val color = if (isAmoledThemeToggled.value) Color.White else primaryColor.value
        if (isChatGptTyping.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MenuIcon(
                    primaryColor = primaryColor,
                    isAmoledThemeToggled = isAmoledThemeToggled,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(start = 15.dp)
                        .clickable {
                            onMenuClick()
                        })
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "ChatGPT is typing",
                    color = color,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                LottieLoading(
                    isChatGptTyping = isChatGptTyping,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.weight(.1f))
                if (listState.canScrollForward) {
                    EndText(
                        primaryColor = primaryColor,
                        isAmoledThemeToggled = isAmoledThemeToggled,
                        modifier = Modifier.clickable {
                            scope.launch {
                                listState.animateScrollToItem(chats.size)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
        if (isChatGptTyping.value.not()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuIcon(
                    primaryColor = primaryColor,
                    isAmoledThemeToggled = isAmoledThemeToggled,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(start = 15.dp)
                        .clickable {
                            onMenuClick()
                        })
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    conversationName.value,
                    color = color,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = headerTextModifier
                )
                Spacer(modifier = Modifier.weight(.1f))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                    tint = color,
                    contentDescription = "Reset Conversation",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            isDeleteDialogShowing.value = !isDeleteDialogShowing.value
                        }
                )
                if (listState.canScrollForward) {
                    Spacer(modifier = Modifier.width(15.dp))
                }
                AnimatedVisibility(visible = listState.canScrollForward) {
                    EndText(
                        primaryColor = primaryColor,
                        isAmoledThemeToggled = isAmoledThemeToggled,
                        modifier = Modifier.clickable {
                            scope.launch {
                                listState.animateScrollToItem(chats.size)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}


/**
    Composable function that displays the "End" text using the given primary color and theme
    toggle state. The color of the text changes based on whether the amoled theme is toggled or not.

    @param [primaryColor] the mutable state of the primary color to use for the text

    @param [isAmoledThemeToggled] the mutable state of the amoled theme toggle

    @param modifier optional [Modifier] for modifying the layout of the composable
 */
@Composable
fun EndText(
    primaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val color = if (isAmoledThemeToggled.value) Color.White else primaryColor.value
    Text(
        "End",
        color = color,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        modifier = modifier
    )
}


/**
    Composable function that represents the section containing the chats. It displays a
    list of [ChatBox]  and handles user interactions such as deleting, copying and reading out the
    chat messages. If there are no chats available, it displays a message indicating so.

    @param [dao] The data access object for the chats.

    @param [chats] The list of chats to display.

    @param [listState] The state of the [LazyListState] to be used for the list of chats.

    @param [scaffoldState] The state of the [ScaffoldState] to be used for showing the
    delete dialog.

    @param [viewModel] The [MainViewModel] for the app.

    @param [primaryColor] The [MutableState] representing the primary color of the app.

    @param [secondaryColor] The [MutableState] representing the secondary color of the app.

    @param [isAmoledThemeToggled] The [MutableState] representing whether the AMOLED theme
    is toggled on or off.

    @param [modifier] Optional [Modifier] to be applied to the chat section.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatSection(
    dao: ChatsDao,
    chats: MutableList<Chat>,
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    viewModel: MainViewModel,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val textColor = if (isAmoledThemeToggled.value) Color.White else primaryColor.value

    if (chats.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                "No Chats Recorded",
                color = textColor,
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }

    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) { index ->
            val (delay, easing) = listState.calculateDelayAndEasing(index, 1)
            val animation =
                tween<Float>(durationMillis = 150, delayMillis = delay, easing = easing)
            val args =
                ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
            val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)

            val chat = chats[index]
            val isHumanChatBox = chat.senderLabel != SenderLabel.CHATGPT_SENDER_LABEL
            val color = if (isHumanChatBox) primaryColor.value else secondaryColor.value
            val humanBoxColor = if (isAmoledThemeToggled.value) Color.Black else color
            val aiBoxColor = if (isAmoledThemeToggled.value) Color.Black else color
            val labelColor = if (isAmoledThemeToggled.value) Color.White else color
            val isDeleteDialogShowing = remember { mutableStateOf(false) }

            DeleteDialog(
                title = "Delete this Chat?",
                isShowing = isDeleteDialogShowing,
                primaryColor = primaryColor
            ) {
                chats.remove(chat)
                dao.removeChat(chat)
                scope.launch { scaffoldState.drawerState.close() }
            }

            ChatBox(
                text = chat.text,
                color = if (isHumanChatBox) humanBoxColor else aiBoxColor,
                labelColor = labelColor,
                context = context,
                senderLabel = chat.senderLabel,
                dateSent = chat.dateSent,
                timeSent = chat.timeSent,
                isHumanChatBox = isHumanChatBox,
                isAmoledThemeToggled = isAmoledThemeToggled,
                modifier = Modifier
                    .padding(10.dp)
                    .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
                    .indication(interactionSource, LocalIndication.current)
                    .animateItemPlacement(),
                onDeleteChat = {
                    isDeleteDialogShowing.value = true
                },
                onDoubleClick = {
                    viewModel.stopSpeech()
                    viewModel.textToSpeech(context, chat.text)
                },
                onStopAudioClick = {
                    viewModel.stopSpeech()
                },
                onLongCLick = {
                    scope.launch {
                        val snackResult = scaffoldState.snackbarHostState.showSnackbar(
                            message = "Copy all text?",
                            actionLabel = "Yes",
                            duration = SnackbarDuration.Short
                        )
                        when (snackResult) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                clipboardManager.setText(AnnotatedString((chat.text)))
                                Toast.makeText(
                                    context,
                                    copyToastMsgs.random(),
                                    Toast.LENGTH_LONG
                                ).show()
                                focusManager.clearFocus()
                            }
                        }
                    }
                }
            )
        }
    }
}


/**
    A composable function that displays a chat text field row, which includes an OutlinedTextField
    and icons for sending a message and using the microphone for voice input.

    @param [promptText] the current text in the text field

    @param [textFieldOnValueChange] a lambda function that is called whenever the value of the text
    field changes

    @param [primaryColor] the primary color of the chat text field row

    @param [secondaryColor] the secondary color of the chat text field row

    @param [isAmoledThemeToggled] a mutable state boolean that indicates whether the AMOLED theme
    is toggled

    @param [modifier] optional Modifier for the OutlinedTextField

    @param [sendIconModifier] optional Modifier for the send icon

    @param [micIconModifier] optional Modifier for the microphone icon

    @return a chat text field row composable with an OutlinedTextField and icons for sending a
    message and using the microphone for voice input.
 */
@Composable
fun ChatTextFieldRow(
    promptText: String,
    textFieldOnValueChange: (String) -> Unit,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    sendIconModifier: Modifier = Modifier,
    micIconModifier: Modifier = Modifier
) {
    val primary = if (isAmoledThemeToggled.value) Color.White else primaryColor.value
    val micColor = if (isAmoledThemeToggled.value) Color.White else Color.Gray

    OutlinedTextField(
        modifier = modifier,
        value = promptText,
        onValueChange = textFieldOnValueChange,
        label = {
            Text(
                text = "Enter a prompt",
                style = TextStyle(
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = primary,
            focusedIndicatorColor = secondaryColor.value,
            unfocusedIndicatorColor = primary
        ),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24),
                tint = micColor,
                contentDescription = "Mic",
                modifier = micIconModifier
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.send_icon),
                tint = primary,
                contentDescription = "Send Message",
                modifier = sendIconModifier
            )
        }
    )

    Spacer(Modifier.height(15.dp))
}


/**
    Composable function that displays a chat box which includes chat details such as sender label,
    date and time sent, and chat text.

    @param [text] the chat text to display

    @param [color] the background color of the chat box

    @param [labelColor] the color of the sender label, date and time sent, and action buttons

    @param [context] the context of the chat box

    @param [senderLabel] the label of the sender of the chat

    @param [dateSent] the date the chat was sent

    @param [timeSent] the time the chat was sent

    @param [isHumanChatBox] a boolean value indicating whether the chat box is for a human

    @param [isAmoledThemeToggled] a mutable state boolean that indicates whether the AMOLED theme
    is toggled

    @param [modifier] optional Modifier for the chat box

    @param [onDeleteChat] a lambda function that is called when the user wants to delete the chat

    @param [onStopAudioClick] a lambda function that is called when the user wants to stop
    audio playback

    @param [onDoubleClick] a lambda function that is called when the user double-clicks the
    chat box

    @param [onLongCLick] a lambda function that is called when the user long-presses the chat box
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBox(
    text: String,
    color: Color,
    labelColor: Color,
    context: Context,
    senderLabel: String,
    dateSent: String,
    timeSent: String,
    isHumanChatBox: Boolean,
    isAmoledThemeToggled: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onDeleteChat: () -> Unit,
    onStopAudioClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongCLick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val isChatInfoShowing = remember { mutableStateOf(false) }
    val isShowingMarkdown = remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.weight(.8f),
            horizontalAlignment = if (isHumanChatBox) Alignment.End else Alignment.Start
        ) {
            AnimatedVisibility(visible = isChatInfoShowing.value) {
                Column(
                    horizontalAlignment = if (isHumanChatBox) Alignment.End else Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = if (isHumanChatBox) 0.dp else 10.dp,
                            end = if (isHumanChatBox) 10.dp else 0.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            senderLabel,
                            style = senderAndTimeStyle(labelColor),
                            modifier = Modifier
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("•", style = senderAndTimeStyle(labelColor))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            dateSent,
                            style = senderAndTimeStyle(labelColor),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("•", style = senderAndTimeStyle(labelColor))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            timeSent,
                            style = senderAndTimeStyle(labelColor),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(
                            start = if (isHumanChatBox) 0.dp else 10.dp,
                            end = if (isHumanChatBox) 10.dp else 0.dp
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stop Audio",
                            color = labelColor,
                            modifier = Modifier.clickable {
                                onStopAudioClick()
                                isChatInfoShowing.value = false
                                Toast
                                    .makeText(
                                        context,
                                        "Chat audio stopped",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("•", style = senderAndTimeStyle(labelColor))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isShowingMarkdown.value.not()) "Markdown" else "Default",
                            color = labelColor,
                            modifier = Modifier.clickable {
                                isShowingMarkdown.value = !isShowingMarkdown.value
                            }
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("•", style = senderAndTimeStyle(labelColor))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Delete",
                            color = CardinalRed,
                            modifier = Modifier.clickable {
                                onDeleteChat()
                                isChatInfoShowing.value = false
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .combinedClickable(
                        onClick = {
                            focusManager.clearFocus()
                            isChatInfoShowing.value = !isChatInfoShowing.value
                        },
                        onDoubleClick = { onDoubleClick() },
                        onLongClick = { onLongCLick() },
                    )
            ) {
                val copyColor = if (isAmoledThemeToggled.value) Color.Gray else Color.DarkGray
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = copyColor,
                    backgroundColor = copyColor
                )
                if (isShowingMarkdown.value.not()) {
                    CompositionLocalProvider(
                        LocalTextSelectionColors provides customTextSelectionColors
                    ) {
                        SelectionContainer {
                            Text(
                                text,
                                color = if (isAmoledThemeToggled.value) Color.White else TextWhite,
                                modifier = Modifier.padding(15.dp)
                            )
                        }
                    }
                } else {
                    MarkdownText(
                        modifier = Modifier.padding(15.dp),
                        markdown = text,
                        color = if (isAmoledThemeToggled.value) Color.White else TextWhite,
                        onClick = {
                            focusManager.clearFocus()
                            isChatInfoShowing.value = !isChatInfoShowing.value
                        }
                    )
                }
            }
        }
    }
}