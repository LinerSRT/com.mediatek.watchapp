package android.support.wearable.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AcceptDenyDialogFragment$Builder implements Parcelable {
    public static final Creator<AcceptDenyDialogFragment$Builder> CREATOR = new C00801();
    private int mIconRes;
    private String mMessage;
    private boolean mShowNegativeButton;
    private boolean mShowPositiveButton;
    private String mTitle;

    /* renamed from: android.support.wearable.view.AcceptDenyDialogFragment$Builder$1 */
    class C00801 implements Creator<AcceptDenyDialogFragment$Builder> {
        C00801() {
        }

        public AcceptDenyDialogFragment$Builder createFromParcel(Parcel in) {
            return new AcceptDenyDialogFragment$Builder(in);
        }

        public AcceptDenyDialogFragment$Builder[] newArray(int size) {
            return new AcceptDenyDialogFragment$Builder[size];
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mTitle);
        out.writeString(this.mMessage);
        out.writeInt(this.mIconRes);
        out.writeValue(Boolean.valueOf(this.mShowPositiveButton));
        out.writeValue(Boolean.valueOf(this.mShowNegativeButton));
    }

    private AcceptDenyDialogFragment$Builder(Parcel in) {
        this.mTitle = in.readString();
        this.mMessage = in.readString();
        this.mIconRes = in.readInt();
        this.mShowPositiveButton = ((Boolean) in.readValue(null)).booleanValue();
        this.mShowNegativeButton = ((Boolean) in.readValue(null)).booleanValue();
    }
}
