package android.support.wearable.complications;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@TargetApi(24)
public class ComplicationText implements Parcelable {
    public static final Creator<ComplicationText> CREATOR = new C00771();
    private final CharSequence mSurroundingText;
    private final TimeDependentText mTimeDependentText;

    /* renamed from: android.support.wearable.complications.ComplicationText$1 */
    class C00771 implements Creator<ComplicationText> {
        C00771() {
        }

        public ComplicationText createFromParcel(Parcel in) {
            return new ComplicationText(in);
        }

        public ComplicationText[] newArray(int size) {
            return new ComplicationText[size];
        }
    }

    interface TimeDependentText {
    }

    private ComplicationText(Parcel in) {
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        this.mSurroundingText = bundle.getCharSequence("surrounding_string");
        if (bundle.containsKey("difference_style") && bundle.containsKey("difference_period_start") && bundle.containsKey("difference_period_end")) {
            this.mTimeDependentText = new TimeDifferenceText(bundle.getLong("difference_period_start"), bundle.getLong("difference_period_end"), bundle.getInt("difference_style"), bundle.getBoolean("show_now_text", true), timeUnitFromName(bundle.getString("minimum_unit")));
        } else if (bundle.containsKey("format_format_string") && bundle.containsKey("format_style")) {
            TimeZone timeZone = null;
            if (bundle.containsKey("format_time_zone")) {
                timeZone = TimeZone.getTimeZone(bundle.getString("format_time_zone"));
            }
            this.mTimeDependentText = new TimeFormatText(bundle.getString("format_format_string"), bundle.getInt("format_style"), timeZone);
        } else {
            this.mTimeDependentText = null;
        }
        checkFields();
    }

    private static TimeUnit timeUnitFromName(@Nullable String name) {
        TimeUnit timeUnit = null;
        if (name == null) {
            return timeUnit;
        }
        try {
            return TimeUnit.valueOf(name);
        } catch (IllegalArgumentException e) {
            return timeUnit;
        }
    }

    private void checkFields() {
        if (this.mSurroundingText == null && this.mTimeDependentText == null) {
            throw new IllegalStateException("One of mSurroundingText and mTimeDependentText must be non-null");
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence("surrounding_string", this.mSurroundingText);
        if (this.mTimeDependentText instanceof TimeDifferenceText) {
            TimeDifferenceText timeDiffText = this.mTimeDependentText;
            bundle.putLong("difference_period_start", timeDiffText.getReferencePeriodStart());
            bundle.putLong("difference_period_end", timeDiffText.getReferencePeriodEnd());
            bundle.putInt("difference_style", timeDiffText.getStyle());
            bundle.putBoolean("show_now_text", timeDiffText.shouldShowNowText());
            if (timeDiffText.getMinimumUnit() != null) {
                bundle.putString("minimum_unit", timeDiffText.getMinimumUnit().name());
            }
        } else if (this.mTimeDependentText instanceof TimeFormatText) {
            TimeFormatText timeFormatText = this.mTimeDependentText;
            bundle.putString("format_format_string", timeFormatText.getFormatString());
            bundle.putInt("format_style", timeFormatText.getStyle());
            TimeZone timeZone = timeFormatText.getTimeZone();
            if (timeZone != null) {
                bundle.putString("format_time_zone", timeZone.getID());
            }
        }
        out.writeBundle(bundle);
    }

    public int describeContents() {
        return 0;
    }
}
