import Foundation

public struct AccessPoint {
    public init(id: String, domain: String, ssids: [String], oids: [String], outerIdentity: String?, serverNames: [String], outerEapTypes: [Int], innerAuthType: Int? = nil, clientCertificate: String? = nil, passphrase: String? = nil, username: String? = nil, password: String? = nil, caCertificates: [String]) {
        self.id = id
        self.domain = domain
        self.ssids = ssids
        self.oids = oids
        self.outerIdentity = outerIdentity
        self.serverNames = serverNames
        self.outerEapTypes = outerEapTypes
        self.innerAuthType = innerAuthType
        self.clientCertificate = clientCertificate
        self.passphrase = passphrase
        self.username = username
        self.password = password
        self.caCertificates = caCertificates
    }
    
    public let id: String
    public let domain: String
    public let ssids: [String]
    public let oids: [String]
    public let outerIdentity: String?
    public let serverNames: [String]
    public let outerEapTypes: [Int]
    public let innerAuthType:Int?
    public let clientCertificate: String?
    public let passphrase: String?
    public let username: String?
    public let password: String?
    public let caCertificates: [String]
}
