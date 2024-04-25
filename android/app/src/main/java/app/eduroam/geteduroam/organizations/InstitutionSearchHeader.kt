package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OrganizationSearchHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onPositionDetermined: (IntOffset) -> Unit,
    showExtraActionsPopup: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier.fillMaxWidth()) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    // Long click, open popup
                    showExtraActionsPopup()
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        /* Regular click, ignore */
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    TextField(
        value = searchText,
        interactionSource = interactionSource,
        onValueChange = onSearchTextChange,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(30.dp)
                    .onGloballyPositioned {
                        val position = it.positionInWindow()
                        onPositionDetermined(IntOffset(position.x.toInt(), position.y.toInt()))
                    }
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.organization_search_text),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        },

        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions {
            focusManager.clearFocus()
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
        )
    )
}


@ExperimentalMaterial3Api
@Preview
@Composable
private fun MarketPlaceHeader_Preview() {
    AppTheme {
        OrganizationSearchHeader("filterOn", {}, {}, {})
    }
}