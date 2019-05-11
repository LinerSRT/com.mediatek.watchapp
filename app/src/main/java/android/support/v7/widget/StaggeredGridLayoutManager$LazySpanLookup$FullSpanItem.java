package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

class StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem implements Parcelable {
    public static final Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> CREATOR = new C00731();
    int mGapDir;
    int[] mGapPerSpan;
    boolean mHasUnwantedGapAfter;
    int mPosition;

    /* renamed from: android.support.v7.widget.StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem$1 */
    static class C00731 implements Creator<StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem> {
        C00731() {
        }

        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem createFromParcel(Parcel in) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(in);
        }

        public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[] newArray(int size) {
            return new StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem[size];
        }
    }

    public StaggeredGridLayoutManager$LazySpanLookup$FullSpanItem(Parcel in) {
        boolean z = false;
        this.mPosition = in.readInt();
        this.mGapDir = in.readInt();
        if (in.readInt() == 1) {
            z = true;
        }
        this.mHasUnwantedGapAfter = z;
        int spanCount = in.readInt();
        if (spanCount > 0) {
            this.mGapPerSpan = new int[spanCount];
            in.readIntArray(this.mGapPerSpan);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPosition);
        dest.writeInt(this.mGapDir);
        dest.writeInt(!this.mHasUnwantedGapAfter ? 0 : 1);
        if (this.mGapPerSpan != null && this.mGapPerSpan.length > 0) {
            dest.writeInt(this.mGapPerSpan.length);
            dest.writeIntArray(this.mGapPerSpan);
            return;
        }
        dest.writeInt(0);
    }

    public String toString() {
        return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
    }
}
