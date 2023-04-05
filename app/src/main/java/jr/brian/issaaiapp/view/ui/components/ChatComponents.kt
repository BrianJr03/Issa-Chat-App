package jr.brian.issaaiapp.view.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.senderAndTimeStyle
import jr.brian.issaaiapp.view.ui.theme.CardinalRed
import jr.brian.issaaiapp.view.ui.theme.TextWhite
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
private fun LottieLoading(isChatGptTyping: MutableState<Boolean>) {
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
        progress,
        modifier = Modifier.size(40.dp)
    )
}

@Composable
private fun MenuIcon(primaryColor: MutableState<Color>, onClick: () -> Unit) {
    Icon(
        painter = painterResource(id = R.drawable.baseline_menu_40),
        tint = primaryColor.value,
        contentDescription = "Menu Icon",
        modifier = Modifier
            .size(45.dp)
            .padding(start = 15.dp)
            .clickable {
                onClick()
            },
    )
}

@Composable
fun ChatHeader(
    modifier: Modifier,
    isChatGptTyping: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    onMenuClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        if (isChatGptTyping.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MenuIcon(primaryColor = primaryColor) { onMenuClick() }
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "ChatGPT is typing",
                    color = primaryColor.value,
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                LottieLoading(isChatGptTyping)
                Spacer(modifier = Modifier.weight(.1f))
            }
        }
        if (isChatGptTyping.value.not()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuIcon(primaryColor = primaryColor) { onMenuClick() }
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "${stringResource(id = R.string.app_name)} x ChatGPT",
                    color = primaryColor.value,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.weight(.1f))
            }
        }

    }
}

@Composable
fun ChatSection(
    modifier: Modifier,
    dao: ChatsDao,
    chats: MutableList<Chat>,
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    viewModel: MainViewModel,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val copyToastMsgs = listOf(
        "Your copy is ready for pasta!",
        "What are you waiting for? Paste!",
        "Your clipboard has been blessed.",
        "Chat copied!",
        "Copied, the chat has been."
    )
    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) { index ->
            val chat = chats[index]
            val isHumanChatBox = chat.senderLabel != SenderLabel.CHATGPT_SENDER_LABEL
            val color = if (isHumanChatBox) primaryColor.value else secondaryColor.value
            val isDeleteDialogShowing = remember { mutableStateOf(false) }

            DeleteChatDialog(isShowing = isDeleteDialogShowing, primaryColor = primaryColor) {
                chats.remove(chat)
                dao.removeChat(chat)
                scope.launch { scaffoldState.drawerState.close() }
            }

            ChatBox(
                text = chat.text,
                color = color,
                context = context,
                senderLabel = chat.senderLabel,
                dateSent = chat.dateSent,
                timeSent = chat.timeSent,
                isHumanChatBox = isHumanChatBox,
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

@Composable
fun ChatTextFieldRow(
    promptText: String,
    sendOnClick: () -> Unit,
    textFieldOnValueChange: (String) -> Unit,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    modifier: Modifier,
    textFieldModifier: Modifier,
    sendIconModifier: Modifier,
    micIconModifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = textFieldModifier,
            value = promptText,
            onValueChange = textFieldOnValueChange,
            label = {
                Text(
                    text = "Enter a prompt",
                    style = TextStyle(
                        color = primaryColor.value,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = secondaryColor.value,
                unfocusedIndicatorColor = primaryColor.value
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { sendOnClick() })
        )
        Icon(
            painter = painterResource(id = R.drawable.send_icon),
            tint = primaryColor.value,
            contentDescription = "Send Message",
            modifier = sendIconModifier
        )
        Icon(
            painter = painterResource(id = R.drawable.baseline_mic_24),
            tint = Color.Gray,
            contentDescription = "Mic",
            modifier = micIconModifier
        )
    }
    Spacer(Modifier.height(15.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBox(
    text: String,
    color: Color,
    context: Context,
    senderLabel: String,
    dateSent: String,
    timeSent: String,
    isHumanChatBox: Boolean,
    onDeleteChat: () -> Unit,
    onStopAudioClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongCLick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val isChatInfoShowing = remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(.8f),
            horizontalAlignment = if (isHumanChatBox) Alignment.End else Alignment.Start
        ) {
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
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = Color.DarkGray,
                    backgroundColor = Color.DarkGray
                )
                CompositionLocalProvider(
                    LocalTextSelectionColors provides customTextSelectionColors
                ) {
                    SelectionContainer {
                        Text(
                            text,
                            style = TextStyle(color = TextWhite),
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(
                    start = if (isHumanChatBox) 0.dp else 10.dp,
                    end = if (isHumanChatBox) 10.dp else 0.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    senderLabel,
                    style = senderAndTimeStyle(color),
                    modifier = Modifier
                )

                if (isChatInfoShowing.value) {
                    Spacer(Modifier.width(5.dp))
                    Text("•", style = senderAndTimeStyle(color))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        dateSent,
                        style = senderAndTimeStyle(color),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("•", style = senderAndTimeStyle(color))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        timeSent,
                        style = senderAndTimeStyle(color),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("•", style = senderAndTimeStyle(color))
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_stop_24),
                        tint = color,
                        contentDescription = "Stop Audio",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                onStopAudioClick()
                                isChatInfoShowing.value = false
                                Toast
                                    .makeText(context, "Chat audio stopped", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Delete Chat",
                        tint = CardinalRed,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                onDeleteChat()
                                isChatInfoShowing.value = false
                            }
                    )
                }
            }
        }
    }
}