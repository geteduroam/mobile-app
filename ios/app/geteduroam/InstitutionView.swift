import core
import SwiftUI

struct InstitutionView: View {
    var institution: Institution

    var body: some View {
        Text(institution.name)
            .frame(width: 200, height: 200)
            .navigationTitle(institution.name)
    }
}

//struct InstitutionView_Previews: PreviewProvider {
//    static var previews: some View {
//        InstitutionView(institution: .init(cat_idp: 1, country: "NL", id: "123", name: "Egeniq", profiles: [.init(eapconfig_endpoint: nil, id: "321", name: "Profile 1", oauth: true, authorization_endpoint: nil, token_endpoint: nil)]))
//    }
//}
