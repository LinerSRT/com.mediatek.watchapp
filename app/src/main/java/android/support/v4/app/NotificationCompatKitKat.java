package android.support.v4.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Action;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompatBase$Action.Factory;
import android.util.SparseArray;

@TargetApi(19)
@RequiresApi(19)
class NotificationCompatKitKat {
    NotificationCompatKitKat() {
    }

    public static Bundle getExtras(Notification notif) {
        return notif.extras;
    }

    public static int getActionCount(Notification notif) {
        return notif.actions == null ? 0 : notif.actions.length;
    }

    public static NotificationCompatBase$Action getAction(Notification notif, int actionIndex, Factory factory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        Action action = notif.actions[actionIndex];
        Bundle actionExtras = null;
        SparseArray<Bundle> actionExtrasMap = notif.extras.getSparseParcelableArray("android.support.actionExtras");
        if (actionExtrasMap != null) {
            actionExtras = (Bundle) actionExtrasMap.get(actionIndex);
        }
        return NotificationCompatJellybean.readAction(factory, remoteInputFactory, action.icon, action.title, action.actionIntent, actionExtras);
    }

    public static boolean getLocalOnly(Notification notif) {
        return notif.extras.getBoolean("android.support.localOnly");
    }
}
