package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception to indicate an error with a NetworkConfiguration using API 28
 *
 * This might indicate something wrong with the configuration, something in the device blocking
 * the creation of the network or a bug in the code where the API was called from a build target
 * higher than API 28, where this specific API is no longer available.
 */
class NetworkConfigurationException : NetworkException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}