package android.support.wearable.preference;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;

class AcceptDenyDialogPreference$SavedState extends BaseSavedState {
    public static final Creator<AcceptDenyDialogPreference$SavedState> CREATOR = new C00781();
    Bundle dialogBundle;
    boolean isDialogShowing;

    /* renamed from: android.support.wearable.preference.AcceptDenyDialogPreference$SavedState$1 */
    class C00781 implements Creator<AcceptDenyDialogPreference$SavedState> {
        C00781() {
        }

        public AcceptDenyDialogPreference$SavedState createFromParcel(Parcel in) {
            return new AcceptDenyDialogPreference$SavedState(in);
        }

        public AcceptDenyDialogPreference$SavedState[] newArray(int size) {
            return new AcceptDenyDialogPreference$SavedState[size];
        }
    }

    public AcceptDenyDialogPreference$SavedState(Parcel source) {
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
