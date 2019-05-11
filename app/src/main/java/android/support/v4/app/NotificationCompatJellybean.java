package android.support.v4.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompatBase$Action.Factory;
import android.util.Log;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.util.ArrayList;

@TargetApi(16)
@RequiresApi(16)
class NotificationCompatJellybean {
    private static Class<?> sActionClass;
    private static Field sActionIconField;
    private static Field sActionIntentField;
    private static Field sActionTitleField;
    private static boolean sActionsAccessFailed;
    private static Field sActionsField;
    private static final Object sActionsLock = new Object();
    private static Field sExtrasField;
    private static boolean sExtrasFieldAccessFailed;
    private static final Object sExtrasLock = new Object();

    NotificationCompatJellybean() {
    }

    public static Bundle getExtras(Notification notif) {
        synchronized (sExtrasLock) {
            if (sExtrasFieldAccessFailed) {
                return null;
            }
            try {
                if (sExtrasField == null) {
                    Field extrasField = Notification.class.getDeclaredField("extras");
                    if (Bundle.class.isAssignableFrom(extrasField.getType())) {
                        extrasField.setAccessible(true);
                        sExtrasField = extrasField;
                    } else {
                        Log.e("NotificationCompat", "Notification.extras field is not of type Bundle");
                        sExtrasFieldAccessFailed = true;
                        return null;
                    }
                }
                Bundle extras = (Bundle) sExtrasField.get(notif);
                if (extras == null) {
                    extras = new Bundle();
                    sExtrasField.set(notif, extras);
                }
                return extras;
            } catch (IllegalAccessException e) {
                Log.e("NotificationCompat", "Unable to access notification extras", e);
                sExtrasFieldAccessFailed = true;
                return null;
            } catch (NoSuchFieldException e2) {
                Log.e("NotificationCompat", "Unable to access notification extras", e2);
                sExtrasFieldAccessFailed = true;
                return null;
            }
        }
    }

    public static NotificationCompatBase$Action readAction(Factory factory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory, int icon, CharSequence title, PendingIntent actionIntent, Bundle extras) {
        RemoteInputCompatBase$RemoteInput[] remoteInputs = null;
        boolean allowGeneratedReplies = false;
        if (extras != null) {
            remoteInputs = RemoteInputCompatJellybean.fromBundleArray(BundleUtil.getBundleArrayFromBundle(extras, "android.support.remoteInputs"), remoteInputFactory);
            allowGeneratedReplies = extras.getBoolean("android.support.allowGeneratedReplies");
        }
        return factory.build(icon, title, actionIntent, extras, remoteInputs, allowGeneratedReplies);
    }

    public static int getActionCount(Notification notif) {
        int length;
        synchronized (sActionsLock) {
            Object[] actionObjects = getActionObjectsLocked(notif);
            length = actionObjects == null ? 0 : actionObjects.length;
        }
        return length;
    }

    public static NotificationCompatBase$Action getAction(Notification notif, int actionIndex, Factory factory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        synchronized (sActionsLock) {
            try {
                Object[] actionObjects = getActionObjectsLocked(notif);
                if (actionObjects != null) {
                    Object actionObject = actionObjects[actionIndex];
                    Bundle actionExtras = null;
                    Bundle extras = getExtras(notif);
                    if (extras != null) {
                        SparseArray<Bundle> actionExtrasMap = extras.getSparseParcelableArray("android.support.actionExtras");
                        if (actionExtrasMap != null) {
                            actionExtras = (Bundle) actionExtrasMap.get(actionIndex);
                        }
                    }
                    NotificationCompatBase$Action readAction = readAction(factory, remoteInputFactory, sActionIconField.getInt(actionObject), (CharSequence) sActionTitleField.get(actionObject), (PendingIntent) sActionIntentField.get(actionObject), actionExtras);
                    return readAction;
                }
            } catch (IllegalAccessException e) {
                Log.e("NotificationCompat", "Unable to access notification actions", e);
                sActionsAccessFailed = true;
            }
            return null;
        }
    }

    private static Object[] getActionObjectsLocked(Notification notif) {
        synchronized (sActionsLock) {
            if (ensureActionReflectionReadyLocked()) {
                try {
                    Object[] objArr = (Object[]) sActionsField.get(notif);
                    return objArr;
                } catch (IllegalAccessException e) {
                    Log.e("NotificationCompat", "Unable to access notification actions", e);
                    sActionsAccessFailed = true;
                    return null;
                }
            }
            return null;
        }
    }

    private static boolean ensureActionReflectionReadyLocked() {
        boolean z = false;
        if (sActionsAccessFailed) {
            return false;
        }
        try {
            if (sActionsField == null) {
                sActionClass = Class.forName("android.app.Notification$Action");
                sActionIconField = sActionClass.getDeclaredField("icon");
                sActionTitleField = sActionClass.getDeclaredField("title");
                sActionIntentField = sActionClass.getDeclaredField("actionIntent");
                sActionsField = Notification.class.getDeclaredField("actions");
                sActionsField.setAccessible(true);
            }
        } catch (ClassNotFoundException e) {
            Log.e("NotificationCompat", "Unable to access notification actions", e);
            sActionsAccessFailed = true;
        } catch (NoSuchFieldException e2) {
            Log.e("NotificationCompat", "Unable to access notification actions", e2);
            sActionsAccessFailed = true;
        }
        if (!sActionsAccessFailed) {
            z = true;
        }
        return z;
    }

    public static NotificationCompatBase$Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> parcelables, Factory actionFactory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        if (parcelables == null) {
            return null;
        }
        NotificationCompatBase$Action[] actions = actionFactory.newArray(parcelables.size());
        for (int i = 0; i < actions.length; i++) {
            actions[i] = getActionFromBundle((Bundle) parcelables.get(i), actionFactory, remoteInputFactory);
        }
        return actions;
    }

    private static NotificationCompatBase$Action getActionFromBundle(Bundle bundle, Factory actionFactory, RemoteInputCompatBase$RemoteInput.Factory remoteInputFactory) {
        Bundle extras = bundle.getBundle("extras");
        boolean allowGeneratedReplies = false;
        if (extras != null) {
            allowGeneratedReplies = extras.getBoolean("android.support.allowGeneratedReplies", false);
        }
        return actionFactory.build(bundle.getInt("icon"), bundle.getCharSequence("title"), (PendingIntent) bundle.getParcelable("actionIntent"), bundle.getBundle("extras"), RemoteInputCompatJellybean.fromBundleArray(BundleUtil.getBundleArrayFromBundle(bundle, "remoteInputs"), remoteInputFactory), allowGeneratedReplies);
    }

    public static boolean getLocalOnly(Notification notif) {
        return getExtras(notif).getBoolean("android.support.localOnly");
    }
}
