import UserNotifications
import Foundation

/* TODO: Move this seemingly unrelated code
@objc func sendNotification(date: String, title: String, message: String) {
        let notifCenter = UNUserNotificationCenter.current()
        notifCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            UserDefaults.standard.set(date, forKey: "date")
            UserDefaults.standard.set(title, forKey: "title")
            UserDefaults.standard.set(message, forKey: "message")

            let content = UNMutableNotificationContent()
            content.title = title
            content.body = message
            content.sound = UNNotificationSound.default
            content.badge = 1
    
            let realDate = Int(date)! - 432000000
            let date = Date(timeIntervalSince1970: Double((realDate) / 1000))
            //let triggerDate = Calendar.current.dateComponents([.year,.month,.day,.hour,.minute,.second,], from: date)

            if date.timeIntervalSinceNow > 0 {
                let trigger = UNTimeIntervalNotificationTrigger.init(timeInterval: date.timeIntervalSinceNow, repeats: false)

                let request = UNNotificationRequest.init(identifier: "getEduroamApp", content: content, trigger: trigger)

                let center = UNUserNotificationCenter.current()
                center.add(request)
            }
        }
}

public func writeToSharedPref(institutionId: String) {
    UserDefaults.standard.set(institutionId, forKey: "institutionId")
}

public func readFromSharedPref() -> String {
     return UserDefaults.standard.string(forKey: "institutionId") ?? ""
}

public func checkIfOpenThroughNotifications() -> Bool {
    return UserDefaults.standard.bool(forKey: "initFromNotification")
}
 */
