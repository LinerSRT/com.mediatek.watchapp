package android.support.wearable.watchface;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Locale;

@TargetApi(21)
public class WatchFaceStyle implements Parcelable {
    public static final Creator<WatchFaceStyle> CREATOR = new C00921();
    private final boolean acceptsTapEvents;
    private final int ambientPeekMode;
    private final int backgroundVisibility;
    private final int cardPeekMode;
    private final int cardProgressMode;
    private final ComponentName component;
    private final boolean hideHotwordIndicator;
    private final boolean hideStatusBar;
    private final int hotwordIndicatorGravity;
    private final int peekOpacityMode;
    private final boolean showSystemUiTime;
    private final boolean showUnreadCountIndicator;
    private final int statusBarGravity;
    private final int viewProtectionMode;

    /* renamed from: android.support.wearable.watchface.WatchFaceStyle$1 */
    static class C00921 implements Creator<WatchFaceStyle> {
        C00921() {
        }

        public WatchFaceStyle createFromParcel(Parcel p) {
            return new WatchFaceStyle(p.readBundle());
        }

        public WatchFaceStyle[] newArray(int size) {
            return new WatchFaceStyle[size];
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBundle(toBundle());
    }

    public WatchFaceStyle(Bundle bundle) {
        this.component = (ComponentName) bundle.getParcelable("component");
        this.ambientPeekMode = bundle.getInt("ambientPeekMode", 0);
        this.backgroundVisibility = bundle.getInt("backgroundVisibility", 0);
        this.cardPeekMode = bundle.getInt("cardPeekMode", 0);
        this.cardProgressMode = bundle.getInt("cardProgressMode", 0);
        this.hotwordIndicatorGravity = bundle.getInt("hotwordIndicatorGravity");
        this.peekOpacityMode = bundle.getInt("peekOpacityMode", 0);
        this.showSystemUiTime = bundle.getBoolean("showSystemUiTime");
        this.showUnreadCountIndicator = bundle.getBoolean("showUnreadIndicator");
        this.statusBarGravity = bundle.getInt("statusBarGravity");
        this.viewProtectionMode = bundle.getInt("viewProtectionMode");
        this.acceptsTapEvents = bundle.getBoolean("acceptsTapEvents");
        this.hideHotwordIndicator = bundle.getBoolean("hideHotwordIndicator");
        this.hideStatusBar = bundle.getBoolean("hideStatusBar");
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("component", this.component);
        bundle.putInt("ambientPeekMode", this.ambientPeekMode);
        bundle.putInt("backgroundVisibility", this.backgroundVisibility);
        bundle.putInt("cardPeekMode", this.cardPeekMode);
        bundle.putInt("cardProgressMode", this.cardProgressMode);
        bundle.putInt("hotwordIndicatorGravity", this.hotwordIndicatorGravity);
        bundle.putInt("peekOpacityMode", this.peekOpacityMode);
        bundle.putBoolean("showSystemUiTime", this.showSystemUiTime);
        bundle.putBoolean("showUnreadIndicator", this.showUnreadCountIndicator);
        bundle.putInt("statusBarGravity", this.statusBarGravity);
        bundle.putInt("viewProtectionMode", this.viewProtectionMode);
        bundle.putBoolean("acceptsTapEvents", this.acceptsTapEvents);
        bundle.putBoolean("hideHotwordIndicator", this.hideHotwordIndicator);
        bundle.putBoolean("hideStatusBar", this.hideStatusBar);
        return bundle;
    }

    public boolean equals(Object otherObj) {
        boolean z = false;
        if (!(otherObj instanceof WatchFaceStyle)) {
            return false;
        }
        WatchFaceStyle other = (WatchFaceStyle) otherObj;
        if (this.component.equals(other.component) && this.cardPeekMode == other.cardPeekMode && this.cardProgressMode == other.cardProgressMode && this.backgroundVisibility == other.backgroundVisibility && this.showSystemUiTime == other.showSystemUiTime && this.ambientPeekMode == other.ambientPeekMode && this.peekOpacityMode == other.peekOpacityMode && this.viewProtectionMode == other.viewProtectionMode && this.statusBarGravity == other.statusBarGravity && this.hotwordIndicatorGravity == other.hotwordIndicatorGravity && this.showUnreadCountIndicator == other.showUnreadCountIndicator && this.acceptsTapEvents == other.acceptsTapEvents && this.hideHotwordIndicator == other.hideHotwordIndicator && this.hideStatusBar == other.hideStatusBar) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i;
        int i2 = 0;
        int hashCode = (((((((((((((((((((this.component.hashCode() + 31) * 31) + this.cardPeekMode) * 31) + this.cardProgressMode) * 31) + this.backgroundVisibility) * 31) + (!this.showSystemUiTime ? 0 : 1)) * 31) + this.ambientPeekMode) * 31) + this.peekOpacityMode) * 31) + this.viewProtectionMode) * 31) + this.statusBarGravity) * 31) + this.hotwordIndicatorGravity) * 31;
        if (this.showUnreadCountIndicator) {
            i = 1;
        } else {
            i = 0;
        }
        hashCode = (hashCode + i) * 31;
        if (this.acceptsTapEvents) {
            i = 1;
        } else {
            i = 0;
        }
        hashCode = (hashCode + i) * 31;
        if (this.hideHotwordIndicator) {
            i = 1;
        } else {
            i = 0;
        }
        i = (hashCode + i) * 31;
        if (this.hideStatusBar) {
            i2 = 1;
        }
        return i + i2;
    }

    public String toString() {
        String shortClassName;
        Locale locale = Locale.US;
        String str = "watch face %s (card %d/%d bg %d time %s ambientPeek %d peekOpacityMode %d viewProtectionMode %d statusBarGravity %d hotwordIndicatorGravity %d showUnreadCountIndicator %s acceptsTapEvents %s hideHotwordIndicator %s hideStatusBar %s)";
        Object[] objArr = new Object[14];
        if (this.component != null) {
            shortClassName = this.component.getShortClassName();
        } else {
            shortClassName = "default";
        }
        objArr[0] = shortClassName;
        objArr[1] = Integer.valueOf(this.cardPeekMode);
        objArr[2] = Integer.valueOf(this.cardProgressMode);
        objArr[3] = Integer.valueOf(this.backgroundVisibility);
        objArr[4] = Boolean.valueOf(this.showSystemUiTime);
        objArr[5] = Integer.valueOf(this.ambientPeekMode);
        objArr[6] = Integer.valueOf(this.peekOpacityMode);
        objArr[7] = Integer.valueOf(this.viewProtectionMode);
        objArr[8] = Integer.valueOf(this.statusBarGravity);
        objArr[9] = Integer.valueOf(this.hotwordIndicatorGravity);
        objArr[10] = Boolean.valueOf(this.showUnreadCountIndicator);
        objArr[11] = Boolean.valueOf(this.acceptsTapEvents);
        objArr[12] = Boolean.valueOf(this.hideHotwordIndicator);
        objArr[13] = Boolean.valueOf(this.hideStatusBar);
        return String.format(locale, str, objArr);
    }
}
