import SwiftUI

struct ConnectButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding()
            .background(Color("Pink"))
            .foregroundColor(.white)
            .font(Font.custom("OpenSans-Bold", size: 20, relativeTo: .body))
            .clipShape(Capsule())
            .scaleEffect(configuration.isPressed ? 0.95 : 1)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}
