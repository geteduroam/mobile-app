package app.eduroam.geteduroam.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class LetswifiResult(
    @Json(name = "http://letswifi.app/api#v2")
    val content: LetswifiConfig
) : Parcelable
