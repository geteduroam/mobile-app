package app.eduroam.geteduroam.oauth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.eduroam.geteduroam.EduTopAppBar
import app.eduroam.geteduroam.R
import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.response.Profile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun OpenIdOAuthScreen(
    url: String,
    institutionId: String,
    profile: Profile,
    viewModel: OAuthViewModel,
    goToConfigScreen: (WifiConfigData) -> Unit,
) {

    Scaffold(topBar = {
        EduTopAppBar(stringResource(R.string.name))
    }) {
        var step by remember {
            mutableStateOf("Unknown")
        }

        val launcher = rememberLauncherForActivityResult(
            contract = viewModel.generateContract(url, profile, institutionId)
        ) {
        }
        val configData by viewModel.configData.collectAsStateWithLifecycle(null)
        configData?.let { wifiConfigData ->
            LaunchedEffect(wifiConfigData) {
                viewModel.clearWifiConfigData()
                goToConfigScreen(wifiConfigData)
            }
        }

        LaunchedEffect(true) {
            launcher.launch(institutionId)
            step = "Launched OAuth"
        }
        Text(
            text = step,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}