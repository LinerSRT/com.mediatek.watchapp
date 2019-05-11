package android.support.wearable.complications;

import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@TargetApi(24)
public class ComplicationProviderInfo implements Parcelable {
    public static final Creator<ComplicationProviderInfo> CREATOR = new C00761();
    public final String appName;
    public final int complicationType;
    public final Icon providerIcon;
    public final String providerName;

    /* renamed from: android.support.wearable.complications.ComplicationProviderInfo$1 */
    class C00761 implements Creator<ComplicationProviderInfo> {
        C00761() {
        }

        public ComplicationProviderInfo createFromParcel(Parcel source) {
            return new ComplicationProviderInfo(source);
        }

        public ComplicationProviderInfo[] newArray(int size) {
            return new ComplicationProviderInfo[size];
        }
    }

    public ComplicationProviderInfo(Parcel in) {
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        this.appName = bundle.getString("app_name");
        this.providerName = bundle.getString("provider_name");
        this.providerIcon = (Icon) bundle.getParcelable("provider_icon");
        this.complicationType = bundle.getInt("complication_type");
    }

    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("app_name", this.appName);
        bundle.putString("provider_name", this.providerName);
        bundle.putParcelable("provider_icon", this.providerIcon);
        bundle.putInt("complication_type", this.complicationType);
        dest.writeBundle(bundle);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String str = this.appName;
        String str2 = this.providerName;
        String valueOf = String.valueOf(this.providerIcon);
        return new StringBuilder(((String.valueOf(str).length() + 98) + String.valueOf(str2).length()) + String.valueOf(valueOf).length()).append("ComplicationProviderInfo{appName='").append(str).append("'").append(", providerName='").append(str2).append("'").append(", providerIcon=").append(valueOf).append(", complicationType=").append(this.complicationType).append("}").toString();
    }
}
