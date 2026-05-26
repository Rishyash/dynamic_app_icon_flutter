package com.application.dynamic_app_icon_flutter_plus;

import android.content.Context;
import android.content.Intent;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.List;

class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
  private final Context context;

  MethodCallHandlerImpl(Context context) {
    this.context = context;
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    switch (call.method) {
      case "mSupportsAlternateIcons":
        result.success(true);
        break;
      case "mGetAlternateIconName":
        try {
          // Pending state wins over live component state so callers don't observe a stale
          // value between the Flutter call and the deferred onTaskRemoved swap.
          if (IconSwapper.hasPending(context)) {
            result.success(IconSwapper.readPendingIconName(context));
          } else {
            result.success(IconSwapper.getCurrentIconName(context));
          }
        } catch (Exception e) {
          result.error("ERROR", e.getMessage(), null);
        }
        break;
      case "mSetAlternateIconName":
        try {
          String iconName = call.argument("iconName");
          // Validate against available aliases up-front so callers get a synchronous error
          // instead of a silent no-op when the swap eventually runs.
          if (iconName != null && !iconName.isEmpty()) {
            List<String> available = IconSwapper.getAvailableIcons(context);
            if (!available.contains(iconName)) {
              result.error(
                  "ERROR",
                  "Icon '" + iconName + "' not found. Available icons: " + available,
                  null);
              return;
            }
          }
          IconSwapper.writePending(context, iconName);
          // Start the service so its onTaskRemoved/onDestroy will perform the actual swap once
          // the user backgrounds or closes the app — avoids the kick-to-home that
          // setComponentEnabledSetting causes on many OEM launchers when MainActivity is in
          // the foreground.
          context.startService(new Intent(context, DynamicAppIconService.class));
          result.success(null);
        } catch (Exception e) {
          result.error("ERROR", e.getMessage(), null);
        }
        break;
      case "mGetAvailableIcons":
        try {
          result.success(IconSwapper.getAvailableIcons(context));
        } catch (Exception e) {
          result.error("ERROR", e.getMessage(), null);
        }
        break;
      case "mApplyPendingIcon":
        // Immediate flush — called when the app goes to background, before the user has a
        // chance to swipe-kill the task. Safe to call when there's no pending (no-op).
        try {
          if (IconSwapper.hasPending(context)) {
            String iconName = IconSwapper.readPendingIconName(context);
            IconSwapper.applyIcon(context, iconName);
            IconSwapper.clearPending(context);
          }
          result.success(null);
        } catch (Exception e) {
          result.error("ERROR", e.getMessage(), null);
        }
        break;
      default:
        result.notImplemented();
        break;
    }
  }
}
