package jr.brian.issaaiapp.view.ui.components

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import jr.brian.issaaiapp.R
import jr.brian.issaaiapp.model.local.Chat
import jr.brian.issaaiapp.util.SenderLabel
import jr.brian.issaaiapp.util.senderAndTimeStyle
import jr.brian.issaaiapp.view.ui.theme.AIChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.HumanChatBoxColor
import jr.brian.issaaiapp.view.ui.theme.TextWhite
import jr.brian.issaaiapp.viewmodel.MainViewModel

@Composable
fun LottieLoading(isChatGptTyping: MutableState<Boolean>) {
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
fun MenuIcon(onMenuClick: () -> Unit) {
    Icon(
        painter = painterResource(id = R.drawable.baseline_menu_40),
        tint = MaterialTheme.colors.primary,
        contentDescription = "Menu Icon",
        modifier = Modifier
            .size(45.dp)
            .padding(start = 15.dp)
            .clickable {
                onMenuClick()
            },
    )
}

@Composable
fun ChatHeader(
    modifier: Modifier,
    isChatGptTyping: MutableState<Boolean>,
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
                MenuIcon(onMenuClick)
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "ChatGPT is typing",
                    color = MaterialTheme.colors.primary,
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
                MenuIcon(onMenuClick)
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    "Issa AI App x ChatGPT",
                    color = MaterialTheme.colors.primary,
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
    chats: MutableList<Chat>,
    listState: LazyListState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
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
            val isHumanChatBox = chats[index].senderLabel == SenderLabel.HUMAN_SENDER_LABEL
            val color = if (isHumanChatBox) HumanChatBoxColor else AIChatBoxColor
            ChatBox(
                text = chats[index].text,
                color = color,
                senderLabel = chats[index].senderLabel,
                dateSent = chats[index].dateSent,
                timeSent = chats[index].timeSent,
                isHumanChatBox = isHumanChatBox,
                onDoubleClick = {
                    viewModel.stopSpeech()
                    viewModel.textToSpeech(context, chats[index].text)
                }
            ) {
                clipboardManager.setText(AnnotatedString((chats[index].text)))
                Toast.makeText(
                    context,
                    copyToastMsgs.random(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun ChatTextFieldRows(
    promptText: String,
    sendOnClick: () -> Unit,
    textFieldOnValueChange: (String) -> Unit,
    isConvoContextFieldShowing: MutableState<Boolean>,
    modifier: Modifier,
    textFieldModifier: Modifier,
    sendIconModifier: Modifier
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
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = MaterialTheme.colors.secondary,
                unfocusedIndicatorColor = MaterialTheme.colors.primary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { sendOnClick() })
        )

        Icon(
            painter = painterResource(id = R.drawable.send_icon),
            tint = MaterialTheme.colors.primary,
            contentDescription = "Send Message",
            modifier = sendIconModifier
        )
    }

    if (isConvoContextFieldShowing.value) {
        Spacer(Modifier.height(10.dp))

    }
    Spacer(Modifier.height(15.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBox(
    text: String,
    color: Color,
    senderLabel: String,
    dateSent: String,
    timeSent: String,
    isHumanChatBox: Boolean,
    onDoubleClick: () -> Unit,
    onLongCLick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isTimeAndDateShowing = remember { mutableStateOf(false) }
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
                            isTimeAndDateShowing.value = !isTimeAndDateShowing.value
                        },
                        onDoubleClick = { onDoubleClick() },
                        onLongClick = { onLongCLick() },
                    )
            ) {
                Text(
                    text,
                    style = TextStyle(color = TextWhite),
                    modifier = Modifier.padding(15.dp)
                )
            }
            Row(
                modifier = Modifier.padding(
                    start = if (isHumanChatBox) 0.dp else 10.dp,
                    end = if (isHumanChatBox) 10.dp else 0.dp
                )
            ) {
                Text(
                    senderLabel,
                    style = senderAndTimeStyle(color),
                    modifier = Modifier
                )

                if (isTimeAndDateShowing.value) {
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
            }
        }
    }
}