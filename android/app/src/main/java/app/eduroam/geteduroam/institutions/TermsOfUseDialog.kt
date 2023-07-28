package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.eduroam.geteduroam.R

@Composable
fun TermsOfUseDialog(
    onConfirmClicked: () -> Unit,
    onDismiss: () -> Unit,
) = Dialog(
    onDismissRequest = onDismiss,
) {
    Surface(
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.terms_of_use_dialog_title))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(weight = 1f, fill = false)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.terms_of_use_dialog_text)
                )
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.terms_of_use_dialog_agree))
                }
                TextButton(onClick = onConfirmClicked) {
                    Text(text = stringResource(R.string.terms_of_use_dialog_read_tou))
                }
                TextButton(onClick = onConfirmClicked) {
                    Text(text = stringResource(R.string.terms_of_use_dialog_disagree))
                }
            }
        }
    }
}
