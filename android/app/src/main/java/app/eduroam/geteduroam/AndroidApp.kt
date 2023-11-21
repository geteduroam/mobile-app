package app.eduroam.geteduroam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG || true) {
            Timber.plant(Timber.DebugTree())
        }
    }
}