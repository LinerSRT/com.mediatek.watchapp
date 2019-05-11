package android.support.v4.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

public class ActivityCompat extends ContextCompat {
    protected ActivityCompat() {
    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode, @Nullable Bundle options) {
        if (VERSION.SDK_INT < 16) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            ActivityCompatJB.startActivityForResult(activity, intent, requestCode, options);
        }
    }
}
