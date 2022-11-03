import core
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

//struct InstitutionRowView_Previews: PreviewProvider {
//    static var previews: some View {
//        InstitutionRowView()
//    }
//}
