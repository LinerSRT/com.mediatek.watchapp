package android.support.wearable.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.support.wearable.R$styleable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import java.util.Objects;

@TargetApi(21)
public class ActionLabel extends View {
    private int mCurTextColor;
    private float mCurrentTextSize;
    private int mDrawMaxLines;
    private int mGravity;
    private Layout mLayout;
    private float mLineSpacingAdd;
    private float mLineSpacingMult;
    private int mMaxLines;
    private float mMaxTextSize;
    private float mMinTextSize;
    private float mSpacingAdd;
    private float mSpacingMult;
    private CharSequence mText;
    private ColorStateList mTextColor;
    private final TextPaint mTextPaint;

    public ActionLabel(Context context) {
        this(context, null);
    }

    public ActionLabel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ActionLabel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mGravity = 8388659;
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mMaxLines = Integer.MAX_VALUE;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        float scaledDensity = dm.scaledDensity;
        this.mMinTextSize = 10.0f * scaledDensity;
        this.mMaxTextSize = 60.0f * scaledDensity;
        this.mTextPaint = new TextPaint(1);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R$styleable.ActionLabel, defStyleAttr, defStyleRes);
        this.mText = a.getText(R$styleable.ActionLabel_android_text);
        this.mMinTextSize = a.getDimension(R$styleable.ActionLabel_minTextSize, this.mMinTextSize);
        this.mMaxTextSize = a.getDimension(R$styleable.ActionLabel_maxTextSize, this.mMaxTextSize);
        this.mTextColor = a.getColorStateList(R$styleable.ActionLabel_android_textColor);
        this.mMaxLines = a.getInt(R$styleable.ActionLabel_android_maxLines, 2);
        if (this.mTextColor != null) {
            updateTextColors();
        }
        this.mTextPaint.setTextSize(this.mMaxTextSize);
        setTypefaceFromAttrs(a.getString(R$styleable.ActionLabel_android_fontFamily), a.getInt(R$styleable.ActionLabel_android_typeface, -1), a.getInt(R$styleable.ActionLabel_android_textStyle, -1));
        this.mGravity = a.getInt(R$styleable.ActionLabel_android_gravity, this.mGravity);
        this.mLineSpacingAdd = (float) a.getDimensionPixelSize(R$styleable.ActionLabel_android_lineSpacingExtra, (int) this.mLineSpacingAdd);
        this.mLineSpacingMult = a.getFloat(R$styleable.ActionLabel_android_lineSpacingMultiplier, this.mLineSpacingMult);
        a.recycle();
        if (this.mText == null) {
            this.mText = "";
        }
    }

    public void setText(CharSequence text) {
        if (text == null) {
            throw new RuntimeException("Can not set ActionLabel text to null");
        } else if (!Objects.equals(this.mText, text)) {
            this.mLayout = null;
            this.mText = text;
            requestLayout();
            invalidate();
        }
    }

    public void setMinTextSize(int unit, float size) {
        float sizePx = TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics());
        if (sizePx != this.mMinTextSize) {
            this.mLayout = null;
            this.mMinTextSize = sizePx;
            requestLayout();
            invalidate();
        }
    }

    public void setMaxTextSize(int unit, float size) {
        float sizePx = TypedValue.applyDimension(unit, size, getContext().getResources().getDisplayMetrics());
        if (sizePx != this.mMaxTextSize) {
            this.mLayout = null;
            this.mMaxTextSize = sizePx;
            requestLayout();
            invalidate();
        }
    }

    public void setTypeface(Typeface tf) {
        if (!Objects.equals(this.mTextPaint.getTypeface(), tf)) {
            this.mTextPaint.setTypeface(tf);
            if (this.mLayout != null) {
                requestLayout();
                invalidate();
            }
        }
    }

    public void setTypeface(Typeface tf, int style) {
        boolean z = false;
        if (style <= 0) {
            this.mTextPaint.setFakeBoldText(false);
            this.mTextPaint.setTextSkewX(0.0f);
            setTypeface(tf);
            return;
        }
        float f;
        if (tf != null) {
            tf = Typeface.create(tf, style);
        } else {
            tf = Typeface.defaultFromStyle(style);
        }
        setTypeface(tf);
        int need = style & ((tf == null ? 0 : tf.getStyle()) ^ -1);
        TextPaint textPaint = this.mTextPaint;
        if ((need & 1) != 0) {
            z = true;
        }
        textPaint.setFakeBoldText(z);
        textPaint = this.mTextPaint;
        if ((need & 2) == 0) {
            f = 0.0f;
        } else {
            f = -0.25f;
        }
        textPaint.setTextSkewX(f);
    }

    void setTypefaceFromAttrs(String familyName, int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        if (familyName != null) {
            tf = Typeface.create(familyName, styleIndex);
            if (tf != null) {
                setTypeface(tf);
                return;
            }
        }
        switch (typefaceIndex) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        setTypeface(tf, styleIndex);
    }

    public void setLineSpacing(float add, float mult) {
        if (this.mSpacingAdd != add || this.mSpacingMult != mult) {
            this.mSpacingAdd = add;
            this.mSpacingMult = mult;
            if (this.mLayout != null) {
                this.mLayout = null;
                requestLayout();
                invalidate();
            }
        }
    }

    public void setTextColor(ColorStateList colors) {
        if (colors != null) {
            this.mTextColor = colors;
            updateTextColors();
            return;
        }
        throw new NullPointerException();
    }

    public void setMaxLines(int lines) {
        if (this.mMaxLines != lines) {
            this.mMaxLines = lines;
            this.mLayout = null;
            requestLayout();
            invalidate();
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            invalidate();
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mTextColor != null && this.mTextColor.isStateful()) {
            updateTextColors();
        }
    }

    private void updateTextColors() {
        int color = this.mTextColor.getColorForState(getDrawableState(), 0);
        if (color != this.mCurTextColor) {
            this.mCurTextColor = color;
            invalidate();
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        this.mLayout = null;
        requestLayout();
        invalidate();
    }

    @SuppressLint({"RtlHardcoded"})
    private Alignment getLayoutAlignment() {
        switch (getTextAlignment()) {
            case 1:
                switch (this.mGravity & 8388615) {
                    case 1:
                        return Alignment.ALIGN_CENTER;
                    case 3:
                        return Alignment.ALIGN_NORMAL;
                    case 5:
                        return Alignment.ALIGN_OPPOSITE;
                    case 8388611:
                        return Alignment.ALIGN_NORMAL;
                    case 8388613:
                        return Alignment.ALIGN_OPPOSITE;
                    default:
                        return Alignment.ALIGN_NORMAL;
                }
            case 2:
                return Alignment.ALIGN_NORMAL;
            case 3:
                return Alignment.ALIGN_OPPOSITE;
            case 4:
                return Alignment.ALIGN_CENTER;
            default:
                return Alignment.ALIGN_NORMAL;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = -1;
        int height = -1;
        if (widthMode == 1073741824) {
            width = widthSize;
        }
        if (heightMode == 1073741824) {
            height = heightSize;
        }
        if (width == -1) {
            this.mTextPaint.setTextSize(this.mMaxTextSize);
            width = (int) Math.ceil((double) Layout.getDesiredWidth(this.mText, this.mTextPaint));
            this.mTextPaint.setTextSize(this.mCurrentTextSize);
        }
        if (widthMode == Integer.MIN_VALUE) {
            width = Math.min(width, widthSize);
        }
        Alignment alignment = getLayoutAlignment();
        if (height == -1) {
            height = heightMode != Integer.MIN_VALUE ? Integer.MAX_VALUE : heightSize;
        }
        if (this.mLayout != null) {
            boolean widthChanged;
            if (this.mLayout.getWidth() == width) {
                widthChanged = false;
            } else {
                widthChanged = true;
            }
            boolean heightChanged;
            if (this.mLayout.getHeight() == height) {
                heightChanged = false;
            } else {
                heightChanged = true;
            }
            if (widthChanged || heightChanged) {
                this.mLayout = makeNewLayout(width, height, alignment);
            }
        } else {
            this.mLayout = makeNewLayout(width, height, alignment);
        }
        if (this.mLayout != null) {
            if (heightMode != 1073741824) {
                height = this.mLayout.getLineTop(this.mLayout.getLineCount());
            }
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(height, heightSize);
            }
            setMeasuredDimension(width, height);
            return;
        }
        setMeasuredDimension(0, 0);
    }

    private Layout makeNewLayout(int width, int height, Alignment alignment) {
        if (height <= 0 || width <= 0) {
            return null;
        }
        boolean tooManyLines;
        boolean tooTall;
        boolean textCanShrink;
        int availableHeight = height - (getPaddingTop() + getPaddingBottom());
        int availableWidth = width - (getPaddingLeft() + getPaddingRight());
        this.mCurrentTextSize = this.mMaxTextSize;
        this.mTextPaint.setTextSize(this.mMaxTextSize);
        int tries = 1;
        Layout layout = new StaticLayout(this.mText, this.mTextPaint, availableWidth, alignment, this.mSpacingMult, this.mSpacingAdd, true);
        if (layout.getLineCount() <= this.mMaxLines) {
            tooManyLines = false;
        } else {
            tooManyLines = true;
        }
        if (layout.getLineTop(layout.getLineCount()) <= availableHeight) {
            tooTall = false;
        } else {
            tooTall = true;
        }
        if (this.mTextPaint.getTextSize() > this.mMinTextSize) {
            textCanShrink = true;
        } else {
            textCanShrink = false;
        }
        if (tooManyLines || tooTall) {
            while (true) {
                if (!tooManyLines && !tooTall) {
                    break;
                } else if (!textCanShrink) {
                    break;
                } else {
                    this.mCurrentTextSize -= 1.0f;
                    this.mTextPaint.setTextSize(this.mCurrentTextSize);
                    layout = new StaticLayout(this.mText, this.mTextPaint, availableWidth, alignment, this.mSpacingMult, this.mSpacingAdd, true);
                    if (layout.getLineTop(layout.getLineCount()) <= availableHeight) {
                        tooTall = false;
                    } else {
                        tooTall = true;
                    }
                    if (layout.getLineCount() <= this.mMaxLines) {
                        tooManyLines = false;
                    } else {
                        tooManyLines = true;
                    }
                    if (this.mTextPaint.getTextSize() > this.mMinTextSize) {
                        textCanShrink = true;
                    } else {
                        textCanShrink = false;
                    }
                    tries++;
                }
            }
        }
        this.mDrawMaxLines = Math.min(this.mMaxLines, layout.getLineCount());
        return layout;
    }

    private int getAvailableHeight() {
        return getHeight() - (getPaddingTop() + getPaddingBottom());
    }

    int getVerticalOffset() {
        int availHeight = getAvailableHeight();
        int textHeight = this.mLayout.getLineTop(this.mDrawMaxLines);
        switch (this.mGravity & 112) {
            case R$styleable.ActionPage_buttonRippleColor /*16*/:
                return (availHeight - textHeight) / 2;
            case 48:
                return 0;
            case 80:
                return availHeight - textHeight;
            default:
                return 0;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mLayout != null) {
            canvas.save();
            this.mTextPaint.setColor(this.mCurTextColor);
            this.mTextPaint.drawableState = getDrawableState();
            canvas.translate((float) getPaddingLeft(), (float) (getPaddingTop() + getVerticalOffset()));
            canvas.clipRect(0, 0, getWidth() - getPaddingRight(), this.mLayout.getLineTop(this.mDrawMaxLines));
            this.mLayout.draw(canvas);
            canvas.restore();
        }
    }
}
