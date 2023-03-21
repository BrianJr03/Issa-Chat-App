package jr.brian.issaaiapp.view.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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