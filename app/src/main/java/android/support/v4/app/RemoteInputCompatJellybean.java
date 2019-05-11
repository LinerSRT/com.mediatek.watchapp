package android.support.v4.app;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.RemoteInputCompatBase$RemoteInput.Factory;

@TargetApi(16)
@RequiresApi(16)
class RemoteInputCompatJellybean {
    RemoteInputCompatJellybean() {
    }

    static RemoteInputCompatBase$RemoteInput fromBundle(Bundle data, Factory factory) {
        return factory.build(data.getString("resultKey"), data.getCharSequence("label"), data.getCharSequenceArray("choices"), data.getBoolean("allowFreeFormInput"), data.getBundle("extras"));
    }

    static RemoteInputCompatBase$RemoteInput[] fromBundleArray(Bundle[] bundles, Factory factory) {
        if (bundles == null) {
            return null;
        }
        RemoteInputCompatBase$RemoteInput[] remoteInputs = factory.newArray(bundles.length);
        for (int i = 0; i < bundles.length; i++) {
            remoteInputs[i] = fromBundle(bundles[i], factory);
        }
        return remoteInputs;
    }
}
