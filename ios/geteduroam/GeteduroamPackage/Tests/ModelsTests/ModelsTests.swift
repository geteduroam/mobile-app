import XCTest
import XMLCoder
import CustomDump
@testable import Models

final class ModelsTests: XCTestCase {
    
    func testProviderInfo() throws {
        let sourceXML = """
        <ProviderInfo>
          <DisplayName>Provider Display Name</DisplayName>
          <Description>Description</Description>
          <ProviderLocation>
            <Longitude>-12.345</Longitude>
            <Latitude>45.987</Latitude>
          </ProviderLocation>
          <ProviderLocation>
            <Longitude>-88.444</Longitude>
            <Latitude>66.333</Latitude>
          </ProviderLocation>
          <TermsOfUse>Terms Of Use</TermsOfUse>
          <Helpdesk>
            <WebAddress>https://www.example.com</WebAddress>
          </Helpdesk>
        </ProviderInfo>
        """
        
        let decoder = XMLDecoder()
        decoder.dateDecodingStrategy = .iso8601
        
        let decoded = try decoder.decode(ProviderInfo.self, from: Data(sourceXML.utf8))
        
        XCTAssertNoDifference(decoded, ProviderInfo(
            displayName: "Provider Display Name",
            description: "Description",
            providerLocations: [
                .init(latitude: 45.987, longitude: -12.345),
                .init(latitude: 66.333, longitude: -88.444)
            ],
            providerLogo: nil,
            termsOfUse: "Terms Of Use",
            helpdesk: .init(
                emailAdress: nil,
                webAddress: "https://www.example.com",
                phone: nil)))
    }
    
    func testServerCredential() throws {
        let sourceXML = """
        <ServerSideCredential>
            <CA format="X.509" encoding="base64">DEADBEEF==</CA>
            <ServerID>radius.example.com</ServerID>
        </ServerSideCredential>
        """
        
        let decoder = XMLDecoder()
        decoder.shouldProcessNamespaces = true
        decoder.dateDecodingStrategy = .iso8601
       
        let decoded = try decoder.decode(ServerCredential.self, from: Data(sourceXML.utf8))
        
        XCTAssertNoDifference(decoded, ServerCredential(
            certificates: [
                .init(value: "DEADBEEF==", format: "X.509", encoding: "base64")
            ],
            serverIDs: ["radius.example.com"]))
    }
    
    func testEntireConfig() throws {
        let sourceXML = """
        <?xml version="1.0" encoding="utf-8"?>
        <EAPIdentityProviderList xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="eap-metadata.xsd">
            <EAPIdentityProvider ID="moreelsepark.geteduroam.nl" namespace="urn:RFC4282:realm" lang="en" version="1">
                <ValidUntil>2023-11-25T08:01:26Z</ValidUntil>
                <AuthenticationMethods>
                    <AuthenticationMethod>
                        <EAPMethod>
                            <Type>13</Type>
                        </EAPMethod>
                        <ServerSideCredential>
                            <CA format="X.509" encoding="base64">DEADBEEF==</CA>
                            <ServerID>radius.example.com</ServerID>
                        </ServerSideCredential>
                        <ClientSideCredential>
                            <OuterIdentity>example@radius.example.com</OuterIdentity>
                            <ClientCertificate format="PKCS12" encoding="base64">DEADBEEF==</ClientCertificate>
                            <Passphrase>secret</Passphrase>
                        </ClientSideCredential>
                    </AuthenticationMethod>
                </AuthenticationMethods>
                <CredentialApplicability>
                    <IEEE80211>
                        <ConsortiumOID>123456</ConsortiumOID>
                    </IEEE80211>
                    <IEEE80211>
                        <ConsortiumOID>654321</ConsortiumOID>
                    </IEEE80211>
                    <IEEE80211>
                        <ConsortiumOID>000000</ConsortiumOID>
                    </IEEE80211>
                    <IEEE80211>
                        <SSID>eduroam</SSID>
                        <MinRSNProto>CCMP</MinRSNProto>
                    </IEEE80211>
                </CredentialApplicability>
                <ProviderInfo>
                    <DisplayName>eduroam</DisplayName>
                </ProviderInfo>
            </EAPIdentityProvider>
        </EAPIdentityProviderList>
        """
        
        let decoder = XMLDecoder()
        decoder.shouldProcessNamespaces = true
        decoder.dateDecodingStrategy = .iso8601
       
        let decoded = try decoder.decode(EAPIdentityProviderList.self, from: Data(sourceXML.utf8))
        
        XCTAssertNoDifference(decoded, EAPIdentityProviderList(providers: [
            .init(
                validUntil: ISO8601DateFormatter().date(from: "2023-11-25T08:01:26Z")!,
                authenticationMethods: .init(methods: [.init(
                    EAPMethod: .init(type: 13),
                    serverSideCredential: .init(certificates: [.init(value: "DEADBEEF==", format: "X.509", encoding: "base64")], serverIDs: ["radius.example.com"]),
                    clientSideCredential: .init(
                        outerIdentity: "example@radius.example.com",
                        innerIdentityPrefix: nil,
                        innerIdentitySuffix: nil,
                        innerIdentityHint: nil,
                        userName: nil,
                        password: nil,
                        clientCertificate: .init(value: "DEADBEEF==", format: "PKCS12", encoding: "base64"),
                        intermediateCACertificates: [],
                        passphrase: "secret",
                        PAC: nil,
                        provisionPAC: nil,
                        allowSave: nil),
                    innerAuthenticationMethods: [])]),
                credentialApplicability: .init(
                    IEEE80211: [
                        .init(ssid: nil, consortiumOID: "123456", minRSNProto: nil),
                        .init(ssid: nil, consortiumOID: "654321", minRSNProto: nil),
                        .init(ssid: nil, consortiumOID: "000000", minRSNProto: nil),
                        .init(ssid: "eduroam", consortiumOID: nil, minRSNProto: .CCMP)
                    ],
                    IEEE8023: []),
                providerInfo: .init(displayName: "eduroam", description: nil, providerLocations: [], providerLogo: nil, termsOfUse: nil, helpdesk: nil))
        ]))
    }
}
