package android.support.wearable.internal;

import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import com.google.android.wearable.WearableSharedLib;

public final class SharedLibraryVersion {

    @VisibleForTesting
    static final class PresenceHolder {
        static final boolean PRESENT = isSharedLibPresent(VERSION.SDK_INT);

        PresenceHolder() {
        }

        @VisibleForTesting
        static boolean isSharedLibPresent(int sdkInt) {
            if (sdkInt <= 21) {
                return true;
            }
            try {
                Class.forName("com.google.android.wearable.compat.WearableActivityController");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    @VisibleForTesting
    static final class VersionHolder {
        static final int VERSION = getSharedLibVersion(VERSION.SDK_INT);

        VersionHolder() {
        }

        @VisibleForTesting
        static int getSharedLibVersion(int sdkInt) {
            return sdkInt >= 25 ? WearableSharedLib.version() : 0;
        }
    }

    private SharedLibraryVersion() {
    }

    public static int version() {
        verifySharedLibraryPresent();
        return VersionHolder.VERSION;
    }

    public static void verifySharedLibraryPresent() {
        if (!PresenceHolder.PRESENT) {
            throw new IllegalStateException("Could not find wearable shared library classes. Please add <uses-library android:name=\"com.google.android.wearable\" android:required=\"false\" /> to the application manifest");
        }
    }
}
