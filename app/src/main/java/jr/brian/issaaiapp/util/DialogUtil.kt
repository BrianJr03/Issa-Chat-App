package jr.brian.issaaiapp.util

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jr.brian.issaaiapp.view.ui.theme.TextWhite

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
fun EmptyTextFieldDialog(title: String, isShowing: MutableState<Boolean>) {
    ShowDialog(
        title = title,
        titleColor = MaterialTheme.colors.primary,
        content = {
            Column {
                Text(
                    "The text field can not be empty.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.primary
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MaterialTheme.colors.primary
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
fun SettingsDialog(
    apiKey: String,
    textFieldOnValueChange: (String) -> Unit,
    isShowing: MutableState<Boolean>,
    onSaveApiKey: () -> Unit,
    onDeleteAllChats: () -> Unit,
    modifier: Modifier,
    textFieldModifier: Modifier
) {
    ShowDialog(
        title = "",
        content = {
            Spacer(modifier = Modifier.height(15.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                OutlinedTextField(
                    modifier = textFieldModifier,
                    value = apiKey,
                    onValueChange = textFieldOnValueChange,
                    label = {
                        Text(
                            text = "Enter your OpenAI Api-Key",
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
                    keyboardActions = KeyboardActions(onDone = { onSaveApiKey() })
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    onClick = {
                        onSaveApiKey()
                        isShowing.value = false
                    }) {
                    Text(text = "Save Api-Key", color = Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    ),
                    onClick = {
                        onDeleteAllChats()
                        isShowing.value = false
                    }) {
                    Text(text = "Delete all chats", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
                onClick = {
                    isShowing.value = false
                }) {
                Text(text = "Close", color = Color.White)
            }
        },
        dismissButton = { /*TODO*/ },
        isShowing = isShowing
    )
}