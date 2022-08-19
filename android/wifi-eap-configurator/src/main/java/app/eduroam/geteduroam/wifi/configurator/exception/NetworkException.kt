package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception indicating that something went wrong with a network.
 */
abstract class NetworkException : Exception {
    internal constructor(message: String?) : super(message) {}
    internal constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    internal constructor(cause: Throwable?) : super(cause) {}
}