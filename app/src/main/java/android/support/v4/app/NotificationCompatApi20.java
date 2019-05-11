package android.support.v4.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Action;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompatBase$Action.Factory;
import java.util.ArrayList;

@TargetApi(20)
@RequiresApi(20)
class NotificationCompatApi20 {
    NotificationCompatApi20() {
    }

    public static NotificationCompatBase$Action getAction(Notification notif, int actionIndex, Factory actionFactory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        return getActionCompatFromAction(notif.actions[actionIndex], actionFactory, remoteInputFactory);
    }

    private static NotificationCompatBase$Action getActionCompatFromAction(Action action, Factory actionFactory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        return actionFactory.build(action.icon, action.title, action.actionIntent, action.getExtras(), RemoteInputCompatApi20.toCompat(action.getRemoteInputs(), remoteInputFactory), action.getExtras().getBoolean("android.support.allowGeneratedReplies"));
    }

    public static NotificationCompatBase$Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> parcelables, Factory actionFactory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        if (parcelables == null) {
            return null;
        }
        NotificationCompatBase$Action[] actions = actionFactory.newArray(parcelables.size());
        for (int i = 0; i < actions.length; i++) {
            actions[i] = getActionCompatFromAction((Action) parcelables.get(i), actionFactory, remoteInputFactory);
        }
        return actions;
    }

    public static boolean getLocalOnly(Notification notif) {
        return (notif.flags & 256) != 0;
    }
}
