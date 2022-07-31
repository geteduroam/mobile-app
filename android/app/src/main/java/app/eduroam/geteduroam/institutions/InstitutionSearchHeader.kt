package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.ui.theme.AppTheme

@Composable
fun InstitutionSearchHeader(
    searchText: String, onSearchTextChange: (String) -> Unit, modifier: Modifier = Modifier,
) = Column(modifier.fillMaxWidth()) {
    val focusManager = LocalFocusManager.current
    Text(
        text = stringResource(id = R.string.institution_select_title),
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        singleLine = true,
        placeholder = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.institution_search_text),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Light
                )
            }
        },

        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions {
            focusManager.clearFocus()
        },
        modifier = Modifier.fillMaxWidth(),
    )
}


@ExperimentalMaterial3Api
@Preview
@Composable
private fun MarketPlaceHeader_Preview() {
    AppTheme {
        InstitutionSearchHeader("filterOn", {})
    }
}