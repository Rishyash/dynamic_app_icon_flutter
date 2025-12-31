## 0.0.7
* Package fix


## 0.0.6
* Initial Setup

## 0.0.5
* Initial Setup


## 0.0.4
* Initial Setup


## 0.0.3
* Initial Setup

## 0.0.1

* Initial release of dynamic_app_icon_flutter
* **Android Support**: Dynamic icon changing using activity aliases
  * Automatic discovery of available icons from AndroidManifest.xml
  * Support for unlimited number of alternate icons
  * Flexible icon naming (no hardcoded names)
  * Uses main activity as default icon launcher
* **iOS Support**: Dynamic icon changing using alternate app icons
  * Support for iOS 10.3+ alternate icons
  * Get available icons from Info.plist
  * Set alternate icons with optional alert suppression
* **Core Features**:
  * `supportsAlternateIcons` - Check if platform supports dynamic icons
  * `getAlternateIconName()` - Get current active icon name
  * `getAvailableIcons()` - Get list of all available alternate icons
  * `setAlternateIconName()` - Set the app icon dynamically
* **Example App**: Complete UI demonstration with Material Design 3
  * Platform support status indicator
  * Current icon display
  * Available icons list with selection
  * Clean, modern interface suitable for pub.dev
