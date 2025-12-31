# dynamic_app_icon_flutter_plus

A Flutter plugin for dynamically changing app icons on Android and iOS.

## Features 🌟

- 🎨 **Android Support**: Change app icons using activity aliases
- 🍎 **iOS Support**: Use alternate app icons (iOS 10.3+)
- 🔍 **Auto-Discovery**: Automatically finds all available icons from configuration
- 🚀 **No Hardcoding**: Fully dynamic, client-driven configuration
- 📱 **Cross-Platform**: Works seamlessly on both Android and iOS

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  dynamic_app_icon_flutter_plus: ^0.0.1
```

Then run:

```bash
flutter pub get
```

## Quick Start 🚀

Let's get you started with dynamic app icons in no time!

```dart
import 'package:dynamic_app_icon_flutter_plus/dynamic_app_icon_flutter_plus.dart';

// Check if dynamic icons are supported on this platform
bool supported = await DynamicAppIconFlutterPlus.supportsAlternateIcons;

// Get all available icons
List<String> icons = await DynamicAppIconFlutterPlus.getAvailableIcons();

// Get the currently active icon
String? currentIcon = await DynamicAppIconFlutterPlus.getAlternateIconName();

// Change to a different icon
await DynamicAppIconFlutterPlus.setAlternateIconName('dark');

// Restore default icon
await DynamicAppIconFlutterPlus.setAlternateIconName(null);
```

That's it! You're ready to change app icons dynamically. 🎉

## API Reference 📚

### `supportsAlternateIcons`

Check if the current platform supports dynamic app icons.

**Returns:** `Future<bool>` - `true` if supported, `false` otherwise

**Example:**
```dart
bool supported = await DynamicAppIconFlutterPlus.supportsAlternateIcons;
if (supported) {
  print('Dynamic icons are supported!');
} else {
  print('Dynamic icons not supported on this platform');
}
```

**Platform Support:**
- ✅ Android: Always returns `true`
- ✅ iOS: Returns `true` on iOS 10.3+, `false` otherwise

---

### `getAlternateIconName()`

Get the name of the currently active alternate icon.

**Returns:** `Future<String?>` - The icon name if an alternate icon is active, `null` if using the default icon

**Example:**
```dart
String? currentIcon = await DynamicAppIconFlutterPlus.getAlternateIconName();
if (currentIcon == null) {
  print('Using default icon');
} else {
  print('Current icon: $currentIcon');
}
```

**Note:** Returns `null` when the default app icon is active.

---

### `getAvailableIcons()`

Get a list of all available alternate icon names that can be used.

**Returns:** `Future<List<String>>` - List of available icon names

**Example:**
```dart
List<String> availableIcons = await DynamicAppIconFlutterPlus.getAvailableIcons();
print('Available icons: $availableIcons');
// Output: Available icons: [dark, light, holiday, summer]
```

**Platform Behavior:**
- **Android**: Dynamically discovers all activity aliases from `AndroidManifest.xml` that follow the pattern `package.MainActivity.iconName`
- **iOS**: Reads icon names from `Info.plist` under `CFBundleAlternateIcons`

**Note:** The "default" alias is automatically excluded from this list on Android.

---

### `setAlternateIconName(iconName, {showAlert})`

Set the app icon to the specified alternate icon, or restore the default icon.

**Parameters:**
- `iconName` (`String?`): The name of the icon to set, or `null` to restore the default icon
- `showAlert` (`bool`, optional): iOS only - Whether to show the system alert when changing icons. Defaults to `true`.

**Returns:** `Future<void>`

**Throws:** `PlatformException` if the icon name doesn't exist or there's an error

**Example:**
```dart
// Set an alternate icon
await DynamicAppIconFlutterPlus.setAlternateIconName('dark');

// Set icon without alert on iOS (use at your own risk)
await DynamicAppIconFlutterPlus.setAlternateIconName('light', showAlert: false);

// Restore default icon
await DynamicAppIconFlutterPlus.setAlternateIconName(null);
```

**Platform Behavior:**
- **Android**: Enables the corresponding activity alias and disables others
- **iOS**: Changes the app icon using `UIApplication.setAlternateIconName()`

**iOS Alert Suppression:**
The `showAlert` parameter uses a private API on iOS to suppress the system alert. Use this at your own risk as it may break in future iOS versions.

---

## Platform Setup 🔧

### Android Setup

#### 1. Configure AndroidManifest.xml

Add activity aliases for each alternate icon. The plugin automatically discovers all aliases following the pattern: `package.MainActivity.iconName`

**Important:** The main activity must have `exported="true"` and a launcher intent-filter to serve as the default icon.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="Your App Name"
        android:icon="@mipmap/ic_launcher">
        
        <!-- Main Activity - Used as default icon launcher -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/LaunchTheme">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        
        <!-- Alternate icon 1 -->
        <activity-alias
            android:name="com.example.yourapp.MainActivity.dark"
            android:targetActivity=".MainActivity"
            android:exported="true"
            android:icon="@mipmap/ic_launcher_dark"
            android:label="@string/app_name"
            android:enabled="false">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity-alias>
        
        <!-- Alternate icon 2 - Add as many as you need -->
        <activity-alias
            android:name="com.example.yourapp.MainActivity.light"
            android:targetActivity=".MainActivity"
            android:exported="true"
            android:icon="@mipmap/ic_launcher_light"
            android:label="@string/app_name"
            android:enabled="false">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity-alias>
    </application>
</manifest>
```

#### 2. Create Icon Resources

For each alternate icon, create icon files in all density folders:

- `mipmap-mdpi/ic_launcher_dark.png`
- `mipmap-hdpi/ic_launcher_dark.png`
- `mipmap-xhdpi/ic_launcher_dark.png`
- `mipmap-xxhdpi/ic_launcher_dark.png`
- `mipmap-xxxhdpi/ic_launcher_dark.png`

**Note:** Replace `dark` with your icon name and ensure all density variants are provided.

---

### iOS Setup

#### Configure Info.plist

Add alternate icons configuration to your `Info.plist`. **Important:** You must include both the alternate icons (`CFBundleAlternateIcons`) and the default/primary icon (`CFBundlePrimaryIcon`) configuration:

```xml
	<key>CFBundleIcons</key>
<dict>
<key>CFBundleAlternateIcons</key>
<dict>
  <key>dark</key>
  <dict>
    <key>CFBundleIconFiles</key>
    <array>
      <string>dark</string>
    </array>
    <key>UIPrerenderedIcon</key>
    <false/>
  </dict>
  <key>light</key>
  <dict>
    <key>CFBundleIconFiles</key>
    <array>
      <string>light</string>
    </array>
    <key>UIPrerenderedIcon</key>
    <false/>
  </dict>
</dict>
<key>CFBundlePrimaryIcon</key>
<dict>
  <key>CFBundleIconFiles</key>
  <array>
    <string>default</string>
  </array>
  <key>UIPrerenderedIcon</key>
  <false/>
</dict>
</dict>
<key>CFBundleIcons~ipad</key>
<dict>
<key>CFBundleAlternateIcons</key>
<dict>
  <key>dark</key>
  <dict>
    <key>CFBundleIconFiles</key>
    <array>
      <string>dark</string>
    </array>
    <key>UIPrerenderedIcon</key>
    <false/>
  </dict>
  <key>light</key>
  <dict>
    <key>CFBundleIconFiles</key>
    <array>
      <string>light</string>
    </array>
    <key>UIPrerenderedIcon</key>
    <false/>
  </dict>
</dict>
<key>CFBundlePrimaryIcon</key>
<dict>
  <key>CFBundleIconFiles</key>
  <array>
    <string>default</string>
  </array>
  <key>UIPrerenderedIcon</key>
  <false/>
</dict>
</dict>
```

**Note:** The `CFBundlePrimaryIcon` configuration for the default icon is **required**. Make sure to include it in both `CFBundleIcons` (for iPhone) and `CFBundleIcons~ipad` (for iPad) sections. The default icon name should match the icon files you place in the `App Icon` folder (e.g., `default@2x.png` and `default@3x.png`).

#### Add Icon Assets

1. Open your project in Xcode
2. Create an `App Icon` folder in your `Runner` directory (e.g., `ios/Runner/App Icon/`)
3. Add icon image files for each icon (including the **required** default icon) with the following naming convention:
   - For each icon name (e.g., `dark`, `light`, `default`), add:
     - `{iconName}@2x.png` (120x120 pixels for iPhone)
     - `{iconName}@3x.png` (180x180 pixels for iPhone)
   
   **Important:** You must include the default icon files. Example files:
   - `default@2x.png` (required - for default/primary icon)
   - `default@3x.png` (required - for default/primary icon)
   - `dark@2x.png` (for alternate icon)
   - `dark@3x.png` (for alternate icon)
   - `light@2x.png` (for alternate icon)
   - `light@3x.png` (for alternate icon)

4. Ensure the icon names in your files match the keys used in `Info.plist` (e.g., if you use `"dark"` in Info.plist, name your files `dark@2x.png` and `dark@3x.png`)

**Requirements:**
- iOS 10.3 or later
- Icon files must be placed in the `App Icon` folder within your Runner directory
- Provide both @2x and @3x versions for each alternate icon

---

## Complete Example 💡

Here's a complete example showing how to build an icon selector:

```dart
import 'package:flutter/material.dart';
import 'package:dynamic_app_icon_flutter_plus/dynamic_app_icon_flutter_plus.dart';

class IconSelector extends StatefulWidget {
  @override
  _IconSelectorState createState() => _IconSelectorState();
}

class _IconSelectorState extends State<IconSelector> {
  List<String> availableIcons = [];
  String? currentIcon;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadIcons();
  }

  Future<void> _loadIcons() async {
    final icons = await DynamicAppIconFlutterPlus.getAvailableIcons();
    final current = await DynamicAppIconFlutterPlus.getAlternateIconName();
    setState(() {
      availableIcons = icons;
      currentIcon = current;
      isLoading = false;
    });
  }

  Future<void> _setIcon(String? iconName) async {
    try {
      await DynamicAppIconFlutterPlus.setAlternateIconName(iconName);
      await _loadIcons();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Icon changed successfully!')),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Center(child: CircularProgressIndicator());
    }

    return ListView(
      children: [
        // Default icon option
        ListTile(
          title: Text('Default'),
          trailing: currentIcon == null ? Icon(Icons.check) : null,
          onTap: () => _setIcon(null),
        ),
        // Alternate icons
        ...availableIcons.map((iconName) => ListTile(
          title: Text(iconName),
          trailing: currentIcon == iconName ? Icon(Icons.check) : null,
          onTap: () => _setIcon(iconName),
        )),
      ],
    );
  }
}
```

For a complete example with a beautiful UI, check out the [example](example) directory.

---

## How It Works 🔍

### Android

The plugin uses Android's activity aliases feature:
1. Each alternate icon is configured as an `activity-alias` targeting the main activity
2. The plugin dynamically discovers all aliases from `AndroidManifest.xml`
3. When changing icons, it enables the target alias and disables others
4. The main activity serves as the default icon launcher

### iOS

The plugin uses iOS's alternate app icons feature:
1. Alternate icons are configured in `Info.plist` under `CFBundleAlternateIcons`
2. The plugin reads available icons from the Info.plist
3. Uses `UIApplication.setAlternateIconName()` to change icons
4. Returns `null` when the default icon is active

---

## Notes ⚠️

- **Android**: After changing the icon, users may need to go to the home screen to see the change
- **Android**: The icon change happens immediately
- **iOS**: Changing icons may show a system alert (can be suppressed with `showAlert: false`)
- **iOS**: Requires iOS 10.3 or later
- All activity aliases (Android) must target the same MainActivity
- Only one icon can be active at a time

---

## License

MIT License - see [LICENSE](LICENSE) file for details.
