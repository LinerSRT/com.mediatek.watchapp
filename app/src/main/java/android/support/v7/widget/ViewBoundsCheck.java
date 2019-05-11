package android.support.v7.widget;

import android.view.View;

class ViewBoundsCheck {
    BoundFlags mBoundFlags = new BoundFlags();
    final Callback mCallback;

    interface Callback {
        View getChildAt(int i);

        int getChildEnd(View view);

        int getChildStart(View view);

        int getParentEnd();

        int getParentStart();
    }

    static class BoundFlags {
        int mBoundFlags = 0;
        int mChildEnd;
        int mChildStart;
        int mRvEnd;
        int mRvStart;

        BoundFlags() {
        }

        void setBounds(int rvStart, int rvEnd, int childStart, int childEnd) {
            this.mRvStart = rvStart;
            this.mRvEnd = rvEnd;
            this.mChildStart = childStart;
            this.mChildEnd = childEnd;
        }

        void addFlags(int flags) {
            this.mBoundFlags |= flags;
        }

        void resetFlags() {
            this.mBoundFlags = 0;
        }

        int compare(int x, int y) {
            if (x > y) {
                return 1;
            }
            if (x != y) {
                return 4;
            }
            return 2;
        }

        boolean boundsMatch() {
            if ((this.mBoundFlags & 7) != 0 && (this.mBoundFlags & (compare(this.mChildStart, this.mRvStart) << 0)) == 0) {
                return false;
            }
            if ((this.mBoundFlags & 112) != 0 && (this.mBoundFlags & (compare(this.mChildStart, this.mRvEnd) << 4)) == 0) {
                return false;
            }
            if ((this.mBoundFlags & 1792) != 0 && (this.mBoundFlags & (compare(this.mChildEnd, this.mRvStart) << 8)) == 0) {
                return false;
            }
            if ((this.mBoundFlags & 28672) != 0 && (this.mBoundFlags & (compare(this.mChildEnd, this.mRvEnd) << 12)) == 0) {
                return false;
            }
            return true;
        }
    }

    ViewBoundsCheck(Callback callback) {
        this.mCallback = callback;
    }

    View findOneViewWithinBoundFlags(int fromIndex, int toIndex, int preferredBoundFlags, int acceptableBoundFlags) {
        int next;
        int start = this.mCallback.getParentStart();
        int end = this.mCallback.getParentEnd();
        if (toIndex <= fromIndex) {
            next = -1;
        } else {
            next = 1;
        }
        View acceptableMatch = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            View child = this.mCallback.getChildAt(i);
            this.mBoundFlags.setBounds(start, end, this.mCallback.getChildStart(child), this.mCallback.getChildEnd(child));
            if (preferredBoundFlags != 0) {
                this.mBoundFlags.resetFlags();
                this.mBoundFlags.addFlags(preferredBoundFlags);
                if (this.mBoundFlags.boundsMatch()) {
                    return child;
                }
            }
            if (acceptableBoundFlags != 0) {
                this.mBoundFlags.resetFlags();
                this.mBoundFlags.addFlags(acceptableBoundFlags);
                if (this.mBoundFlags.boundsMatch()) {
                    acceptableMatch = child;
                }
            }
        }
        return acceptableMatch;
    }
}
