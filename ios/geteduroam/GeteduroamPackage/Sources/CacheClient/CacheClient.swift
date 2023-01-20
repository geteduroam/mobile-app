import Models
import Foundation

public struct CacheClient {
    public var cacheInstitutions: (InstitutionsResponse) -> Void
    public var restoreInstitutions: () throws -> InstitutionsResponse
    
    static func cacheURLForInstitutions() throws -> URL {
        guard let directoryURL = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).last else {
            throw CacheClientError.noDocumentsFolder
        }
        let cacheURL = directoryURL.appendingPathComponent("institutions.json")
        return cacheURL
    }
}

import Dependencies

extension DependencyValues {
    public var cacheClient: CacheClient {
        get { self[CacheClientKey.self] }
        set { self[CacheClientKey.self] = newValue }
    }
    
    public enum CacheClientKey: TestDependencyKey {
        public static var testValue = CacheClient.mock
    }
}

extension CacheClient {
    static var mock: Self = .init(
        cacheInstitutions: { _ in
            print("Should now create cache")
        },
        restoreInstitutions: {
            print("Should now restore cache")
            return InstitutionsResponse(instances: [.init(id: "restored", name: "Restored Institution from Cache", country: "NL", cat_idp: 0, profiles: [], geo: [])])
        })
}

extension DependencyValues.CacheClientKey: DependencyKey {
    public static var liveValue = CacheClient.live
}

extension CacheClient {
    static var live: Self = .init(
        cacheInstitutions: { institutions in
            guard let data = try? JSONEncoder().encode(institutions), let cacheURL = try? Self.cacheURLForInstitutions() else {
                return
            }
            try? data.write(to: cacheURL)
            print("Cached to \(cacheURL)")
        },
        restoreInstitutions: {
            do {
                let cacheURL = try Self.cacheURLForInstitutions()
                let data = try Data(contentsOf: cacheURL)
                let institutions = try JSONDecoder().decode(InstitutionsResponse.self, from: data)
                print("Restored from \(cacheURL)")
                return institutions
            } catch {
                print("No cache found, using bundled cache")
                guard let cacheURL = Bundle.main.url(forResource: "institutions", withExtension: "json") else {
                    throw CacheClientError.noCacheInBundle
                }
                let data = try Data(contentsOf: cacheURL)
                print("Restored from \(cacheURL)")
                let institutions = try JSONDecoder().decode(InstitutionsResponse.self, from: data)
                return institutions
            }
        })
}

enum CacheClientError: Error {
    case noDocumentsFolder
    case noCacheInBundle
}
