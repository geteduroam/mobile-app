// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "GeteduroamPackage",
    platforms: [.macOS(.v11), .iOS(.v14)],
    products: [
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "Main",
            targets: ["Main"]),
        .library(
            name: "DiscoveryClient",
            targets: ["DiscoveryClient"]),
        .library(
            name: "EAPConfig",
            targets: ["EAPConfig"]),
        .library(
            name: "Institution",
            targets: ["Institution"]),
        .library(
            name: "WiFiEAPConfigurator",
            targets: ["WiFiEAPConfigurator"]),
    ],
    dependencies: [
        .package(url: "https://github.com/apple/swift-algorithms.git", from: "1.0.0"),
        .package(url: "https://github.com/cezheng/Fuzi", from: "3.0.0"),
        .package(url: "https://github.com/openid/AppAuth-iOS.git", from: "1.0.0"),
        .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "0.42.0"),
        .package(url: "https://github.com/pointfreeco/swift-url-routing", from: "0.0.0"),
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .target(
            name: "Main",
            dependencies: [
                "DiscoveryClient",
                .product(name: "AppAuth", package: "AppAuth-iOS"),
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture")
            ]),
        .testTarget(
            name: "MainTests",
            dependencies: ["Main"]),
        .target(
            name: "DiscoveryClient",
            dependencies: [
                .product(name: "URLRouting", package: "swift-url-routing")
            ]),
        .target(
            name: "EAPConfig",
            dependencies: [
                .product(name: "Algorithms", package: "swift-algorithms"),
                .product(name: "Fuzi", package: "Fuzig")
            ]),
        .target(
            name: "Institution",
            dependencies: [
                "DiscoveryClient"
            ]),
        .target(
            name: "WiFiEAPConfigurator",
            dependencies: []),
    ]
)
