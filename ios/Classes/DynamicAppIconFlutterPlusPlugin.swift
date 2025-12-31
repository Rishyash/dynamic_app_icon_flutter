import Flutter
import UIKit

@objc public class DynamicAppIconFlutterPlusPlugin: NSObject, FlutterPlugin {
  @objc public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_dynamic_icon", binaryMessenger: registrar.messenger())
    let instance = DynamicAppIconFlutterPlusPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method == "mSupportsAlternateIcons" {
      if #available(iOS 10.3, *) {
        result(UIApplication.shared.supportsAlternateIcons)
      } else {
        result(FlutterError(code: "UNAVAILABLE", message: "Not supported on iOS ver < 10.3", details: nil))
      }
    } else if call.method == "mGetAlternateIconName" {
      if #available(iOS 10.3, *) {
        result(UIApplication.shared.alternateIconName)
      } else {
        result(FlutterError(code: "UNAVAILABLE", message: "Not supported on iOS ver < 10.3", details: nil))
      }
    } else if call.method == "mSetAlternateIconName" {
      if #available(iOS 10.3, *) {
        let args = call.arguments as? [String: Any]
        let iconName = args?["iconName"] as? String
        let showAlert = args?["showAlert"] as? Bool ?? true

        if showAlert {
          UIApplication.shared.setAlternateIconName(iconName) { error in
            if let error = error {
              result(FlutterError(code: "Failed to set icon", message: error.localizedDescription, details: nil))
            } else {
              result(nil)
            }
          }
        } else {
          // Use private API to set icon without alert (use at your own risk)
          let selectorString = "_setAlternateIconName:completionHandler:"
          let selector = NSSelectorFromString(selectorString)
          if UIApplication.shared.responds(to: selector) {
            let imp = UIApplication.shared.method(for: selector)
            typealias SetAlternateIconNameFunc = @convention(c) (AnyObject, Selector, String?, @escaping (Error?) -> Void) -> Void
            let function = unsafeBitCast(imp, to: SetAlternateIconNameFunc.self)
            function(UIApplication.shared, selector, iconName) { error in
              DispatchQueue.main.async {
                if let error = error {
                  result(FlutterError(code: "Failed to set icon", message: error.localizedDescription, details: nil))
                } else {
                  result(nil)
                }
              }
            }
          } else {
            result(FlutterError(code: "Failed to set icon", message: "Private API not available", details: nil))
          }
        }
      } else {
        result(FlutterError(code: "UNAVAILABLE", message: "Not supported on iOS ver < 10.3", details: nil))
      }
    } else if call.method == "mGetAvailableIcons" {
      if #available(iOS 10.3, *) {
        var availableIcons: [String] = []
        if let infoDictionary = Bundle.main.infoDictionary,
           let bundleIcons = infoDictionary["CFBundleIcons"] as? [String: Any],
           let alternateIcons = bundleIcons["CFBundleAlternateIcons"] as? [String: Any] {
          availableIcons = Array(alternateIcons.keys)
        }
        result(availableIcons)
      } else {
        result(FlutterError(code: "UNAVAILABLE", message: "Not supported on iOS ver < 10.3", details: nil))
      }
    } else {
      result(FlutterMethodNotImplemented)
    }
  }
}
