package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.eduroam.geteduroam.R

@Composable
fun UsernamePasswordDialog(
    requiredSuffix: String?,
    cancel: () -> Unit = { },
    logIn: (String, String) -> Unit = { _, _ -> }
) = Dialog(
    onDismissRequest = { },
    properties = DialogProperties(decorFitsSystemWindows = false)
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val requiredSuffixError = requiredSuffix?.let { stringResource(id = R.string.username_password_error_required_suffix, it) }
    Column {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.username_password_login_required),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = stringResource(id = R.string.username_password_please_enter))
                Spacer(modifier = Modifier.size(16.dp))
                // Username / password input fields need to be LTR even in RTL languages
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && username.isNotEmpty()) {
                                    if (username.isNotEmpty() &&
                                        !requiredSuffix.isNullOrEmpty() &&
                                        !username.contains("@")
                                    ) {
                                        username += "@$requiredSuffix"
                                    }
                                }
                            },
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        supportingText = {
                            usernameError?.let { nonNullError ->
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    text = nonNullError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrect = false, imeAction = ImeAction.Next),
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
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                        maxLines = 1,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    logIn(username, password)
                                } else {
                                    keyboardController?.hide()
                                }
                            }
                        ),
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
                }
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
                            if (!requiredSuffix.isNullOrEmpty() && !username.endsWith(requiredSuffix, ignoreCase = false)) {
                                usernameError = requiredSuffixError
                            } else {
                                usernameError = null
                                logIn(username, password)
                            }
                        },
                        enabled = username.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(stringResource(id = R.string.username_password_button_log_in))
                    }
                }
            }
        }
        // Spacer to slide the dialog up when the keyboard shows
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .size(16.dp)
        )
    }
}