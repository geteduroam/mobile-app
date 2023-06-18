package app.eduroam.geteduroam.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AlertDialogWithSingleButton(
    title: String,
    explanation: String,
    buttonLabel: String,
    onDismiss: () -> Unit = {},
) {
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
                onDismiss()
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        onDismiss()
                    }
                ) {
                    Text(
                        text = buttonLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        )
    }
}