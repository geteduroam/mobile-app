package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.terms_of_use_dialog_text)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
            ) {

                TextButton(onClick = onConfirmClicked) {
                    Text(text = stringResource(R.string.terms_of_use_dialog_agree))
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.terms_of_use_dialog_disagree))
                }
            }
        }
    }
}
