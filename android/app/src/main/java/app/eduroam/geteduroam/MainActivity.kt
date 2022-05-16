package app.eduroam.geteduroam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import app.eduroam.geteduroam.ui.theme.AppTheme
import app.eduroam.shared.Greeting

fun greet(): String {
	return Greeting().greeting()
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent {
			AppTheme {
				NavGraph()
			}
		}
	}
}
