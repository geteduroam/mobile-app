package app.eduroam.geteduroam

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.eduroam.geteduroam.welcome.SelectInstitutionScreen
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger

@Composable
fun NavGraph(viewModel: SelectInstitutionViewModel, log: Logger) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SelectInstitution
    ) {
        composable(SelectInstitution) { SelectInstitutionScreen(viewModel, log) }
    }
}

private const val SelectInstitution = "selectInstitution"
