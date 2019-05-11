package android.support.wearable.complications;

import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@TargetApi(24)
public class TimeDifferenceText implements ComplicationText.TimeDependentText {
    @Nullable
    private final TimeUnit mMinimumUnit;
    private final long mReferencePeriodEnd;
    private final long mReferencePeriodStart;
    private final boolean mShowNowText;
    private final int mStyle;

    public TimeDifferenceText(long referencePeriodStart, long referencePeriodEnd, int style, boolean showNowText, @Nullable TimeUnit minimumUnit) {
        this.mReferencePeriodStart = referencePeriodStart;
        this.mReferencePeriodEnd = referencePeriodEnd;
        this.mStyle = style;
        this.mShowNowText = showNowText;
        this.mMinimumUnit = minimumUnit;
    }

    long getReferencePeriodStart() {
        return this.mReferencePeriodStart;
    }

    long getReferencePeriodEnd() {
        return this.mReferencePeriodEnd;
    }

    int getStyle() {
        return this.mStyle;
    }

    boolean shouldShowNowText() {
        return this.mShowNowText;
    }

    @Nullable
    TimeUnit getMinimumUnit() {
        return this.mMinimumUnit;
    }
}
