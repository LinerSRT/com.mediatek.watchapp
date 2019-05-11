package android.support.v4.app;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.app.RemoteInputCompatBase$RemoteInput.Factory;

public final class RemoteInput extends RemoteInputCompatBase$RemoteInput {
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static final Factory FACTORY = new C00231();
    private static final Impl IMPL;
    private final boolean mAllowFreeFormInput;
    private final CharSequence[] mChoices;
    private final Bundle mExtras;
    private final CharSequence mLabel;
    private final String mResultKey;

    /* renamed from: android.support.v4.app.RemoteInput$1 */
    static class C00231 implements Factory {
        C00231() {
        }

        public RemoteInput build(String resultKey, CharSequence label, CharSequence[] choices, boolean allowFreeFormInput, Bundle extras) {
            return new RemoteInput(resultKey, label, choices, allowFreeFormInput, extras);
        }

        public RemoteInput[] newArray(int size) {
            return new RemoteInput[size];
        }
    }

    interface Impl {
    }

    static class ImplApi20 implements Impl {
        ImplApi20() {
        }
    }

    static class ImplBase implements Impl {
        ImplBase() {
        }
    }

    static class ImplJellybean implements Impl {
        ImplJellybean() {
        }
    }

    RemoteInput(String resultKey, CharSequence label, CharSequence[] choices, boolean allowFreeFormInput, Bundle extras) {
        this.mResultKey = resultKey;
        this.mLabel = label;
        this.mChoices = choices;
        this.mAllowFreeFormInput = allowFreeFormInput;
        this.mExtras = extras;
    }

    static {
        if (VERSION.SDK_INT >= 20) {
            IMPL = new ImplApi20();
        } else if (VERSION.SDK_INT < 16) {
            IMPL = new ImplBase();
        } else {
            IMPL = new ImplJellybean();
        }
    }
}
