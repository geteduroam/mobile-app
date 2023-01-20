import Models
import SwiftUI

public struct InstitutionRowView: View {
    public init(institution: Institution) {
        self.institution = institution
    }
    
    let institution: Institution
    
    @EnvironmentObject var theme: Theme
    
    public var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading) {
                Text(institution.name)
                    .font(theme.institutionNameFont)
                Text(institution.country)
                    .font(theme.institutionCountryFont)
            }
            Spacer()
            Image(systemName: "chevron.right")
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
