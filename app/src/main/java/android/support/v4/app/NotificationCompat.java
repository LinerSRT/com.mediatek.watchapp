package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.app.NotificationCompatBase$Action.Factory;
import android.support.v4.os.BuildCompat;
import java.util.ArrayList;
import java.util.Collections;

public class NotificationCompat {
    static final NotificationCompatImpl IMPL;

    public static class Action extends NotificationCompatBase$Action {
        @RestrictTo({Scope.LIBRARY_GROUP})
        public static final Factory FACTORY = new C00221();
        public PendingIntent actionIntent;
        public int icon;
        private boolean mAllowGeneratedReplies;
        final Bundle mExtras;
        private final RemoteInput[] mRemoteInputs;
        public CharSequence title;

        /* renamed from: android.support.v4.app.NotificationCompat$Action$1 */
        static class C00221 implements Factory {
            C00221() {
            }

            public NotificationCompatBase$Action build(int icon, CharSequence title, PendingIntent actionIntent, Bundle extras, RemoteInputCompatBase$RemoteInput[] remoteInputs, boolean allowGeneratedReplies) {
                return new Action(icon, title, actionIntent, extras, (RemoteInput[]) remoteInputs, allowGeneratedReplies);
            }

            public Action[] newArray(int length) {
                return new Action[length];
            }
        }

        Action(int icon, CharSequence title, PendingIntent intent, Bundle extras, RemoteInput[] remoteInputs, boolean allowGeneratedReplies) {
            this.icon = icon;
            this.title = Builder.limitCharSequenceLength(title);
            this.actionIntent = intent;
            if (extras == null) {
                extras = new Bundle();
            }
            this.mExtras = extras;
            this.mRemoteInputs = remoteInputs;
            this.mAllowGeneratedReplies = allowGeneratedReplies;
        }
    }

    public static class Builder {
        protected static CharSequence limitCharSequenceLength(CharSequence cs) {
            if (cs == null) {
                return cs;
            }
            if (cs.length() > 5120) {
                cs = cs.subSequence(0, 5120);
            }
            return cs;
        }
    }

    interface NotificationCompatImpl {
        Action getAction(Notification notification, int i);

        int getActionCount(Notification notification);

        Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> arrayList);

        Bundle getExtras(Notification notification);

        boolean getLocalOnly(Notification notification);
    }

    static class NotificationCompatImplBase implements NotificationCompatImpl {
        NotificationCompatImplBase() {
        }

        public Bundle getExtras(Notification n) {
            return null;
        }

        public int getActionCount(Notification n) {
            return 0;
        }

        public Action getAction(Notification n, int actionIndex) {
            return null;
        }

        public Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> arrayList) {
            return null;
        }

        public boolean getLocalOnly(Notification n) {
            return false;
        }
    }

    static class NotificationCompatImplJellybean extends NotificationCompatImplBase {
        NotificationCompatImplJellybean() {
        }

        public Bundle getExtras(Notification n) {
            return NotificationCompatJellybean.getExtras(n);
        }

        public int getActionCount(Notification n) {
            return NotificationCompatJellybean.getActionCount(n);
        }

        public Action getAction(Notification n, int actionIndex) {
            return (Action) NotificationCompatJellybean.getAction(n, actionIndex, Action.FACTORY, RemoteInput.FACTORY);
        }

        public Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> parcelables) {
            return (Action[]) NotificationCompatJellybean.getActionsFromParcelableArrayList(parcelables, Action.FACTORY, RemoteInput.FACTORY);
        }

        public boolean getLocalOnly(Notification n) {
            return NotificationCompatJellybean.getLocalOnly(n);
        }
    }

    static class NotificationCompatImplKitKat extends NotificationCompatImplJellybean {
        NotificationCompatImplKitKat() {
        }

        public Bundle getExtras(Notification n) {
            return NotificationCompatKitKat.getExtras(n);
        }

        public int getActionCount(Notification n) {
            return NotificationCompatKitKat.getActionCount(n);
        }

        public Action getAction(Notification n, int actionIndex) {
            return (Action) NotificationCompatKitKat.getAction(n, actionIndex, Action.FACTORY, RemoteInput.FACTORY);
        }

        public boolean getLocalOnly(Notification n) {
            return NotificationCompatKitKat.getLocalOnly(n);
        }
    }

    static class NotificationCompatImplApi20 extends NotificationCompatImplKitKat {
        NotificationCompatImplApi20() {
        }

        public Action getAction(Notification n, int actionIndex) {
            return (Action) NotificationCompatApi20.getAction(n, actionIndex, Action.FACTORY, RemoteInput.FACTORY);
        }

        public Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> parcelables) {
            return (Action[]) NotificationCompatApi20.getActionsFromParcelableArrayList(parcelables, Action.FACTORY, RemoteInput.FACTORY);
        }

        public boolean getLocalOnly(Notification n) {
            return NotificationCompatApi20.getLocalOnly(n);
        }
    }

    static class NotificationCompatImplApi21 extends NotificationCompatImplApi20 {
        NotificationCompatImplApi21() {
        }
    }

    static class NotificationCompatImplApi24 extends NotificationCompatImplApi21 {
        NotificationCompatImplApi24() {
        }
    }

    static class NotificationCompatImplHoneycomb extends NotificationCompatImplBase {
        NotificationCompatImplHoneycomb() {
        }
    }

    static class NotificationCompatImplIceCreamSandwich extends NotificationCompatImplBase {
        NotificationCompatImplIceCreamSandwich() {
        }
    }

    public static final class WearableExtender {
        private ArrayList<Action> mActions = new ArrayList();
        private Bitmap mBackground;
        private String mBridgeTag;
        private int mContentActionIndex = -1;
        private int mContentIcon;
        private int mContentIconGravity = 8388613;
        private int mCustomContentHeight;
        private int mCustomSizePreset = 0;
        private String mDismissalId;
        private PendingIntent mDisplayIntent;
        private int mFlags = 1;
        private int mGravity = 80;
        private int mHintScreenTimeout;
        private ArrayList<Notification> mPages = new ArrayList();

        public WearableExtender(Notification notif) {
            Bundle wearableBundle = null;
            Bundle extras = NotificationCompat.getExtras(notif);
            if (extras != null) {
                wearableBundle = extras.getBundle("android.wearable.EXTENSIONS");
            }
            if (wearableBundle != null) {
                Action[] actions = NotificationCompat.IMPL.getActionsFromParcelableArrayList(wearableBundle.getParcelableArrayList("actions"));
                if (actions != null) {
                    Collections.addAll(this.mActions, actions);
                }
                this.mFlags = wearableBundle.getInt("flags", 1);
                this.mDisplayIntent = (PendingIntent) wearableBundle.getParcelable("displayIntent");
                Notification[] pages = NotificationCompat.getNotificationArrayFromBundle(wearableBundle, "pages");
                if (pages != null) {
                    Collections.addAll(this.mPages, pages);
                }
                this.mBackground = (Bitmap) wearableBundle.getParcelable("background");
                this.mContentIcon = wearableBundle.getInt("contentIcon");
                this.mContentIconGravity = wearableBundle.getInt("contentIconGravity", 8388613);
                this.mContentActionIndex = wearableBundle.getInt("contentActionIndex", -1);
                this.mCustomSizePreset = wearableBundle.getInt("customSizePreset", 0);
                this.mCustomContentHeight = wearableBundle.getInt("customContentHeight");
                this.mGravity = wearableBundle.getInt("gravity", 80);
                this.mHintScreenTimeout = wearableBundle.getInt("hintScreenTimeout");
                this.mDismissalId = wearableBundle.getString("dismissalId");
                this.mBridgeTag = wearableBundle.getString("bridgeTag");
            }
        }

        public WearableExtender clone() {
            WearableExtender that = new WearableExtender();
            that.mActions = new ArrayList(this.mActions);
            that.mFlags = this.mFlags;
            that.mDisplayIntent = this.mDisplayIntent;
            that.mPages = new ArrayList(this.mPages);
            that.mBackground = this.mBackground;
            that.mContentIcon = this.mContentIcon;
            that.mContentIconGravity = this.mContentIconGravity;
            that.mContentActionIndex = this.mContentActionIndex;
            that.mCustomSizePreset = this.mCustomSizePreset;
            that.mCustomContentHeight = this.mCustomContentHeight;
            that.mGravity = this.mGravity;
            that.mHintScreenTimeout = this.mHintScreenTimeout;
            that.mDismissalId = this.mDismissalId;
            that.mBridgeTag = this.mBridgeTag;
            return that;
        }
    }

    static {
        if (BuildCompat.isAtLeastN()) {
            IMPL = new NotificationCompatImplApi24();
        } else if (VERSION.SDK_INT >= 21) {
            IMPL = new NotificationCompatImplApi21();
        } else if (VERSION.SDK_INT >= 20) {
            IMPL = new NotificationCompatImplApi20();
        } else if (VERSION.SDK_INT >= 19) {
            IMPL = new NotificationCompatImplKitKat();
        } else if (VERSION.SDK_INT >= 16) {
            IMPL = new NotificationCompatImplJellybean();
        } else if (VERSION.SDK_INT >= 14) {
            IMPL = new NotificationCompatImplIceCreamSandwich();
        } else if (VERSION.SDK_INT < 11) {
            IMPL = new NotificationCompatImplBase();
        } else {
            IMPL = new NotificationCompatImplHoneycomb();
        }
    }

    static Notification[] getNotificationArrayFromBundle(Bundle bundle, String key) {
        Parcelable[] array = bundle.getParcelableArray(key);
        if ((array instanceof Notification[]) || array == null) {
            return (Notification[]) array;
        }
        Notification[] typedArray = new Notification[array.length];
        for (int i = 0; i < array.length; i++) {
            typedArray[i] = (Notification) array[i];
        }
        bundle.putParcelableArray(key, typedArray);
        return typedArray;
    }

    public static Bundle getExtras(Notification notif) {
        return IMPL.getExtras(notif);
    }

    public static int getActionCount(Notification notif) {
        return IMPL.getActionCount(notif);
    }

    public static Action getAction(Notification notif, int actionIndex) {
        return IMPL.getAction(notif, actionIndex);
    }

    public static boolean getLocalOnly(Notification notif) {
        return IMPL.getLocalOnly(notif);
    }
}
