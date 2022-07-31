package app.eduroam.shared.response

import kotlinx.serialization.Serializable

//with authorization
//authorization_endpoint	"https://shlonderwijs.get…roam.nl/oauth/authorize/"
//default	true
//eapconfig_endpoint	"https://shlonderwijs.geteduroam.nl/api/eap-config/"
//id	"letswifi_cat_8149"
//name	"geteduroam"
//oauth	true
//token_endpoint	"https://shlonderwijs.geteduroam.nl/oauth/token/"

//no token
//
//eapconfig_endpoint	"https://cat.eduroam.org/user/API.php?action=downloadInstaller&device=eap-generic&profile=7347"
//id	"cat_7347"
//name	"(UFV) Universidad Francisco de Vitoria"
//oauth	false
@Serializable
data class Profile(
    val eapconfig_endpoint: String? = null,
    val id: String,
    val name: String,
    val oauth: Boolean = false,
    val authorization_endpoint: String? = null,
    val token_endpoint: String? = null,
)