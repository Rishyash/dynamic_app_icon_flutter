package com.application.dynamic_app_icon_flutter_plus;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the "full reset" component-toggle algorithm: disable MainActivity and every
 * activity-alias under `${applicationId}.MainActivity.*`, then enable exactly one. Used both by
 * the immediate MethodChannel path (for reads / iOS-parity behaviour) and by the deferred
 * service path that fires from onTaskRemoved.
 */
final class IconSwapper {
  static final String MAIN_ACTIVITY_SUFFIX = ".MainActivity";
  private static final String DEFAULT_ALIAS_SUFFIX = ".default";

  static final String PENDING_PREFS = "dynamic_app_icon_flutter_plus.pending";
  static final String PENDING_HAS_KEY = "has_pending";
  static final String PENDING_ICON_KEY = "icon_name";

  private IconSwapper() {}

  /** Read the persisted pending icon (if any) before falling back to live component state. */
  static String readPendingIconName(Context context) {
    SharedPreferences sp = context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE);
    if (!sp.getBoolean(PENDING_HAS_KEY, false)) return null;
    return sp.getString(PENDING_ICON_KEY, null);
  }

  static boolean hasPending(Context context) {
    SharedPreferences sp = context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE);
    return sp.getBoolean(PENDING_HAS_KEY, false);
  }

  static void writePending(Context context, String iconName) {
    SharedPreferences sp = context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit().putBoolean(PENDING_HAS_KEY, true);
    if (iconName == null) {
      editor.remove(PENDING_ICON_KEY);
    } else {
      editor.putString(PENDING_ICON_KEY, iconName);
    }
    editor.apply();
  }

  static void clearPending(Context context) {
    context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE).edit().clear().apply();
  }

  /** Enumerate available alias suffixes (excludes `.default` for backwards compat). */
  static List<String> getAvailableIcons(Context context) {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();
    List<String> availableIcons = new ArrayList<>();

    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
    intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
    intent.setPackage(packageName);

    List<ResolveInfo> resolveInfos =
        pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);

    String mainActivityName = packageName + MAIN_ACTIVITY_SUFFIX;
    String prefix = packageName + MAIN_ACTIVITY_SUFFIX + ".";

    for (ResolveInfo resolveInfo : resolveInfos) {
      ActivityInfo activityInfo = resolveInfo.activityInfo;
      String componentName = activityInfo.name;
      if (componentName.equals(mainActivityName)) continue;
      if (componentName.startsWith(prefix)) {
        String suffix = componentName.substring(prefix.length());
        if (!suffix.equals("default")) {
          availableIcons.add(suffix);
        }
      }
    }
    return availableIcons;
  }

  /**
   * Returns the suffix of the currently-enabled alias, or null if MainActivity itself is the
   * active launcher entry (= default icon).
   */
  static String getCurrentIconName(Context context) {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();

    ComponentName mainActivity = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX);
    int mainState = pm.getComponentEnabledSetting(mainActivity);
    if (mainState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        || mainState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
      return null;
    }

    try {
      ComponentName defaultComponent =
          new ComponentName(
              packageName, packageName + MAIN_ACTIVITY_SUFFIX + DEFAULT_ALIAS_SUFFIX);
      pm.getActivityInfo(defaultComponent, 0);
      int defaultState = pm.getComponentEnabledSetting(defaultComponent);
      if (defaultState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        return null;
      }
    } catch (PackageManager.NameNotFoundException ignored) {
      // No `.default` alias declared — that's fine, MainActivity is the default.
    }

    for (String iconName : getAvailableIcons(context)) {
      ComponentName iconComponent =
          new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + iconName);
      if (pm.getComponentEnabledSetting(iconComponent)
          == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
        return iconName;
      }
    }
    return null;
  }

  /**
   * Full-reset swap: disable MainActivity + all aliases, then enable the target (or re-enable
   * MainActivity if iconName is null/empty). Safe to call from any thread.
   */
  static void applyIcon(Context context, String iconName) {
    PackageManager pm = context.getPackageManager();
    String packageName = context.getPackageName();
    List<String> availableIcons = getAvailableIcons(context);

    ComponentName mainActivity = new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX);
    ComponentName defaultComponent =
        new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + DEFAULT_ALIAS_SUFFIX);

    try {
      pm.getActivityInfo(defaultComponent, 0);
      pm.setComponentEnabledSetting(
          defaultComponent,
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);
    } catch (PackageManager.NameNotFoundException ignored) {
      // No `.default` alias — nothing to disable.
    }

    pm.setComponentEnabledSetting(
        mainActivity,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);

    for (String availableIcon : availableIcons) {
      ComponentName iconComponent =
          new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + availableIcon);
      pm.setComponentEnabledSetting(
          iconComponent,
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);
    }

    if (iconName == null || iconName.isEmpty()) {
      pm.setComponentEnabledSetting(
          mainActivity,
          PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
          PackageManager.DONT_KILL_APP);
      return;
    }

    boolean iconExists = false;
    for (String availableIcon : availableIcons) {
      if (availableIcon.equals(iconName)) {
        iconExists = true;
        break;
      }
    }
    if (!iconExists) {
      throw new IllegalArgumentException(
          "Icon '" + iconName + "' not found. Available icons: " + availableIcons);
    }

    ComponentName iconComponent =
        new ComponentName(packageName, packageName + MAIN_ACTIVITY_SUFFIX + "." + iconName);
    pm.setComponentEnabledSetting(
        iconComponent,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
  }
}
