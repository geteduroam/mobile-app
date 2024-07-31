package app.eduroam.geteduroam.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.ui.PrimaryButton
import app.eduroam.geteduroam.ui.theme.AppTheme
import kotlinx.coroutines.flow.stateIn
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import java.util.Date


@Composable
fun StatusScreen(
    viewModel: StatusScreenViewModel,
    goToInstitutionSelection: () -> Unit,
    renewAccount: (String) -> Unit,
    repairConfig: (ConfigSource, String, String?, EAPIdentityProviderList) -> Unit
) = EduTopAppBar(
    withBackIcon = false
) { paddingValues ->

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val organizationId by viewModel.organizationId
        .collectAsStateWithLifecycle(initialValue = "", lifecycle = lifecycle)
    val organizationName by viewModel.organizationName.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val configSource by viewModel.configSource.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val lastConfig by viewModel.lastConfig.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val expiryTimestampMs by viewModel.expiryTimestampMs.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)

    LaunchedEffect(organizationId) {
        if (organizationId == null) {
            goToInstitutionSelection()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        StatusScreenContent(
            paddingValues = paddingValues,
            debugOptionsEnabled = viewModel.uiState.showDebugOptions,
            lastConfig = lastConfig,
            configSource = configSource ?: ConfigSource.Unknown,
            organizationId = organizationId,
            organizationName = organizationName,
            expiryTimestampMs = expiryTimestampMs,
            goToInstitutionSelection = goToInstitutionSelection,
            renewAccount = renewAccount,
            repairConfig = { config ->
                repairConfig(
                    configSource ?: ConfigSource.Unknown,
                    organizationId.orEmpty(),
                    organizationName,
                    config
                )
            },
            showDebugOptions = {
                viewModel.uiState = viewModel.uiState.copy(showDebugOptions = true)
            }
        )
    }
}

@Composable
fun StatusScreenContent(
    paddingValues: PaddingValues,
    debugOptionsEnabled: Boolean,
    lastConfig: EAPIdentityProviderList?,
    configSource: ConfigSource,
    organizationId: String?,
    organizationName: String?,
    expiryTimestampMs: Long?,
    goToInstitutionSelection: () -> Unit,
    renewAccount: (String) -> Unit,
    repairConfig: (EAPIdentityProviderList) -> Unit,
    showDebugOptions: () -> Unit
) = Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.surface
) {
    // Center heart icon
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.fillMaxHeight(fraction = 0.3f))
        Icon(
            painterResource(R.drawable.ic_home_center),
            contentDescription = "App logo",
            tint = Color(0xFFBDD6E5),
            modifier = Modifier.size(150.dp)
        )
    }
    // Bottom right eduroam icon
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Spacer(Modifier.fillMaxHeight(fraction = 0.8f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                .background(Color.White)
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Image(
                painterResource(R.drawable.ic_home_bottom_right),
                contentDescription = "App logo",
                modifier = Modifier.width(120.dp)
            )
        }
    }
    // OAuth logins expire after a specific time, regular logins do not, we show different texts for these two
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(id = R.string.status_authenticated_by, organizationName ?: "?"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        val isExpired: Boolean
        if (expiryTimestampMs != null) {
            val configuration = LocalConfiguration.current
            Spacer(modifier = Modifier.size(16.dp))
            val nowDate = LocalDateTime.now()
            val expiryDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expiryTimestampMs), ZoneOffset.UTC)
            val expiryDate = Date(expiryTimestampMs)
            if (nowDate.isBefore(expiryDateTime)) {
                isExpired = false
                val duration = formatDuration(date1 = nowDate, date2 = expiryDateTime)
                if (duration != null) {
                    Text(
                        text = stringResource(id = R.string.status_account_valid_for),
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, configuration.locales[0])
                    val dateString = dateFormatter.format(expiryDate)
                    Text(
                        text = stringResource(id = R.string.status_account_valid_until, dateString),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                isExpired = true
                val duration = formatDuration(date1 = expiryDateTime, date2 = nowDate)
                if (duration != null) {
                    Text(
                        text = stringResource(id = R.string.status_account_expired_ago),
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, configuration.locales[0])
                    val dateString = dateFormatter.format(expiryDate)
                    Text(
                        text = stringResource(id = R.string.status_account_expired_on, dateString),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        } else {
            isExpired = false
        }

        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = stringResource(id = R.string.status_select_different_organization),
            onClick = {
                goToInstitutionSelection()
            })
        if (configSource == ConfigSource.Discovery) {
            Spacer(modifier = Modifier.size(8.dp))
            val renewButtonText = if (isExpired) {
                // OAuth and expired
                R.string.status_reauthenticate_my_account
            } else if (expiryTimestampMs == null) {
                // Password based login
                R.string.status_update_my_password
            } else {
                // OAuth, but not expired yet
                R.string.status_renew_my_accout
            }
            if (organizationId != null) {
                PrimaryButton(
                    buttonBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    buttonTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    text = stringResource(id = renewButtonText),
                    onClick = {
                        renewAccount(organizationId)
                    })
            }
        }
        if (lastConfig != null) {
            Spacer(modifier = Modifier.size(8.dp))
            PrimaryButton(
                buttonBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                buttonTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                text = stringResource(id = R.string.status_repair),
                onClick = {
                    repairConfig(lastConfig)
                })
        }
        Spacer(modifier = Modifier.weight(1f))
        /** TODO implement debug options
        if (!debugOptionsEnabled) {
            TextButton(
                onClick = {
                    showDebugOptions()
                }) {
                Text(
                    text = stringResource(id = R.string.status_show_debug_options),
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = TextDecoration.Underline
                )
            }
        } else {
            // TODO show username, suggestion / app intent, SSID
        }
        **/
        Spacer(modifier = Modifier.size(12.dp))
    }

}

@Composable
fun formatDuration(date1: LocalDateTime, date2: LocalDateTime): String? {
    val period = Period.between(date1.toLocalDate(), date2.toLocalDate())
    val duration = Duration.between(date1, date2)

    val days = period.days
    val months = period.months
    val years = period.years

    val hours = duration.toHours().toInt()
    val minutes = (duration.toMinutes() % 60).toInt()
    val seconds = (duration.seconds % 60).toInt()
    return if (years > 0 || months > 0) {
        null
    } else if (days > 0) {
        "$days ${pluralStringResource(id = R.plurals.time_days, days)}"
    } else if (hours > 0) {
        "$hours ${pluralStringResource(id = R.plurals.time_hours, hours)}"
    } else if (minutes > 0) {
        "$minutes ${pluralStringResource(id = R.plurals.time_minutes, minutes)}"
    } else {
        "$seconds ${pluralStringResource(id = R.plurals.time_seconds, seconds)}"
    }
}


@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun Preview_StatusScreenContent() {
    AppTheme {
        StatusScreenContent(
            paddingValues = PaddingValues(),
            debugOptionsEnabled = true,
            lastConfig = null,
            configSource = ConfigSource.Discovery,
            organizationId = "abc123",
            organizationName = "Budapest University of Technology and Economics",
            expiryTimestampMs = Date().time + 10_000,
            goToInstitutionSelection = {},
            renewAccount = {},
            showDebugOptions = {},
            repairConfig = {}
        )
    }
}