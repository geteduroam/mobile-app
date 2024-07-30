package app.eduroam.geteduroam.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DiscoveryContent(
    val institutions: List<Organization>,
    val seq: Long
) : Parcelable
