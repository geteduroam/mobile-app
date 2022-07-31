package app.eduroam.geteduroam.institutions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.eduroam.shared.response.Institution


@Composable
fun InstitutionRow(
    institution: Institution,
    onSelectInstitution: (Institution) -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier
        .fillMaxWidth()
        .clickable { onSelectInstitution(institution) }) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = institution.name,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = institution.country,
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    Divider(
        Modifier
            .height(0.5.dp)
            .fillMaxWidth()
    )
}