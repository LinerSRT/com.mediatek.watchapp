package android.support.wearable.complications;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

@TargetApi(24)
public class ComplicationData implements Parcelable {
    public static final Creator<ComplicationData> CREATOR = new C00751();
    private static final String[][] OPTIONAL_FIELDS;
    private static final String[][] REQUIRED_FIELDS;
    private final Bundle mFields;
    private final int mType;

    /* renamed from: android.support.wearable.complications.ComplicationData$1 */
    static class C00751 implements Creator<ComplicationData> {
        C00751() {
        }

        public ComplicationData createFromParcel(Parcel source) {
            return new ComplicationData(source);
        }

        public ComplicationData[] newArray(int size) {
            return new ComplicationData[size];
        }
    }

    static {
        String[][] r0 = new String[11][];
        r0[3] = new String[]{"SHORT_TEXT"};
        r0[4] = new String[]{"LONG_TEXT"};
        r0[5] = new String[]{"VALUE", "MIN_VALUE", "MAX_VALUE"};
        r0[6] = new String[]{"ICON"};
        r0[7] = new String[]{"SMALL_IMAGE", "IMAGE_STYLE"};
        r0[8] = new String[]{"LARGE_IMAGE"};
        r0[9] = new String[0];
        r0[10] = new String[0];
        REQUIRED_FIELDS = r0;
        r0 = new String[11][];
        r0[3] = new String[]{"SHORT_TITLE", "ICON", "ICON_BURN_IN_PROTECTION", "TAP_ACTION"};
        r0[4] = new String[]{"LONG_TITLE", "ICON", "ICON_BURN_IN_PROTECTION", "SMALL_IMAGE", "IMAGE_STYLE", "TAP_ACTION"};
        r0[5] = new String[]{"SHORT_TEXT", "SHORT_TITLE", "ICON", "ICON_BURN_IN_PROTECTION", "TAP_ACTION"};
        r0[6] = new String[]{"TAP_ACTION", "ICON_BURN_IN_PROTECTION"};
        r0[7] = new String[]{"TAP_ACTION"};
        r0[8] = new String[]{"TAP_ACTION"};
        r0[9] = new String[]{"SHORT_TEXT", "SHORT_TITLE", "ICON", "ICON_BURN_IN_PROTECTION"};
        r0[10] = new String[0];
        OPTIONAL_FIELDS = r0;
    }

    private ComplicationData(Parcel in) {
        this.mType = in.readInt();
        this.mFields = in.readBundle(getClass().getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeBundle(this.mFields);
    }

    public String toString() {
        int i = this.mType;
        String valueOf = String.valueOf(this.mFields);
        return new StringBuilder(String.valueOf(valueOf).length() + 45).append("ComplicationData{mType=").append(i).append(", mFields=").append(valueOf).append("}").toString();
    }
}
