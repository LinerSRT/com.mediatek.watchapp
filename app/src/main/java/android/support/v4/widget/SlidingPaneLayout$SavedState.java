package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;

class SlidingPaneLayout$SavedState extends AbsSavedState {
    public static final Creator<SlidingPaneLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new C00481());
    boolean isOpen;

    /* renamed from: android.support.v4.widget.SlidingPaneLayout$SavedState$1 */
    static class C00481 implements ParcelableCompatCreatorCallbacks<SlidingPaneLayout$SavedState> {
        C00481() {
        }

        public SlidingPaneLayout$SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SlidingPaneLayout$SavedState(in, loader);
        }

        public SlidingPaneLayout$SavedState[] newArray(int size) {
            return new SlidingPaneLayout$SavedState[size];
        }
    }

    SlidingPaneLayout$SavedState(Parcel in, ClassLoader loader) {
        boolean z = false;
        super(in, loader);
        if (in.readInt() != 0) {
            z = true;
        }
        this.isOpen = z;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 0;
        super.writeToParcel(out, flags);
        if (this.isOpen) {
            i = 1;
        }
        out.writeInt(i);
    }
}
