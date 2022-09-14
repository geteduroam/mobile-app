package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception indicating an error in the parsed eap-config profile
 *
 * This can either be due to a problem in the eap-config file, or due to the parser in ionic.
 */
open class EapConfigException : Exception {
    internal constructor(message: String?) : super(message) {}
    internal constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    internal constructor(cause: Throwable?) : super(cause) {}
}