package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.ui.theme.AppTheme

@Composable
fun OrganizationSearchHeader(
    searchText: String, onSearchTextChange: (String) -> Unit, modifier: Modifier = Modifier,
) = Column(modifier.fillMaxWidth()) {
    val focusManager = LocalFocusManager.current
    Spacer(Modifier.height(16.dp))
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(30.dp)
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
        OrganizationSearchHeader("filterOn", {})
    }
}