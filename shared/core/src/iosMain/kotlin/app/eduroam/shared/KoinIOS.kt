package app.eduroam.shared

import app.eduroam.shared.models.SelectInstitutionCallbackViewModel
import app.eduroam.shared.storage.DriverFactory
import co.touchlab.kermit.Logger
import io.ktor.client.engine.darwin.*
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

fun initKoinIos(
    appInfo: AppInfo,
    doOnStartup: () -> Unit
): KoinApplication = initKoin(
    module {
        single { appInfo }
        single { doOnStartup }
    }
)

actual val platformModule = module {

    single { Darwin.create() }
    single { DriverFactory() }

    single {
        SelectInstitutionCallbackViewModel(
            get(),
            getWith("SelectInstitutionCallbackViewModel")
        )
    }
}

// Access from Swift to create a logger
@Suppress("unused")
fun Koin.loggerWithTag(tag: String) =
    get<Logger>(qualifier = null) { parametersOf(tag) }

@Suppress("unused") // Called from Swift
object KotlinDependencies : KoinComponent {
    fun getSelectInstitutionViewModel() = getKoin().get<SelectInstitutionCallbackViewModel>()
}