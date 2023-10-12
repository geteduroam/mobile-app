package app.eduroam.geteduroam.organizations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eduroam.geteduroam.models.Organization


@Composable
fun OrganizationRow(
    organization: Organization,
    onSelectOrganization: (Organization) -> Unit,
    modifier: Modifier = Modifier,
) = Surface(
    color = MaterialTheme.colorScheme.surface
) {
    Column(
    modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clickable { onSelectOrganization(organization) }) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = organization.nameOrId,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = organization.country,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(8.dp))
        Divider(
            Modifier
                .height(0.5.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}