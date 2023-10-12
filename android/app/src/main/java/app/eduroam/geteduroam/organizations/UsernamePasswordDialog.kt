package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.eduroam.geteduroam.R

@Composable
fun UsernamePasswordDialog(
    requiredSuffix: String?,
    cancel: () -> Unit = { },
    logIn: (String, String) -> Unit = { _, _ -> }
) = Dialog(onDismissRequest = { }) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.username_password_login_required),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(id = R.string.username_password_please_enter))
            Spacer(modifier = Modifier.size(16.dp))
            TextField(
                value = username,
                onValueChange = {
                    username = it
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = {
                    val exampleUsername =
                        stringResource(id = R.string.username_password_placeholder_username_before_suffix)
                    val suffix = requiredSuffix
                        ?: stringResource(id = R.string.username_password_placeholder_username_example_suffix)
                    Text("${exampleUsername}@${suffix}", modifier = Modifier.alpha(0.5f))
                },
                label = {
                    Text(
                        stringResource(id = R.string.username_password_label_username),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
            TextField(
                value = password,
                onValueChange = {
                    password = it
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = {
                    Text(
                        stringResource(id = R.string.username_password_label_password),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible) {
                        stringResource(id = R.string.username_password_accessibility_hide_password)
                    } else {
                        stringResource(id = R.string.username_password_accessibility_show_password)
                    }

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row {
                Button(
                    onClick = {
                        cancel()
                    },
                ) {
                    Text(stringResource(id = R.string.username_password_button_cancel))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        logIn(username, password)
                    },
                    enabled = username.isNotBlank() && password.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.username_password_button_log_in))
                }
            }
        }
    }
}