package com.application.dynamic_app_icon_flutter_plus;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
  private Context context;
  private static final String MAIN_ACTIVITY_SUFFIX = ".MainActivity";
  private static final String DEFAULT_ALIAS_SUFFIX = ".default";

  MethodCallHandlerImpl(Context context) {
    this.context = context;
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    if (call.method.equals("mSupportsAlternateIcons")) {
      result.success(true);
    } else if (call.method.equals("mGetAlternateIconName")) {
      try {
        String currentIcon = getCurrentIconName();
        result.success(currentIcon);
      } catch (Exception e) {
        result.error("ERROR", e.getMessage(), null);
      }
    } else if (call.method.equals("mSetAlternateIconName")) {
      try {
        String iconName = call.argument("iconName");
        setIcon(iconName);
        result.success(null);
      } catch (Exception e) {
        result.error("ERROR", e.getMessage(), null);
      }
    } else if (call.method.equals("mGetAvailableIcons")) {
      try {
        List<String> availableIcons = getAvailableIcons();
        result.success(availableIcons);
      } catch (Exception e) {
        result.error("ERROR", e.getMessage(), null);
      }
    } else {
      result.notImplemented();
    }
  }

  /**
   * Dynamically discovers all available icon aliases by querying PackageManager
   * for all launcher activities in this package
   */
  private List<String> getAvailableIcons() {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();
    List<String> availableIcons = new ArrayList<>();
    
    // Query for all launcher activities in this package
    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
    intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
    intent.setPackage(packageName);
    
    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
    
    String mainActivityName = packageName + MAIN_ACTIVITY_SUFFIX;
    String defaultAliasName = packageName + MAIN_ACTIVITY_SUFFIX + DEFAULT_ALIAS_SUFFIX;
    
    for (ResolveInfo resolveInfo : resolveInfos) {
      ActivityInfo activityInfo = resolveInfo.activityInfo;
      String componentName = activityInfo.name;
      
      // Skip the main activity itself (not an alias)
      if (componentName.equals(mainActivityName)) {
        continue;
      }
      
      // Check if it's an alias for MainActivity
      if (componentName.startsWith(packageName + MAIN_ACTIVITY_SUFFIX + ".")) {
        // Extract icon name from component name
        // Format: package.MainActivity.iconName
        String fullSuffix = componentName.substring(packageName.length() + MAIN_ACTIVITY_SUFFIX.length() + 1);
        
        // Skip the default alias (we use main activity for default)
        if (!fullSuffix.equals("default")) {
          availableIcons.add(fullSuffix);
        }
      }
    }
    
    return availableIcons;
  }

  /**
   * Gets the currently active icon name by checking which activity alias is enabled
   */
  private String getCurrentIconName() {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();
    
    // Get all available icons
    List<String> availableIcons = getAvailableIcons();
    
    // Check main activity (if it's the launcher, we're using default icon)
    ComponentName mainActivity = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX);
    int mainState = pm.getComponentEnabledSetting(mainActivity);
    
    // If main activity is enabled or default, we're using the default icon
    if (mainState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || 
        mainState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
      return null; // Default icon
    }
    
    // Check default alias (if it exists)
    try {
      ComponentName defaultComponent = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + DEFAULT_ALIAS_SUFFIX);
      // Check if component exists
      pm.getActivityInfo(defaultComponent, 0);
      int defaultState = pm.getComponentEnabledSetting(defaultComponent);
      if (defaultState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        return null; // Default icon
      }
    } catch (PackageManager.NameNotFoundException e) {
      // Default alias doesn't exist, which is fine - we use main activity for default
    }
    
    // Check each available icon
    for (String iconName : availableIcons) {
      ComponentName iconComponent = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + iconName);
      int state = pm.getComponentEnabledSetting(iconComponent);
      if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        return iconName;
      }
    }
    
    return null; // Default icon (if no specific icon is enabled)
  }

  /**
   * Sets the app icon by enabling/disabling activity aliases dynamically
   * @param iconName The name of the icon to set, or null for default
   */
  private void setIcon(String iconName) {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();
    
    // Get all available icons
    List<String> availableIcons = getAvailableIcons();
    
    // Get component names
    ComponentName mainActivity = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX);
    ComponentName defaultComponent = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + DEFAULT_ALIAS_SUFFIX);
    
    // Helper method to safely disable a component (only if it exists)
    try {
      // Check if component exists before trying to disable it
      pm.getActivityInfo(defaultComponent, 0);
      // Component exists, disable it
      pm.setComponentEnabledSetting(defaultComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    } catch (PackageManager.NameNotFoundException e) {
      // Default alias doesn't exist, which is fine - we use main activity for default
    }
    
    // Disable main activity and all icon aliases first
    pm.setComponentEnabledSetting(mainActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    
    for (String availableIcon : availableIcons) {
      ComponentName iconComponent = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + availableIcon);
      pm.setComponentEnabledSetting(iconComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    
    // Enable the selected icon
    if (iconName == null || iconName.isEmpty()) {
      // Restore default - enable main activity
      pm.setComponentEnabledSetting(mainActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    } else {
      // Check if the requested icon exists
      boolean iconExists = false;
      for (String availableIcon : availableIcons) {
        if (availableIcon.equals(iconName)) {
          iconExists = true;
          break;
        }
      }
      
      if (!iconExists) {
        throw new IllegalArgumentException("Icon '" + iconName + "' not found. Available icons: " + availableIcons);
      }
      
      // Enable the requested icon alias
      ComponentName iconComponent = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + iconName);
      pm.setComponentEnabledSetting(iconComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
  }
}