package app.eduroam.geteduroam.util

import app.eduroam.shared.response.Institution
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.pkce.PKCE
import com.github.scribejava.core.pkce.PKCEService
import io.ktor.http.*


object Oauth2 {
    private const val APPLICATION_ID = "app.eduroam.geteduroam"

    private val clientId = APPLICATION_ID.encodeURLParameter()
    private val redirectUrl = "$APPLICATION_ID://".encodeURLParameter()

    private lateinit var pkce: PKCE

    fun getAuthorizationUrl(institution: Institution):String {
        // each auth flow has a codeChallenge/codeVerifier pair
        pkce = PKCEService.defaultInstance().generatePKCE()
        val builder = StringBuilder()

        builder.append(institution.profiles[0].authorization_endpoint)
        builder.append("?response_type=code")
        builder.append("&code_challenge_method=S256")
        builder.append("&scope=eap-metadata")
        builder.append("&code_challenge=${pkce.codeChallenge}")
        builder.append("&redirect_uri=$redirectUrl")
        builder.append("&client_id=$clientId")
        builder.append("&state="+institution.id)

        return builder.toString()
    }

    fun getTokenUrl(institution: Institution, code:String):String {
        val builder = StringBuilder()

        builder.append(institution.profiles[0].token_endpoint)
        builder.append("?grant_type=code")
        builder.append("&authorization_code=$code")
        builder.append("&redirect_uri=$redirectUrl")
        builder.append("&client_id=$clientId")
        builder.append("&code_verifier=${pkce.codeVerifier}")

        return builder.toString()
    }
}