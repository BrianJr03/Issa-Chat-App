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
import androidx.compose.ui.graphics.graphicsLayer
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
import jr.brian.issaaiapp.view.ui.util.scaleAndAlpha
import jr.brian.issaaiapp.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
        progress,
        modifier = modifier
    )
}

@Composable
private fun MenuIcon(
    primaryColor: MutableState<Color>,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.baseline_menu_40),
        tint = primaryColor.value,
        contentDescription = "Menu Icon",
        modifier = modifier
    )
}

@Composable
fun ChatHeader(
    isChatGptTyping: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    chats: MutableList<Chat>,
    scope: CoroutineScope,
    listState: LazyListState,
    modifier: Modifier = Modifier,
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
                MenuIcon(
                    primaryColor = primaryColor,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(start = 15.dp)
                        .clickable {
                            onMenuClick()
                        })
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "ChatGPT is typing",
                    color = primaryColor.value,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
                    modifier = Modifier
                        .size(45.dp)
                        .padding(start = 15.dp)
                        .clickable {
                            onMenuClick()
                        })
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "${stringResource(id = R.string.app_name)} x ChatGPT",
                    color = primaryColor.value,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.weight(.1f))
                if (listState.canScrollForward) {
                    EndText(
                        primaryColor = primaryColor,
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

@Composable
fun EndText(
    primaryColor: MutableState<Color>,
    modifier: Modifier = Modifier
) {
    Text(
        "End",
        color = primaryColor.value,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
        modifier = modifier
    )
}

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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val copyToastMsgs = listOf(
        "Your copy is ready for pasta!",
        "What are you waiting for? Paste!",
        "Your clipboard has been blessed.",
        "Chat copied!",
        "Copied, the chat has been."
    )
    LazyColumn(modifier = modifier, state = listState) {
        items(chats.size) { index ->
            val (delay, easing) = listState.calculateDelayAndEasing(index, 1)
            val animation = tween<Float>(durationMillis = 150, delayMillis = delay, easing = easing)
            val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
            val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)

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

@Composable
fun ChatTextFieldRow(
    promptText: String,
    sendOnClick: () -> Unit,
    textFieldOnValueChange: (String) -> Unit,
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    sendIconModifier: Modifier = Modifier,
    micIconModifier: Modifier = Modifier
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
                if (isShowingMarkdown.value.not()) {
                    CompositionLocalProvider(
                        LocalTextSelectionColors provides customTextSelectionColors
                    ) {
                        SelectionContainer {
                            Text(
                                text,
                                color = TextWhite,
                                modifier = Modifier.padding(15.dp)
                            )
                        }
                    }
                } else {
                    MarkdownText(
                        modifier = Modifier.padding(15.dp),
                        markdown = text,
                        color = TextWhite,
                        onClick = {
                            focusManager.clearFocus()
                            isChatInfoShowing.value = !isChatInfoShowing.value
                        }
                    )
                }
            }
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
                            style = senderAndTimeStyle(color),
                            modifier = Modifier
                        )
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
                    }
                    Row(
                        modifier = Modifier.padding(
                            start = if (isHumanChatBox) 0.dp else 10.dp,
                            end = if (isHumanChatBox) 10.dp else 0.dp
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stop Audio",
                            color = color,
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
                        Text("•", style = senderAndTimeStyle(color))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isShowingMarkdown.value.not()) "Markdown" else "Default",
                            color = color,
                            modifier = Modifier.clickable {
                                isShowingMarkdown.value = !isShowingMarkdown.value
                            }
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("•", style = senderAndTimeStyle(color))
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

        }
    }
}