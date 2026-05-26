package com.application.dynamic_app_icon_flutter_plus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Defers the actual component-enable toggle to the moment the user removes the task (or the
 * service is otherwise destroyed). Doing the toggle while MainActivity is foregrounded triggers
 * a launcher refresh that, on many OEM launchers, steals focus and kicks the user back to home.
 * Running it from onTaskRemoved sidesteps the issue because the activity is already gone.
 *
 * Requires `android:stopWithTask="false"` in the host AndroidManifest so this service outlives
 * the task removal long enough to receive onTaskRemoved.
 */
public class DynamicAppIconService extends Service {
  private static final String TAG = "DynamicAppIconService";

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_NOT_STICKY;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Log.d(TAG, "onTaskRemoved — applying pending icon");
    applyPending();
    super.onTaskRemoved(rootIntent);
    stopSelf();
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy — applying pending icon");
    applyPending();
    super.onDestroy();
  }

  private void applyPending() {
    if (!IconSwapper.hasPending(this)) return;
    String iconName = IconSwapper.readPendingIconName(this);
    try {
      IconSwapper.applyIcon(this, iconName);
      IconSwapper.clearPending(this);
    } catch (Exception e) {
      Log.e(TAG, "Failed to apply pending icon '" + iconName + "'", e);
    }
  }
}
