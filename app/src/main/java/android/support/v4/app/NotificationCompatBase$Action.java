package android.support.v4.app;

import android.app.PendingIntent;
import android.os.Bundle;

public abstract class NotificationCompatBase$Action {

    /* renamed from: android.support.v4.app.NotificationCompatBase$Action$Factory */
    public interface Factory {
        NotificationCompatBase$Action build(int i, CharSequence charSequence, PendingIntent pendingIntent, Bundle bundle, RemoteInputCompatBase$RemoteInput[] remoteInputCompatBase$RemoteInputArr, boolean z);

        NotificationCompatBase$Action[] newArray(int i);
    }
}
