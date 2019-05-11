package android.support.wearable.preference;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;

class AcceptDenySwitchPreference$SavedState extends BaseSavedState {
    public static final Creator<AcceptDenySwitchPreference$SavedState> CREATOR = new C00791();
    Bundle dialogBundle;
    boolean isDialogShowing;

    /* renamed from: android.support.wearable.preference.AcceptDenySwitchPreference$SavedState$1 */
    class C00791 implements Creator<AcceptDenySwitchPreference$SavedState> {
        C00791() {
        }

        public AcceptDenySwitchPreference$SavedState createFromParcel(Parcel in) {
            return new AcceptDenySwitchPreference$SavedState(in);
        }

        public AcceptDenySwitchPreference$SavedState[] newArray(int size) {
            return new AcceptDenySwitchPreference$SavedState[size];
        }
    }

    public AcceptDenySwitchPreference$SavedState(Parcel source) {
        boolean z = true;
        super(source);
        if (source.readInt() != 1) {
            z = false;
        }
        this.isDialogShowing = z;
        this.dialogBundle = source.readBundle();
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        super.writeToParcel(dest, flags);
        if (this.isDialogShowing) {
            i = 1;
        }
        dest.writeInt(i);
        dest.writeBundle(this.dialogBundle);
    }
}
