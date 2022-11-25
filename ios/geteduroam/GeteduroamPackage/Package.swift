// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "GeteduroamPackage",
    platforms: [.iOS(.v14), .macCatalyst(.v14)],
    products: [
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "Main",
            targets: ["Main"]),
        .library(
            name: "AuthClient",
            targets: ["AuthClient"]),
        .library(
            name: "Backport",
            targets: ["Backport"]),
        .library(
            name: "DiscoveryClient",
            targets: ["DiscoveryClient"]),
        .library(
            name: "EAPConfigParser",
            targets: ["EAPConfigParser"]),
        .library(
            name: "Connect",
            targets: ["Connect"]),
        .library(
            name: "Models",
            targets: ["Models"]),
        .library(
            name: "EAPConfigurator",
            targets: ["EAPConfigurator"]),
    ],
    dependencies: [
        .package(url: "https://github.com/apple/swift-algorithms.git", from: "1.0.0"),
        .package(url: "https://github.com/cezheng/Fuzi", from: "3.0.0"),
        .package(url: "https://github.com/openid/AppAuth-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "0.42.0"),
        .package(url: "https://github.com/pointfreeco/swift-url-routing", from: "0.0.0"),
        .package(url: "https://github.com/pointfreeco/xctest-dynamic-overlay", from: "0.0.0"),
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .target(
            name: "Main",
            dependencies: [
                "AuthClient",
                "Backport",
                "DiscoveryClient",
                "Connect",
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture")
            ]),
        .testTarget(
            name: "MainTests",
            dependencies: ["Main"]),
        .target(
            name: "AuthClient",
            dependencies: [
                .product(name: "AppAuth", package: "AppAuth-iOS"),
                .product(name: "Dependencies", package: "swift-composable-architecture"),
                .product(name: "XCTestDynamicOverlay", package: "xctest-dynamic-overlay")
            ]),
        .target(
            name: "Backport",
            dependencies: []),
        .target(
            name: "DiscoveryClient",
            dependencies: [
                "Models",
                .product(name: "Dependencies", package: "swift-composable-architecture"),
                .product(name: "URLRouting", package: "swift-url-routing")
            ]),
        .target(
            name: "EAPConfigParser",
            dependencies: [
                "Models",
                .product(name: "Algorithms", package: "swift-algorithms"),
                .product(name: "Fuzi", package: "Fuzi")
            ]),
        .target(
            name: "Connect",
            dependencies: [
                "AuthClient",
                "EAPConfigParser",
                "EAPConfigurator",
                "Models",
                .product(name: "AppAuth", package: "AppAuth-iOS"),
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture")
            ]),
        .target(
            name: "Models"),
        .target(
            name: "EAPConfigurator",
            dependencies: [
                "Models",
            ]),
    ]
)
