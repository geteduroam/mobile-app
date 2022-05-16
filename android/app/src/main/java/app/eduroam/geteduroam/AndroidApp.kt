package app.eduroam.geteduroam

import android.app.Application
import app.eduroam.shared.BuildConfig
import com.fin.sisaa.android.util.CrashlyticsTree
import timber.log.Timber

class AndroidApp : Application() {

	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		} else {
			Timber.plant(CrashlyticsTree())
		}
	}
}
