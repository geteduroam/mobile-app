package app.eduroam.geteduroam

import android.app.Application
import android.content.Context
import app.eduroam.geteduroam.util.CrashlyticsTree
import app.eduroam.shared.AppInfo
import app.eduroam.shared.BuildConfig
import app.eduroam.shared.initKoin
import app.eduroam.shared.select.SelectInstitutionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import timber.log.Timber

class AndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                single<Context> { this@AndroidApp }
                viewModel { SelectInstitutionViewModel(get(), get { parametersOf("SelectInstitutionViewModel") }) }
                single<AppInfo> { AndroidAppInfo }
                single {
                    { Timber.tag("Startup").i("Hello from Android/Kotlin!") }
                }
            }
        )

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}


object AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.BUILD_TYPE
}
