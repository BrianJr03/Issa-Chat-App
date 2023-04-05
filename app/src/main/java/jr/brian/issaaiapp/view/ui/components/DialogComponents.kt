package jr.brian.issaaiapp.view.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.util.ChatConfig
import jr.brian.issaaiapp.view.ui.theme.*

@Composable
private fun ShowDialog(
    title: String,
    titleColor: Color = TextWhite,
    content: @Composable (() -> Unit)?,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    isShowing: MutableState<Boolean>
) {
    if (isShowing.value) {
        AlertDialog(
            title = { Text(title, fontSize = 22.sp, color = titleColor) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            onDismissRequest = { isShowing.value = false },
        )
    }
}

@Composable
fun ThemeDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    isThemeOneToggled: Boolean,
    isThemeTwoToggled: Boolean,
    isThemeThreeToggled: Boolean,
    onThemeOneChange: ((Boolean) -> Unit)?,
    onThemeTwoChange: ((Boolean) -> Unit)?,
    onThemeThreeChange: ((Boolean) -> Unit)?,
) {
    ShowDialog(
        title = "Select App Theme",
        titleColor = primaryColor.value,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ThemeRow(
                    primaryColor = DefaultPrimaryColor,
                    secondaryColor = DefaultSecondaryColor,
                    isThemeToggled = isThemeOneToggled,
                    onThemeChange = onThemeOneChange
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeRow(
                    primaryColor = ThemeTwoPrimary,
                    secondaryColor = ThemeTwoSecondary,
                    isThemeToggled = isThemeTwoToggled,
                    onThemeChange = onThemeTwoChange
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeRow(
                    primaryColor = ThemeThreePrimary,
                    secondaryColor = ThemeThreeSecondary,
                    isThemeToggled = isThemeThreeToggled,
                    onThemeChange = onThemeThreeChange
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Dismiss", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
private fun ThemeRow(
    primaryColor: Color,
    secondaryColor: Color,
    isThemeToggled: Boolean,
    onThemeChange: ((Boolean) -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isThemeToggled,
            onCheckedChange = onThemeChange
        )
        Spacer(modifier = Modifier.width(25.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(primaryColor)
        )
        Spacer(modifier = Modifier.width(25.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(secondaryColor)
        )
    }
}

@Composable
fun DeleteChatDialog(
    isShowing: MutableState<Boolean>,
    primaryColor: MutableState<Color>,
    onDeleteClick: () -> Unit
) {
    ShowDialog(
        title = "Delete this Chat?",
        titleColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "This can't be undone.",
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    onDeleteClick()
                    isShowing.value = false
                }) {
                Text(text = "Delete", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Cancel", color = Color.White)
            }
        },
        isShowing = isShowing
    )
}

@Composable
fun HowToUseDialog(isShowing: MutableState<Boolean>, primaryColor: MutableState<Color>) {
    ShowDialog(
        title = "How to use",
        titleColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "You must provide your own OpenAI API Key in Settings to use this app.",
                    fontSize = 17.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Single Tap to toggle a Chat's date and time",
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Double Tap to play the Chat's text as audio",
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Long Tap to copy the Chat's text",
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Conversational Context: Use this to have ChatGPT respond a certain way.",
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "\"${ChatConfig.conversationalContext.random()}\"",
                    color = primaryColor.value,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "OK", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@Composable
fun EmptyPromptDialog(isShowing: MutableState<Boolean>, primaryColor: MutableState<Color>) {
    ShowDialog(
        title = "Please provide a prompt",
        titleColor = primaryColor.value,
        content = {
            Column {
                Text(
                    "The text field can not be empty.",
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "OK", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsDialog(
    primaryColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    apiKey: String,
    apiKeyOnValueChange: (String) -> Unit,
    humanSenderLabel: String,
    senderLabelOnValueChange: (String) -> Unit,
    isShowing: MutableState<Boolean>,
    showChatsDeletionWarning: () -> Unit,
    onClearApiKey: () -> Unit,
    showClearApiKeyWarning: () -> Unit,
    onDeleteAllChats: () -> Unit,
    isAutoSpeakToggled: Boolean,
    onAutoSpeakCheckedChange: ((Boolean) -> Unit)?,
) {
    ShowDialog(
        title = "Settings",
        titleColor = primaryColor.value,
        content = {
            Spacer(modifier = Modifier.height(15.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Clear API Key",
                    color = CardinalRed,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showClearApiKeyWarning()
                        },
                        onLongClick = {
                            onClearApiKey()
                        }
                    ))

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Delete All Chats",
                    color = CardinalRed,
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            showChatsDeletionWarning()
                        },
                        onLongClick = {
                            onDeleteAllChats()
                        }
                    ))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAutoSpeakToggled,
                        onCheckedChange = onAutoSpeakCheckedChange
                    )
                    Text("Auto-play incoming Chat audio")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = humanSenderLabel,
                    onValueChange = senderLabelOnValueChange,
                    label = {
                        Text(
                            text = "Custom Sender Label",
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
                    keyboardActions = KeyboardActions(onDone = {
                        isShowing.value = false
                    })
                )

                Spacer(modifier = Modifier.height(25.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = apiKeyOnValueChange,
                    label = {
                        Text(
                            text = "Your OpenAI API Key goes here",
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
                    keyboardActions = KeyboardActions(onDone = {
                        isShowing.value = false
                    })
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = primaryColor.value
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Close", color = Color.White)
            }
        },
        dismissButton = {},
        isShowing = isShowing
    )
}