package android.support.wearable.complications;

import android.annotation.TargetApi;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@TargetApi(24)
public class TimeFormatText implements ComplicationText.TimeDependentText {
    private static final long[] DATE_TIME_FORMAT_PRECISION = new long[]{TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.DAYS.toMillis(1)};
    private static final String[][] DATE_TIME_FORMAT_SYMBOLS;
    private final Date mDate;
    private final SimpleDateFormat mDateFormat;
    private final int mStyle;
    private long mTimePrecision = -1;
    private final TimeZone mTimeZone;

    static {
        String[][] r0 = new String[4][];
        r0[0] = new String[]{"S", "s"};
        r0[1] = new String[]{"m"};
        r0[2] = new String[]{"H", "K", "h", "k"};
        r0[3] = new String[]{"D", "E", "F", "c", "d", "W", "w", "M", "y"};
        DATE_TIME_FORMAT_SYMBOLS = r0;
    }

    public TimeFormatText(String format, int style, TimeZone timeZone) {
        this.mDateFormat = new SimpleDateFormat(format);
        this.mStyle = style;
        if (timeZone == null) {
            this.mTimeZone = this.mDateFormat.getTimeZone();
        } else {
            this.mDateFormat.setTimeZone(timeZone);
            this.mTimeZone = timeZone;
        }
        this.mDate = new Date();
    }

    public String getFormatString() {
        return this.mDateFormat.toPattern();
    }

    public int getStyle() {
        return this.mStyle;
    }

    public TimeZone getTimeZone() {
        return this.mTimeZone;
    }
}
