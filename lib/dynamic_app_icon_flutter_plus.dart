import 'package:flutter/services.dart';

class DynamicAppIconFlutterPlus {
  /// The [MethodChannel] used by this plugin
  static const MethodChannel _channel = MethodChannel('flutter_dynamic_icon');

  /// Indicates whether the current platform supports dynamic app icons
  static Future<bool> get supportsAlternateIcons async {
    final bool supportsAltIcons = await _channel.invokeMethod(
      'mSupportsAlternateIcons',
    );
    return supportsAltIcons;
  }

  /// Fetches the current iconName
  ///
  /// Returns `null` if the current icon is the default icon
  static Future<String?> getAlternateIconName() async {
    final String? altIconName = await _channel.invokeMethod(
      'mGetAlternateIconName',
    );
    return altIconName;
  }

  /// Gets a list of all available alternate icon names
  ///
  /// Returns a list of icon names that can be used with [setAlternateIconName]
  /// On Android, this dynamically discovers all activity aliases configured in the manifest
  static Future<List<String>> getAvailableIcons() async {
    final List<dynamic> icons = await _channel.invokeMethod(
      'mGetAvailableIcons',
    );
    return icons.cast<String>();
  }

  /// Sets [iconName] as the current icon for the app
  ///
  /// [iOS]: Use [showAlert] at your own risk as it uses a private/undocumented API to
  /// not show the icon change alert. By default, it shows the alert
  /// Reference: https://stackoverflow.com/questions/43356570/alternate-icon-in-ios-10-3-avoid-notification-dialog-for-icon-change/49730130#49730130
  ///
  /// Throws a [PlatformException] with description if
  /// it can't find [iconName] or there's any other error
  static Future setAlternateIconName(
    String? iconName, {
    bool showAlert = true,
  }) async {
    await _channel.invokeMethod('mSetAlternateIconName', <String, dynamic>{
      'iconName': iconName,
      'showAlert': showAlert,
    });
  }
}
