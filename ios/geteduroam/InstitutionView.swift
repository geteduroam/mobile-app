import SwiftUI

struct InstitutionView: View {
    var institution: Institution

    var body: some View {
        Text(institution.name)
            .frame(width: 200, height: 200)
            .navigationTitle(institution.name)
    }
}

struct InstitutionView_Previews: PreviewProvider {
    static var previews: some View {
        InstitutionView(institution: .init(id: "1", name: "My Institution", country: "NL", cat_idp: 1, profiles: [.init(id: "2", name: "My Profile", default: true, eapconfig_endpoint: nil, oauth: false, authorization_endpoint: nil, token_endpoint: nil)], geo: [.init(lat: 0, lon: 0)]))
    }
}
