package android.support.v4.app;

import android.annotation.TargetApi;
import android.app.RemoteInput;
import android.support.annotation.RequiresApi;
import android.support.v4.app.RemoteInputCompatBase$RemoteInput.Factory;

@TargetApi(20)
@RequiresApi(20)
class RemoteInputCompatApi20 {
    RemoteInputCompatApi20() {
    }

    static RemoteInputCompatBase$RemoteInput[] toCompat(RemoteInput[] srcArray, Factory factory) {
        if (srcArray == null) {
            return null;
        }
        RemoteInputCompatBase$RemoteInput[] result = factory.newArray(srcArray.length);
        for (int i = 0; i < srcArray.length; i++) {
            RemoteInput src = srcArray[i];
            result[i] = factory.build(src.getResultKey(), src.getLabel(), src.getChoices(), src.getAllowFreeFormInput(), src.getExtras());
        }
        return result;
    }
}
