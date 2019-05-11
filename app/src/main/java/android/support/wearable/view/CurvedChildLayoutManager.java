package android.support.wearable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.VisibleForTesting;
import android.support.wearable.R$dimen;
import android.support.wearable.view.WearableRecyclerView.ChildLayoutManager;
import android.view.View;

@TargetApi(23)
public class CurvedChildLayoutManager extends ChildLayoutManager {
    private final float[] mAnchorOffsetXY = new float[2];
    private float mCurveBottom;
    private final Path mCurvePath = new Path();
    private int mCurvePathHeight;
    private float mCurveTop;
    private boolean mIsScreenRound;
    private int mLayoutHeight;
    private int mLayoutWidth;
    private float mLineGradient;
    private WearableRecyclerView mParentView;
    private float mPathLength;
    private final PathMeasure mPathMeasure = new PathMeasure();
    private final float[] mPathPoints = new float[2];
    private final float[] mPathTangent = new float[2];
    private int mXCurveOffset;

    public CurvedChildLayoutManager(Context context) {
        super(context);
        this.mIsScreenRound = context.getResources().getConfiguration().isScreenRound();
        this.mXCurveOffset = context.getResources().getDimensionPixelSize(R$dimen.wrv_curve_default_x_offset);
    }

    @VisibleForTesting
    void setRound(boolean isScreenRound) {
        this.mIsScreenRound = isScreenRound;
    }

    @VisibleForTesting
    void setOffset(int offset) {
        this.mXCurveOffset = offset;
    }

    public void updateChild(View child, WearableRecyclerView parent) {
        if (this.mParentView != parent) {
            this.mParentView = parent;
            this.mLayoutWidth = this.mParentView.getWidth();
            this.mLayoutHeight = this.mParentView.getHeight();
        }
        if (this.mIsScreenRound) {
            boolean topClusterRisk;
            maybeSetUpCircularInitialLayout(this.mLayoutWidth, this.mLayoutHeight);
            this.mAnchorOffsetXY[0] = (float) this.mXCurveOffset;
            this.mAnchorOffsetXY[1] = ((float) child.getHeight()) / 2.0f;
            adjustAnchorOffsetXY(child, this.mAnchorOffsetXY);
            float minCenter = (-((float) child.getHeight())) / 2.0f;
            float maxCenter = ((float) this.mLayoutHeight) + (((float) child.getHeight()) / 2.0f);
            float verticalAnchor = ((float) child.getTop()) + this.mAnchorOffsetXY[1];
            this.mPathMeasure.getPosTan(this.mPathLength * ((Math.abs(minCenter) + verticalAnchor) / (maxCenter - minCenter)), this.mPathPoints, this.mPathTangent);
            if (Math.abs(this.mPathPoints[1] - this.mCurveBottom) >= 0.001f || minCenter >= this.mPathPoints[1]) {
                topClusterRisk = false;
            } else {
                topClusterRisk = true;
            }
            boolean bottomClusterRisk;
            if (Math.abs(this.mPathPoints[1] - this.mCurveTop) >= 0.001f || maxCenter <= this.mPathPoints[1]) {
                bottomClusterRisk = false;
            } else {
                bottomClusterRisk = true;
            }
            if (topClusterRisk || bottomClusterRisk) {
                this.mPathPoints[1] = verticalAnchor;
                this.mPathPoints[0] = Math.abs(verticalAnchor) * this.mLineGradient;
            }
            child.offsetLeftAndRight(((int) (this.mPathPoints[0] - this.mAnchorOffsetXY[0])) - child.getLeft());
            child.setTranslationY(this.mPathPoints[1] - verticalAnchor);
        }
    }

    public void adjustAnchorOffsetXY(View child, float[] anchorOffsetXY) {
    }

    private void maybeSetUpCircularInitialLayout(int width, int height) {
        if (this.mCurvePathHeight != height) {
            this.mCurvePathHeight = height;
            this.mCurveBottom = ((float) height) * -0.048f;
            this.mCurveTop = ((float) height) * 1.048f;
            this.mLineGradient = 10.416667f;
            this.mCurvePath.reset();
            this.mCurvePath.moveTo(((float) width) * 0.5f, this.mCurveBottom);
            this.mCurvePath.lineTo(((float) width) * 0.34f, ((float) height) * 0.075f);
            this.mCurvePath.cubicTo(((float) width) * 0.22f, ((float) height) * 0.17f, ((float) width) * 0.13f, ((float) height) * 0.32f, ((float) width) * 0.13f, (float) (height / 2));
            this.mCurvePath.cubicTo(((float) width) * 0.13f, ((float) height) * 0.68f, ((float) width) * 0.22f, ((float) height) * 0.83f, ((float) width) * 0.34f, ((float) height) * 0.925f);
            this.mCurvePath.lineTo((float) (width / 2), this.mCurveTop);
            this.mPathMeasure.setPath(this.mCurvePath, false);
            this.mPathLength = this.mPathMeasure.getLength();
        }
    }
}
