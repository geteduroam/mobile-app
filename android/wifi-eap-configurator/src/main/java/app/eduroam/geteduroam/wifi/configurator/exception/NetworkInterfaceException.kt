package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception to indicate an issue with a network interface, such as a configuration, albeit valid,
 * not being supported - this may happen with Passpoint.  Another example is not being able to
 * enable the interface.
 */
class NetworkInterfaceException : NetworkException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}