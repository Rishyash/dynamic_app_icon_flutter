// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "dynamic_app_icon_flutter_plus",
    platforms: [
        .iOS("13.0"),
    ],
    products: [
        .library(name: "dynamic-app-icon-flutter-plus", targets: ["dynamic_app_icon_flutter_plus"]),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "dynamic_app_icon_flutter_plus",
            dependencies: [],
            path: "Classes"
        ),
    ]
)
