package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.eduroam.geteduroam.R

@Composable
fun LoginDialog(
    onConfirmClicked: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val openDialog = remember { mutableStateOf(true) }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    if (openDialog.value) {
        Dialog(
            onDismissRequest = onDismiss,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.login_dialog_title))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .weight(weight = 1f, fill = false)
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_dialog_text)
                        )

                        OutlinedTextField(
                            value = username.value,
                            onValueChange = {
                                username.value = it
                            },
                            Modifier.padding(top = 8.dp),
                            label = { Text(text = stringResource(R.string.login_dialog_username)) },
                            textStyle = TextStyle(color = Color.White)
                        )

                        OutlinedTextField(
                            value = password.value,
                            onValueChange = {
                                password.value = it
                            },
                            Modifier.padding(top = 8.dp),
                            label = { Text(text = stringResource(R.string.login_dialog_password)) },
                            textStyle = TextStyle(color = Color.White),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done, keyboardType = KeyboardType.Password
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            openDialog.value = false
                            onDismiss()
                        }) {
                            Text(text = stringResource(R.string.login_dialog_cancel))
                        }
                        TextButton(onClick = {
                            openDialog.value = false
                            onDismiss()
                            onConfirmClicked(username.value, password.value)
                        }) {
                            Text(text = stringResource(R.string.login_dialog_login))
                        }
                    }
                }
            }
        }
    }
}
