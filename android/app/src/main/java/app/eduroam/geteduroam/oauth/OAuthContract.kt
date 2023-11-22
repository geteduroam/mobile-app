package app.eduroam.geteduroam.oauth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract


class OAuthContract : ActivityResultContract<Intent, Intent?>() {
    override fun createIntent(context: Context, input: Intent): Intent = input
    override fun parseResult(resultCode: Int, intent: Intent?): Intent? = intent
}