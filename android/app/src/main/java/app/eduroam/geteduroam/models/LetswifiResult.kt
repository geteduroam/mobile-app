package app.eduroam.geteduroam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LetswifiResult(
    @SerialName("http://letswifi.app/api#v2")
    val content: LetswifiConfig
) : Parcelable
