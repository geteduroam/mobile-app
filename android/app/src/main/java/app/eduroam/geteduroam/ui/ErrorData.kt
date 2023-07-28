package app.eduroam.geteduroam.ui

import android.content.Context
import androidx.annotation.StringRes

data class ErrorData(
    val title: String = "",
    val message: String = "",
    @StringRes private val titleId: Int = 0,
    @StringRes private val messageId: Int = 0,
    private val messageArg: String? = null,
) {
    fun title(context: Context): String = title.ifEmpty { context.getString(titleId) }
    fun message(context: Context): String =
        message.ifEmpty {
            if (messageArg == null) {
                context.getString(messageId)
            } else {
                context.getString(messageId, messageArg)
            }

        }

}