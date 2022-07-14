package app.eduroam.geteduroam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import app.eduroam.geteduroam.ui.theme.AppTheme
import app.eduroam.shared.injectLogger
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    private val log: Logger by injectLogger("MainActivity")
    private val viewModel: SelectInstitutionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme {
                NavGraph(viewModel, log)
            }
        }
    }
}
