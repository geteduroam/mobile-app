import Models
import SwiftUI

struct ConnectButtonStyle: ButtonStyle {
    
    @EnvironmentObject var theme: Theme
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding()
            .background(Color("Pink"))
            .foregroundColor(.white)
            .font(theme.connectButtonFont)
            .clipShape(Capsule())
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}
