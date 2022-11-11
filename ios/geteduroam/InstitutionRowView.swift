import SwiftUI

struct InstitutionRowView: View {
    var institution: Institution
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(institution.name)
            Text(institution.country)
                .font(.footnote)
        }
    }
}

struct InstitutionRowView_Previews: PreviewProvider {
    static var previews: some View {
        List {
            InstitutionRowView(institution: .init(id: "1", name: "My Institution", country: "NL", cat_idp: 1, profiles: [.init(id: "2", name: "My Profile", default: true, eapconfig_endpoint: nil, oauth: false, authorization_endpoint: nil, token_endpoint: nil)], geo: [.init(lat: 0, lon: 0)]))
        }
    }
}
