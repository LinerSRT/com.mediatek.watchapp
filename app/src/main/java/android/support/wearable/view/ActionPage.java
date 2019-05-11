package android.support.wearable.view;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.mediatek.watchapp.R;

@TargetApi(21)
@Deprecated
public class ActionPage extends ViewGroup {
    private int mBottomInset;
    private final Point mButtonCenter;
    private float mButtonRadius;
    private int mButtonSize;
    private CircularButton mCircularButton;
    private boolean mInsetsApplied;
    private boolean mIsRound;
    private final ActionLabel mLabel;
    private int mTextHeight;
    private int mTextWidth;


    public ActionPage(Context context) {
        this(context, null);
    }

    public ActionPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionPage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_ActionPage);
    }

    public ActionPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mButtonCenter = new Point();
        this.mCircularButton = new CircularButton(context);
        this.mLabel = new ActionLabel(context);
        this.mLabel.setGravity(17);
        this.mLabel.setMaxLines(2);
        float lineSpacingMult = 1.0f;
        float lineSpacingExtra = 0.0f;
        String fontFamily = null;
        int typefaceIndex = 1;
        int styleIndex = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionPage, defStyleAttr, defStyleRes);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ActionPage_android_color) {
                this.mCircularButton.setColor(a.getColorStateList(attr));
            } else if (attr == R.styleable.ActionPage_android_src) {
                this.mCircularButton.setImageDrawable(a.getDrawable(attr));
            } else if (attr == R.styleable.ActionPage_imageScaleMode) {
                this.mCircularButton.setImageScaleMode(a.getInt(attr, 0));
            } else if (attr == R.styleable.ActionPage_buttonRippleColor) {
                this.mCircularButton.setRippleColor(a.getColor(attr, -1));
            } else if (attr == R.styleable.ActionPage_pressedButtonTranslationZ) {
                this.mCircularButton.setPressedTranslationZ(a.getDimension(attr, 0.0f));
            } else if (attr == R.styleable.ActionPage_android_text) {
                this.mLabel.setText(a.getText(attr));
            } else if (attr == R.styleable.ActionPage_minTextSize) {
                this.mLabel.setMinTextSize(0, a.getDimension(attr, 10.0f));
            } else if (attr == R.styleable.ActionPage_maxTextSize) {
                this.mLabel.setMaxTextSize(0, a.getDimension(attr, 60.0f));
            } else if (attr == R.styleable.ActionPage_android_textColor) {
                this.mLabel.setTextColor(a.getColorStateList(attr));
            } else if (attr == R.styleable.ActionPage_android_maxLines) {
                this.mLabel.setMaxLines(a.getInt(attr, 2));
            } else if (attr == R.styleable.ActionPage_android_fontFamily) {
                fontFamily = a.getString(attr);
            } else if (attr == R.styleable.ActionPage_android_typeface) {
                typefaceIndex = a.getInt(attr, typefaceIndex);
            } else if (attr == R.styleable.ActionPage_android_textStyle) {
                styleIndex = a.getInt(attr, styleIndex);
            } else if (attr == R.styleable.ActionPage_android_gravity) {
                this.mLabel.setGravity(a.getInt(attr, 17));
            } else if (attr == R.styleable.ActionPage_android_lineSpacingExtra) {
                lineSpacingExtra = a.getDimension(attr, lineSpacingExtra);
            } else if (attr == R.styleable.ActionPage_android_lineSpacingMultiplier) {
                lineSpacingMult = a.getDimension(attr, lineSpacingMult);
            } else if (attr == R.styleable.ActionPage_android_stateListAnimator) {
                this.mCircularButton.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, a.getResourceId(attr, 0)));
            }
        }
        a.recycle();
        this.mLabel.setLineSpacing(lineSpacingExtra, lineSpacingMult);
        this.mLabel.setTypefaceFromAttrs(fontFamily, typefaceIndex, styleIndex);
        addView(this.mLabel);
        addView(this.mCircularButton);
    }

    public void setStateListAnimator(StateListAnimator stateListAnimator) {
        if (this.mCircularButton != null) {
            this.mCircularButton.setStateListAnimator(stateListAnimator);
        }
    }

    public void setOnClickListener(OnClickListener l) {
        if (this.mCircularButton != null) {
            this.mCircularButton.setOnClickListener(l);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mCircularButton != null) {
            this.mCircularButton.setEnabled(enabled);
        }
    }

    @SuppressLint("WrongConstant")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        if (this.mCircularButton.getImageScaleMode() == 1 && this.mCircularButton.getImageDrawable() != null) {
            this.mCircularButton.measure(0, 0);
            this.mButtonSize = Math.min(this.mCircularButton.getMeasuredWidth(), this.mCircularButton.getMeasuredHeight());
            this.mButtonRadius = ((float) this.mButtonSize) / 2.0f;
        } else {
            this.mButtonSize = (int) (((float) Math.min(width, height)) * 0.45f);
            this.mButtonRadius = ((float) this.mButtonSize) / 2.0f;
            this.mCircularButton.measure(MeasureSpec.makeMeasureSpec(this.mButtonSize, 1073741824), MeasureSpec.makeMeasureSpec(this.mButtonSize, 1073741824));
        }
        if (this.mIsRound) {
            this.mButtonCenter.set(width / 2, height / 2);
            this.mTextWidth = (int) (((float) width) * 0.625f);
            this.mBottomInset = (int) (((float) height) * 0.09375f);
        } else {
            this.mButtonCenter.set(width / 2, (int) (((float) height) * 0.43f));
            this.mTextWidth = (int) (((float) width) * 0.892f);
        }
        this.mTextHeight = (int) ((((float) height) - (((float) this.mButtonCenter.y) + this.mButtonRadius)) - ((float) this.mBottomInset));
        this.mLabel.measure(MeasureSpec.makeMeasureSpec(this.mTextWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mTextHeight, 1073741824));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mInsetsApplied) {
            requestApplyInsets();
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mInsetsApplied = true;
        if (this.mIsRound != insets.isRound()) {
            this.mIsRound = insets.isRound();
            requestLayout();
        }
        int insetBottom = insets.getSystemWindowInsetBottom();
        if (this.mBottomInset != insetBottom) {
            this.mBottomInset = insetBottom;
            requestLayout();
        }
        if (this.mIsRound) {
            this.mBottomInset = (int) Math.max((float) this.mBottomInset, ((float) getMeasuredHeight()) * 0.09375f);
        }
        return insets;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        this.mCircularButton.layout((int) (((float) this.mButtonCenter.x) - this.mButtonRadius), (int) (((float) this.mButtonCenter.y) - this.mButtonRadius), (int) (((float) this.mButtonCenter.x) + this.mButtonRadius), (int) (((float) this.mButtonCenter.y) + this.mButtonRadius));
        int textHorizPadding = (int) (((float) (w - this.mTextWidth)) / 2.0f);
        this.mLabel.layout(textHorizPadding, this.mCircularButton.getBottom(), this.mTextWidth + textHorizPadding, this.mCircularButton.getBottom() + this.mTextHeight);
    }
}
