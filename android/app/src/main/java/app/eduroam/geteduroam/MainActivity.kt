package app.eduroam.geteduroam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import app.eduroam.geteduroam.ui.theme.AppTheme
import app.eduroam.geteduroam.util.Oauth2
import app.eduroam.shared.injectLogger
import app.eduroam.shared.profile.SelectProfileViewModel
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    private val log: Logger by injectLogger("MainActivity")
    private val institutionViewModel: SelectInstitutionViewModel by viewModel()
    private val profileViewModel: SelectProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        intent.data?.let {
            val code = it.getQueryParameter("code")
            val institutionId = it.getQueryParameter("state")
            // get Institution by ID
            // get token url Oauth2.getTokenUrl(Institution, code)
            // select/download config
        }
        setContent {
            AppTheme {
                NavGraph(institutionViewModel, profileViewModel, log)
            }
        }
    }
}
