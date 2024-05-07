package com.tencentasr.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
  public static void checkPermission(Activity activity, String permission) {
    if (!hasPermission(activity, permission)) {
      ActivityCompat.requestPermissions(activity, new String[] {permission},
                                        1000);
    }
  }

  public static boolean hasPermission(Activity activity, String permission) {
    return ContextCompat.checkSelfPermission(activity, permission) ==
        PackageManager.PERMISSION_GRANTED;
  }
}
