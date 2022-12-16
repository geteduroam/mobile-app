import Foundation
import NetworkExtension
import SystemConfiguration.CaptiveNetwork

public class SSID {
    public class func fetchNetworkInfo() -> [NetworkInfo]? {
        if let interfaces: NSArray = CNCopySupportedInterfaces() {
            var networkInfos = [NetworkInfo]()
            for interface in interfaces {
                let interfaceName = interface as! String
                var networkInfo = NetworkInfo(
                    interface: interfaceName,
                    success: false,
                    ssid: nil,
                    bssid: nil
                )
                if let dict = CNCopyCurrentNetworkInfo(interfaceName as CFString) as NSDictionary? {
                    networkInfo.success = true
                    networkInfo.ssid = dict[kCNNetworkInfoKeySSID as String] as? String
                    networkInfo.bssid = dict[kCNNetworkInfoKeyBSSID as String] as? String
                }
                networkInfos.append(networkInfo)
            }
            return networkInfos
        }
        return nil
    }
    
}

public struct NetworkInfo {
    public var interface: String
    public var success: Bool
    public var ssid: String?
    public var bssid: String?
}
