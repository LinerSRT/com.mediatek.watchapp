package android.support.v4.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.ArraySet;
import android.support.v4.util.DebugUtils;
import android.support.v4.util.LogWriter;
import android.support.v4.util.Pair;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: FragmentManager */
final class FragmentManagerImpl extends FragmentManager implements LayoutInflaterFactory {
    static final Interpolator ACCELERATE_CUBIC = new AccelerateInterpolator(1.5f);
    static final Interpolator ACCELERATE_QUINT = new AccelerateInterpolator(2.5f);
    static boolean DEBUG = false;
    static final Interpolator DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);
    static final Interpolator DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
    static final boolean HONEYCOMB;
    static Field sAnimationListenerField = null;
    ArrayList<Fragment> mActive;
    ArrayList<Fragment> mAdded;
    ArrayList<Integer> mAvailBackStackIndices;
    ArrayList<Integer> mAvailIndices;
    ArrayList<BackStackRecord> mBackStack;
    ArrayList<OnBackStackChangedListener> mBackStackChangeListeners;
    ArrayList<BackStackRecord> mBackStackIndices;
    FragmentContainer mContainer;
    ArrayList<Fragment> mCreatedMenus;
    int mCurState = 0;
    boolean mDestroyed;
    Runnable mExecCommit = new C00051();
    boolean mExecutingActions;
    boolean mHavePendingDeferredStart;
    FragmentHostCallback mHost;
    private CopyOnWriteArrayList<Pair<FragmentLifecycleCallbacks, Boolean>> mLifecycleCallbacks;
    boolean mNeedMenuInvalidate;
    String mNoTransactionsBecause;
    Fragment mParent;
    ArrayList<OpGenerator> mPendingActions;
    ArrayList<StartEnterTransitionListener> mPostponedTransactions;
    SparseArray<Parcelable> mStateArray = null;
    Bundle mStateBundle = null;
    boolean mStateSaved;
    ArrayList<Fragment> mTmpAddedFragments;
    ArrayList<Boolean> mTmpIsPop;
    ArrayList<BackStackRecord> mTmpRecords;

    /* compiled from: FragmentManager */
    interface OpGenerator {
        boolean generateOps(ArrayList<BackStackRecord> arrayList, ArrayList<Boolean> arrayList2);
    }

    /* compiled from: FragmentManager */
    /* renamed from: android.support.v4.app.FragmentManagerImpl$1 */
    class C00051 implements Runnable {
        C00051() {
        }

        public void run() {
            FragmentManagerImpl.this.execPendingActions();
        }
    }

    /* compiled from: FragmentManager */
    static class AnimateOnHWLayerIfNeededListener implements AnimationListener {
        private AnimationListener mOriginalListener;
        private boolean mShouldRunOnHWLayer;
        View mView;

        /* compiled from: FragmentManager */
        /* renamed from: android.support.v4.app.FragmentManagerImpl$AnimateOnHWLayerIfNeededListener$1 */
        class C00071 implements Runnable {
            C00071() {
            }

            public void run() {
                ViewCompat.setLayerType(AnimateOnHWLayerIfNeededListener.this.mView, 0, null);
            }
        }

        public AnimateOnHWLayerIfNeededListener(View v, Animation anim) {
            if (v != null && anim != null) {
                this.mView = v;
            }
        }

        public AnimateOnHWLayerIfNeededListener(View v, Animation anim, AnimationListener listener) {
            if (v != null && anim != null) {
                this.mOriginalListener = listener;
                this.mView = v;
                this.mShouldRunOnHWLayer = true;
            }
        }

        @CallSuper
        public void onAnimationStart(Animation animation) {
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationStart(animation);
            }
        }

        @CallSuper
        public void onAnimationEnd(Animation animation) {
            if (this.mView != null && this.mShouldRunOnHWLayer) {
                if (ViewCompat.isAttachedToWindow(this.mView) || BuildCompat.isAtLeastN()) {
                    this.mView.post(new C00071());
                } else {
                    ViewCompat.setLayerType(this.mView, 0, null);
                }
            }
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationEnd(animation);
            }
        }

        public void onAnimationRepeat(Animation animation) {
            if (this.mOriginalListener != null) {
                this.mOriginalListener.onAnimationRepeat(animation);
            }
        }
    }

    /* compiled from: FragmentManager */
    static class FragmentTag {
        public static final int[] Fragment = new int[]{16842755, 16842960, 16842961};

        FragmentTag() {
        }
    }

    /* compiled from: FragmentManager */
    static class StartEnterTransitionListener implements OnStartEnterTransitionListener {
        private final boolean mIsBack;
        private int mNumPostponed;
        private final BackStackRecord mRecord;

        StartEnterTransitionListener(BackStackRecord record, boolean isBack) {
            this.mIsBack = isBack;
            this.mRecord = record;
        }

        public void onStartEnterTransition() {
            this.mNumPostponed--;
            if (this.mNumPostponed == 0) {
                this.mRecord.mManager.scheduleCommit();
            }
        }

        public void startListening() {
            this.mNumPostponed++;
        }

        public boolean isReady() {
            return this.mNumPostponed == 0;
        }

        public void completeTransaction() {
            boolean canceled;
            boolean z = false;
            if (this.mNumPostponed <= 0) {
                canceled = false;
            } else {
                canceled = true;
            }
            FragmentManagerImpl manager = this.mRecord.mManager;
            int numAdded = manager.mAdded.size();
            for (int i = 0; i < numAdded; i++) {
                Fragment fragment = (Fragment) manager.mAdded.get(i);
                fragment.setOnStartEnterTransitionListener(null);
                if (canceled && fragment.isPostponed()) {
                    fragment.startPostponedEnterTransition();
                }
            }
            FragmentManagerImpl fragmentManagerImpl = this.mRecord.mManager;
            BackStackRecord backStackRecord = this.mRecord;
            boolean z2 = this.mIsBack;
            if (!canceled) {
                z = true;
            }
            fragmentManagerImpl.completeExecute(backStackRecord, z2, z, true);
        }

        public void cancelTransaction() {
            this.mRecord.mManager.completeExecute(this.mRecord, this.mIsBack, false, false);
        }
    }

    FragmentManagerImpl() {
    }

    static {
        boolean z = false;
        if (VERSION.SDK_INT >= 11) {
            z = true;
        }
        HONEYCOMB = z;
    }

    static boolean modifiesAlpha(Animation anim) {
        if (anim instanceof AlphaAnimation) {
            return true;
        }
        if (anim instanceof AnimationSet) {
            List<Animation> anims = ((AnimationSet) anim).getAnimations();
            for (int i = 0; i < anims.size(); i++) {
                if (anims.get(i) instanceof AlphaAnimation) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean shouldRunOnHWLayer(View v, Animation anim) {
        if (VERSION.SDK_INT >= 19 && ViewCompat.getLayerType(v) == 0 && ViewCompat.hasOverlappingRendering(v) && modifiesAlpha(anim)) {
            return true;
        }
        return false;
    }

    private void throwException(RuntimeException ex) {
        Log.e("FragmentManager", ex.getMessage());
        Log.e("FragmentManager", "Activity state:");
        PrintWriter pw = new PrintWriter(new LogWriter("FragmentManager"));
        if (this.mHost == null) {
            try {
                dump("  ", null, pw, new String[0]);
            } catch (Exception e) {
                Log.e("FragmentManager", "Failed dumping state", e);
            }
        } else {
            try {
                this.mHost.onDump("  ", null, pw, new String[0]);
            } catch (Exception e2) {
                Log.e("FragmentManager", "Failed dumping state", e2);
            }
        }
        throw ex;
    }

    public FragmentTransaction beginTransaction() {
        return new BackStackRecord(this);
    }

    public boolean popBackStackImmediate() {
        checkStateLoss();
        return popBackStackImmediate(null, -1, 0);
    }

    private boolean popBackStackImmediate(String name, int id, int flags) {
        execPendingActions();
        ensureExecReady(true);
        boolean executePop = popBackStackState(this.mTmpRecords, this.mTmpIsPop, name, id, flags);
        if (executePop) {
            this.mExecutingActions = true;
            try {
                optimizeAndExecuteOps(this.mTmpRecords, this.mTmpIsPop);
            } finally {
                cleanupExec();
            }
        }
        doPendingDeferredStart();
        return executePop;
    }

    public void putFragment(Bundle bundle, String key, Fragment fragment) {
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        bundle.putInt(key, fragment.mIndex);
    }

    public Fragment getFragment(Bundle bundle, String key) {
        int index = bundle.getInt(key, -1);
        if (index == -1) {
            return null;
        }
        if (index >= this.mActive.size()) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + key + ": index " + index));
        }
        Fragment f = (Fragment) this.mActive.get(index);
        if (f == null) {
            throwException(new IllegalStateException("Fragment no longer exists for key " + key + ": index " + index));
        }
        return f;
    }

    public SavedState saveFragmentInstanceState(Fragment fragment) {
        SavedState savedState = null;
        if (fragment.mIndex < 0) {
            throwException(new IllegalStateException("Fragment " + fragment + " is not currently in the FragmentManager"));
        }
        if (fragment.mState <= 0) {
            return null;
        }
        Bundle result = saveFragmentBasicState(fragment);
        if (result != null) {
            savedState = new SavedState(result);
        }
        return savedState;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("FragmentManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        if (this.mParent == null) {
            DebugUtils.buildShortClassTag(this.mHost, sb);
        } else {
            DebugUtils.buildShortClassTag(this.mParent, sb);
        }
        sb.append("}}");
        return sb.toString();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        int N;
        int i;
        Fragment f;
        String innerPrefix = prefix + "    ";
        if (this.mActive != null) {
            N = this.mActive.size();
            if (N > 0) {
                writer.print(prefix);
                writer.print("Active Fragments in ");
                writer.print(Integer.toHexString(System.identityHashCode(this)));
                writer.println(":");
                for (i = 0; i < N; i++) {
                    f = (Fragment) this.mActive.get(i);
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.print(": ");
                    writer.println(f);
                    if (f != null) {
                        f.dump(innerPrefix, fd, writer, args);
                    }
                }
            }
        }
        if (this.mAdded != null) {
            N = this.mAdded.size();
            if (N > 0) {
                writer.print(prefix);
                writer.println("Added Fragments:");
                for (i = 0; i < N; i++) {
                    f = (Fragment) this.mAdded.get(i);
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.print(": ");
                    writer.println(f.toString());
                }
            }
        }
        if (this.mCreatedMenus != null) {
            N = this.mCreatedMenus.size();
            if (N > 0) {
                writer.print(prefix);
                writer.println("Fragments Created Menus:");
                for (i = 0; i < N; i++) {
                    f = (Fragment) this.mCreatedMenus.get(i);
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.print(": ");
                    writer.println(f.toString());
                }
            }
        }
        if (this.mBackStack != null) {
            N = this.mBackStack.size();
            if (N > 0) {
                writer.print(prefix);
                writer.println("Back Stack:");
                for (i = 0; i < N; i++) {
                    BackStackRecord bs = (BackStackRecord) this.mBackStack.get(i);
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.print(": ");
                    writer.println(bs.toString());
                    bs.dump(innerPrefix, fd, writer, args);
                }
            }
        }
        synchronized (this) {
            if (this.mBackStackIndices != null) {
                N = this.mBackStackIndices.size();
                if (N > 0) {
                    writer.print(prefix);
                    writer.println("Back Stack Indices:");
                    for (i = 0; i < N; i++) {
                        bs = (BackStackRecord) this.mBackStackIndices.get(i);
                        writer.print(prefix);
                        writer.print("  #");
                        writer.print(i);
                        writer.print(": ");
                        writer.println(bs);
                    }
                }
            }
            if (this.mAvailBackStackIndices != null && this.mAvailBackStackIndices.size() > 0) {
                writer.print(prefix);
                writer.print("mAvailBackStackIndices: ");
                writer.println(Arrays.toString(this.mAvailBackStackIndices.toArray()));
            }
        }
        if (this.mPendingActions != null) {
            N = this.mPendingActions.size();
            if (N > 0) {
                writer.print(prefix);
                writer.println("Pending Actions:");
                for (i = 0; i < N; i++) {
                    OpGenerator r = (OpGenerator) this.mPendingActions.get(i);
                    writer.print(prefix);
                    writer.print("  #");
                    writer.print(i);
                    writer.print(": ");
                    writer.println(r);
                }
            }
        }
        writer.print(prefix);
        writer.println("FragmentManager misc state:");
        writer.print(prefix);
        writer.print("  mHost=");
        writer.println(this.mHost);
        writer.print(prefix);
        writer.print("  mContainer=");
        writer.println(this.mContainer);
        if (this.mParent != null) {
            writer.print(prefix);
            writer.print("  mParent=");
            writer.println(this.mParent);
        }
        writer.print(prefix);
        writer.print("  mCurState=");
        writer.print(this.mCurState);
        writer.print(" mStateSaved=");
        writer.print(this.mStateSaved);
        writer.print(" mDestroyed=");
        writer.println(this.mDestroyed);
        if (this.mNeedMenuInvalidate) {
            writer.print(prefix);
            writer.print("  mNeedMenuInvalidate=");
            writer.println(this.mNeedMenuInvalidate);
        }
        if (this.mNoTransactionsBecause != null) {
            writer.print(prefix);
            writer.print("  mNoTransactionsBecause=");
            writer.println(this.mNoTransactionsBecause);
        }
        if (this.mAvailIndices != null && this.mAvailIndices.size() > 0) {
            writer.print(prefix);
            writer.print("  mAvailIndices: ");
            writer.println(Arrays.toString(this.mAvailIndices.toArray()));
        }
    }

    static Animation makeOpenCloseAnimation(Context context, float startScale, float endScale, float startAlpha, float endAlpha) {
        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scale = new ScaleAnimation(startScale, endScale, startScale, endScale, 1, 0.5f, 1, 0.5f);
        scale.setInterpolator(DECELERATE_QUINT);
        scale.setDuration(220);
        set.addAnimation(scale);
        AlphaAnimation alpha = new AlphaAnimation(startAlpha, endAlpha);
        alpha.setInterpolator(DECELERATE_CUBIC);
        alpha.setDuration(220);
        set.addAnimation(alpha);
        return set;
    }

    static Animation makeFadeAnimation(Context context, float start, float end) {
        AlphaAnimation anim = new AlphaAnimation(start, end);
        anim.setInterpolator(DECELERATE_CUBIC);
        anim.setDuration(220);
        return anim;
    }

    Animation loadAnimation(Fragment fragment, int transit, boolean enter, int transitionStyle) {
        Animation animObj = fragment.onCreateAnimation(transit, enter, fragment.getNextAnim());
        if (animObj != null) {
            return animObj;
        }
        if (fragment.getNextAnim() != 0) {
            Animation anim = AnimationUtils.loadAnimation(this.mHost.getContext(), fragment.getNextAnim());
            if (anim != null) {
                return anim;
            }
        }
        if (transit == 0) {
            return null;
        }
        int styleIndex = transitToStyleIndex(transit, enter);
        if (styleIndex < 0) {
            return null;
        }
        switch (styleIndex) {
            case 1:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.125f, 1.0f, 0.0f, 1.0f);
            case 2:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 0.975f, 1.0f, 0.0f);
            case 3:
                return makeOpenCloseAnimation(this.mHost.getContext(), 0.975f, 1.0f, 0.0f, 1.0f);
            case 4:
                return makeOpenCloseAnimation(this.mHost.getContext(), 1.0f, 1.075f, 1.0f, 0.0f);
            case 5:
                return makeFadeAnimation(this.mHost.getContext(), 0.0f, 1.0f);
            case 6:
                return makeFadeAnimation(this.mHost.getContext(), 1.0f, 0.0f);
            default:
                if (transitionStyle == 0 && this.mHost.onHasWindowAnimations()) {
                    transitionStyle = this.mHost.onGetWindowAnimations();
                }
                return transitionStyle != 0 ? null : null;
        }
    }

    public void performPendingDeferredStart(Fragment f) {
        if (f.mDeferStart) {
            if (this.mExecutingActions) {
                this.mHavePendingDeferredStart = true;
                return;
            }
            f.mDeferStart = false;
            moveToState(f, this.mCurState, 0, 0, false);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setHWLayerAnimListenerIfAlpha(android.view.View r8, android.view.animation.Animation r9) {
        /*
        r7 = this;
        r6 = 0;
        if (r8 != 0) goto L_0x0004;
    L_0x0003:
        return;
    L_0x0004:
        if (r9 == 0) goto L_0x0003;
    L_0x0006:
        r4 = shouldRunOnHWLayer(r8, r9);
        if (r4 != 0) goto L_0x000d;
    L_0x000c:
        return;
    L_0x000d:
        r3 = 0;
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        if (r4 == 0) goto L_0x0029;
    L_0x0012:
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        r4 = r4.get(r9);	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        r0 = r4;
        r0 = (android.view.animation.Animation.AnimationListener) r0;	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        r3 = r0;
    L_0x001c:
        r4 = 2;
        android.support.v4.view.ViewCompat.setLayerType(r8, r4, r6);
        r4 = new android.support.v4.app.FragmentManagerImpl$AnimateOnHWLayerIfNeededListener;
        r4.<init>(r8, r9, r3);
        r9.setAnimationListener(r4);
        goto L_0x000c;
    L_0x0029:
        r4 = android.view.animation.Animation.class;
        r5 = "mListener";
        r4 = r4.getDeclaredField(r5);	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        sAnimationListenerField = r4;	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        r4 = sAnimationListenerField;	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        r5 = 1;
        r4.setAccessible(r5);	 Catch:{ NoSuchFieldException -> 0x003b, IllegalAccessException -> 0x0046 }
        goto L_0x0012;
    L_0x003b:
        r2 = move-exception;
        r4 = "FragmentManager";
        r5 = "No field with the name mListener is found in Animation class";
        android.util.Log.e(r4, r5, r2);
        goto L_0x001c;
    L_0x0046:
        r1 = move-exception;
        r4 = "FragmentManager";
        r5 = "Cannot access Animation's mListener field";
        android.util.Log.e(r4, r5, r1);
        goto L_0x001c;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.setHWLayerAnimListenerIfAlpha(android.view.View, android.view.animation.Animation):void");
    }

    boolean isStateAtLeast(int state) {
        return this.mCurState >= state;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void moveToState(android.support.v4.app.Fragment r18, int r19, int r20, int r21, boolean r22) {
        /*
        r17 = this;
        r0 = r18;
        r4 = r0.mAdded;
        if (r4 != 0) goto L_0x0030;
    L_0x0006:
        r4 = 1;
        r0 = r19;
        if (r0 > r4) goto L_0x0037;
    L_0x000b:
        r0 = r18;
        r4 = r0.mRemoving;
        if (r4 != 0) goto L_0x003a;
    L_0x0011:
        r0 = r18;
        r4 = r0.mDeferStart;
        if (r4 != 0) goto L_0x0049;
    L_0x0017:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 < r0) goto L_0x0058;
    L_0x001f:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 > r0) goto L_0x047b;
    L_0x0027:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r4 != r0) goto L_0x0679;
    L_0x002f:
        return;
    L_0x0030:
        r0 = r18;
        r4 = r0.mDetached;
        if (r4 != 0) goto L_0x0006;
    L_0x0036:
        goto L_0x000b;
    L_0x0037:
        r19 = 1;
        goto L_0x000b;
    L_0x003a:
        r0 = r18;
        r4 = r0.mState;
        r0 = r19;
        if (r0 <= r4) goto L_0x0011;
    L_0x0042:
        r0 = r18;
        r0 = r0.mState;
        r19 = r0;
        goto L_0x0011;
    L_0x0049:
        r0 = r18;
        r4 = r0.mState;
        r5 = 4;
        if (r4 >= r5) goto L_0x0017;
    L_0x0050:
        r4 = 3;
        r0 = r19;
        if (r0 <= r4) goto L_0x0017;
    L_0x0055:
        r19 = 3;
        goto L_0x0017;
    L_0x0058:
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 != 0) goto L_0x012d;
    L_0x005e:
        r4 = r18.getAnimatingAway();
        if (r4 != 0) goto L_0x0134;
    L_0x0064:
        r0 = r18;
        r4 = r0.mState;
        switch(r4) {
            case 0: goto L_0x006c;
            case 1: goto L_0x00fe;
            case 2: goto L_0x0103;
            case 3: goto L_0x0108;
            case 4: goto L_0x010d;
            default: goto L_0x006b;
        };
    L_0x006b:
        goto L_0x0027;
    L_0x006c:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x014a;
    L_0x0070:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        if (r4 != 0) goto L_0x0168;
    L_0x0076:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r0.mHost = r4;
        r0 = r17;
        r4 = r0.mParent;
        r0 = r18;
        r0.mParentFragment = r4;
        r0 = r17;
        r4 = r0.mParent;
        if (r4 != 0) goto L_0x01d6;
    L_0x008c:
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getFragmentManagerImpl();
    L_0x0094:
        r0 = r18;
        r0.mFragmentManager = r4;
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getContext();
        r5 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentPreAttached(r1, r4, r5);
        r4 = 0;
        r0 = r18;
        r0.mCalled = r4;
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getContext();
        r0 = r18;
        r0.onAttach(r4);
        r0 = r18;
        r4 = r0.mCalled;
        if (r4 == 0) goto L_0x01de;
    L_0x00c0:
        r0 = r18;
        r4 = r0.mParentFragment;
        if (r4 == 0) goto L_0x0201;
    L_0x00c6:
        r0 = r18;
        r4 = r0.mParentFragment;
        r0 = r18;
        r4.onAttachFragment(r0);
    L_0x00cf:
        r0 = r17;
        r4 = r0.mHost;
        r4 = r4.getContext();
        r5 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentAttached(r1, r4, r5);
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 == 0) goto L_0x020c;
    L_0x00e5:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.restoreChildFragmentState(r4);
        r4 = 1;
        r0 = r18;
        r0.mState = r4;
    L_0x00f3:
        r4 = 0;
        r0 = r18;
        r0.mRetaining = r4;
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 != 0) goto L_0x0223;
    L_0x00fe:
        r4 = 1;
        r0 = r19;
        if (r0 > r4) goto L_0x029b;
    L_0x0103:
        r4 = 2;
        r0 = r19;
        if (r0 > r4) goto L_0x0428;
    L_0x0108:
        r4 = 3;
        r0 = r19;
        if (r0 > r4) goto L_0x042f;
    L_0x010d:
        r4 = 4;
        r0 = r19;
        if (r0 <= r4) goto L_0x0027;
    L_0x0112:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x045d;
    L_0x0116:
        r18.performResume();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentResumed(r1, r4);
        r4 = 0;
        r0 = r18;
        r0.mSavedFragmentState = r4;
        r4 = 0;
        r0 = r18;
        r0.mSavedViewState = r4;
        goto L_0x0027;
    L_0x012d:
        r0 = r18;
        r4 = r0.mInLayout;
        if (r4 != 0) goto L_0x005e;
    L_0x0133:
        return;
    L_0x0134:
        r4 = 0;
        r0 = r18;
        r0.setAnimatingAway(r4);
        r6 = r18.getStateAfterAnimating();
        r7 = 0;
        r8 = 0;
        r9 = 1;
        r4 = r17;
        r5 = r18;
        r4.moveToState(r5, r6, r7, r8, r9);
        goto L_0x0064;
    L_0x014a:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0070;
    L_0x0168:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r17;
        r5 = r0.mHost;
        r5 = r5.getContext();
        r5 = r5.getClassLoader();
        r4.setClassLoader(r5);
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:view_state";
        r4 = r4.getSparseParcelableArray(r5);
        r0 = r18;
        r0.mSavedViewState = r4;
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:target_state";
        r0 = r17;
        r4 = r0.getFragment(r4, r5);
        r0 = r18;
        r0.mTarget = r4;
        r0 = r18;
        r4 = r0.mTarget;
        if (r4 != 0) goto L_0x01c5;
    L_0x01a1:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:user_visible_hint";
        r6 = 1;
        r4 = r4.getBoolean(r5, r6);
        r0 = r18;
        r0.mUserVisibleHint = r4;
        r0 = r18;
        r4 = r0.mUserVisibleHint;
        if (r4 != 0) goto L_0x0076;
    L_0x01b7:
        r4 = 1;
        r0 = r18;
        r0.mDeferStart = r4;
        r4 = 3;
        r0 = r19;
        if (r0 <= r4) goto L_0x0076;
    L_0x01c1:
        r19 = 3;
        goto L_0x0076;
    L_0x01c5:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = "android:target_req_state";
        r6 = 0;
        r4 = r4.getInt(r5, r6);
        r0 = r18;
        r0.mTargetRequestCode = r4;
        goto L_0x01a1;
    L_0x01d6:
        r0 = r17;
        r4 = r0.mParent;
        r4 = r4.mChildFragmentManager;
        goto L_0x0094;
    L_0x01de:
        r4 = new android.support.v4.app.SuperNotCalledException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " did not call through to super.onAttach()";
        r5 = r5.append(r6);
        r5 = r5.toString();
        r4.<init>(r5);
        throw r4;
    L_0x0201:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r4.onAttachFragment(r0);
        goto L_0x00cf;
    L_0x020c:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.performCreate(r4);
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentCreated(r1, r4, r5);
        goto L_0x00f3;
    L_0x0223:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.getLayoutInflater(r4);
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r6 = 0;
        r0 = r18;
        r4 = r0.performCreateView(r4, r6, r5);
        r0 = r18;
        r0.mView = r4;
        r0 = r18;
        r4 = r0.mView;
        if (r4 != 0) goto L_0x0249;
    L_0x0242:
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x00fe;
    L_0x0249:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.mInnerView = r4;
        r4 = android.os.Build.VERSION.SDK_INT;
        r5 = 11;
        if (r4 >= r5) goto L_0x0288;
    L_0x0257:
        r0 = r18;
        r4 = r0.mView;
        r4 = android.support.v4.app.NoSaveStateFrameLayout.wrap(r4);
        r0 = r18;
        r0.mView = r4;
    L_0x0263:
        r0 = r18;
        r4 = r0.mHidden;
        if (r4 != 0) goto L_0x0291;
    L_0x0269:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r0.onViewCreated(r4, r5);
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r6 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentViewCreated(r1, r4, r5, r6);
        goto L_0x00fe;
    L_0x0288:
        r0 = r18;
        r4 = r0.mView;
        r5 = 0;
        android.support.v4.view.ViewCompat.setSaveFromParentEnabled(r4, r5);
        goto L_0x0263;
    L_0x0291:
        r0 = r18;
        r4 = r0.mView;
        r5 = 8;
        r4.setVisibility(r5);
        goto L_0x0269;
    L_0x029b:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x02c7;
    L_0x029f:
        r0 = r18;
        r4 = r0.mFromLayout;
        if (r4 == 0) goto L_0x02e4;
    L_0x02a5:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.performActivityCreated(r4);
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r5 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentActivityCreated(r1, r4, r5);
        r0 = r18;
        r4 = r0.mView;
        if (r4 != 0) goto L_0x041d;
    L_0x02c0:
        r4 = 0;
        r0 = r18;
        r0.mSavedFragmentState = r4;
        goto L_0x0103;
    L_0x02c7:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto ACTIVITY_CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x029f;
    L_0x02e4:
        r11 = 0;
        r0 = r18;
        r4 = r0.mContainerId;
        if (r4 != 0) goto L_0x0313;
    L_0x02eb:
        r0 = r18;
        r0.mContainer = r11;
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.getLayoutInflater(r4);
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r4 = r0.performCreateView(r4, r11, r5);
        r0 = r18;
        r0.mView = r4;
        r0 = r18;
        r4 = r0.mView;
        if (r4 != 0) goto L_0x03aa;
    L_0x030d:
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x02a5;
    L_0x0313:
        r0 = r18;
        r4 = r0.mContainerId;
        r5 = -1;
        if (r4 == r5) goto L_0x037c;
    L_0x031a:
        r0 = r17;
        r4 = r0.mContainer;
        r0 = r18;
        r5 = r0.mContainerId;
        r11 = r4.onFindViewById(r5);
        r11 = (android.view.ViewGroup) r11;
        if (r11 != 0) goto L_0x02eb;
    L_0x032a:
        r0 = r18;
        r4 = r0.mRestored;
        if (r4 != 0) goto L_0x02eb;
    L_0x0330:
        r4 = r18.getResources();	 Catch:{ NotFoundException -> 0x03a5 }
        r0 = r18;
        r5 = r0.mContainerId;	 Catch:{ NotFoundException -> 0x03a5 }
        r14 = r4.getResourceName(r5);	 Catch:{ NotFoundException -> 0x03a5 }
    L_0x033c:
        r4 = new java.lang.IllegalArgumentException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "No view found for id 0x";
        r5 = r5.append(r6);
        r0 = r18;
        r6 = r0.mContainerId;
        r6 = java.lang.Integer.toHexString(r6);
        r5 = r5.append(r6);
        r6 = " (";
        r5 = r5.append(r6);
        r5 = r5.append(r14);
        r6 = ") for fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r4.<init>(r5);
        r0 = r17;
        r0.throwException(r4);
        goto L_0x02eb;
    L_0x037c:
        r4 = new java.lang.IllegalArgumentException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Cannot create fragment ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " for a container view with no id";
        r5 = r5.append(r6);
        r5 = r5.toString();
        r4.<init>(r5);
        r0 = r17;
        r0.throwException(r4);
        goto L_0x031a;
    L_0x03a5:
        r12 = move-exception;
        r14 = "unknown";
        goto L_0x033c;
    L_0x03aa:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.mInnerView = r4;
        r4 = android.os.Build.VERSION.SDK_INT;
        r5 = 11;
        if (r4 >= r5) goto L_0x03fa;
    L_0x03b8:
        r0 = r18;
        r4 = r0.mView;
        r4 = android.support.v4.app.NoSaveStateFrameLayout.wrap(r4);
        r0 = r18;
        r0.mView = r4;
    L_0x03c4:
        if (r11 != 0) goto L_0x0403;
    L_0x03c6:
        r0 = r18;
        r4 = r0.mHidden;
        if (r4 != 0) goto L_0x040b;
    L_0x03cc:
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r0 = r18;
        r0.onViewCreated(r4, r5);
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r5 = r0.mSavedFragmentState;
        r6 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentViewCreated(r1, r4, r5, r6);
        r0 = r18;
        r4 = r0.mView;
        r4 = r4.getVisibility();
        if (r4 == 0) goto L_0x0415;
    L_0x03f3:
        r4 = 0;
    L_0x03f4:
        r0 = r18;
        r0.mIsNewlyAdded = r4;
        goto L_0x02a5;
    L_0x03fa:
        r0 = r18;
        r4 = r0.mView;
        r5 = 0;
        android.support.v4.view.ViewCompat.setSaveFromParentEnabled(r4, r5);
        goto L_0x03c4;
    L_0x0403:
        r0 = r18;
        r4 = r0.mView;
        r11.addView(r4);
        goto L_0x03c6;
    L_0x040b:
        r0 = r18;
        r4 = r0.mView;
        r5 = 8;
        r4.setVisibility(r5);
        goto L_0x03cc;
    L_0x0415:
        r0 = r18;
        r4 = r0.mContainer;
        if (r4 == 0) goto L_0x03f3;
    L_0x041b:
        r4 = 1;
        goto L_0x03f4;
    L_0x041d:
        r0 = r18;
        r4 = r0.mSavedFragmentState;
        r0 = r18;
        r0.restoreViewState(r4);
        goto L_0x02c0;
    L_0x0428:
        r4 = 3;
        r0 = r18;
        r0.mState = r4;
        goto L_0x0108;
    L_0x042f:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x0440;
    L_0x0433:
        r18.performStart();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentStarted(r1, r4);
        goto L_0x010d;
    L_0x0440:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto STARTED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0433;
    L_0x045d:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveto RESUMED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0116;
    L_0x047b:
        r0 = r18;
        r4 = r0.mState;
        switch(r4) {
            case 1: goto L_0x0484;
            case 2: goto L_0x04d7;
            case 3: goto L_0x04d2;
            case 4: goto L_0x04cd;
            case 5: goto L_0x04c8;
            default: goto L_0x0482;
        };
    L_0x0482:
        goto L_0x0027;
    L_0x0484:
        r4 = 1;
        r0 = r19;
        if (r0 >= r4) goto L_0x0027;
    L_0x0489:
        r0 = r17;
        r4 = r0.mDestroyed;
        if (r4 != 0) goto L_0x062d;
    L_0x048f:
        r4 = r18.getAnimatingAway();
        if (r4 != 0) goto L_0x0642;
    L_0x0495:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x0649;
    L_0x0499:
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 == 0) goto L_0x0667;
    L_0x049f:
        r4 = 0;
        r0 = r18;
        r0.mState = r4;
    L_0x04a4:
        r18.performDetach();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentDetached(r1, r4);
        if (r22 != 0) goto L_0x0027;
    L_0x04b1:
        r0 = r18;
        r4 = r0.mRetaining;
        if (r4 == 0) goto L_0x0674;
    L_0x04b7:
        r4 = 0;
        r0 = r18;
        r0.mHost = r4;
        r4 = 0;
        r0 = r18;
        r0.mParentFragment = r4;
        r4 = 0;
        r0 = r18;
        r0.mFragmentManager = r4;
        goto L_0x0027;
    L_0x04c8:
        r4 = 5;
        r0 = r19;
        if (r0 < r4) goto L_0x0508;
    L_0x04cd:
        r4 = 4;
        r0 = r19;
        if (r0 < r4) goto L_0x0535;
    L_0x04d2:
        r4 = 3;
        r0 = r19;
        if (r0 < r4) goto L_0x0562;
    L_0x04d7:
        r4 = 2;
        r0 = r19;
        if (r0 >= r4) goto L_0x0484;
    L_0x04dc:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x0588;
    L_0x04e0:
        r0 = r18;
        r4 = r0.mView;
        if (r4 != 0) goto L_0x05a6;
    L_0x04e6:
        r18.performDestroyView();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentViewDestroyed(r1, r4);
        r0 = r18;
        r4 = r0.mView;
        if (r4 != 0) goto L_0x05bd;
    L_0x04f7:
        r4 = 0;
        r0 = r18;
        r0.mContainer = r4;
        r4 = 0;
        r0 = r18;
        r0.mView = r4;
        r4 = 0;
        r0 = r18;
        r0.mInnerView = r4;
        goto L_0x0484;
    L_0x0508:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x0518;
    L_0x050c:
        r18.performPause();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentPaused(r1, r4);
        goto L_0x04cd;
    L_0x0518:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom RESUMED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x050c;
    L_0x0535:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x0545;
    L_0x0539:
        r18.performStop();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentStopped(r1, r4);
        goto L_0x04d2;
    L_0x0545:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom STARTED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0539;
    L_0x0562:
        r4 = DEBUG;
        if (r4 != 0) goto L_0x056b;
    L_0x0566:
        r18.performReallyStop();
        goto L_0x04d7;
    L_0x056b:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom STOPPED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0566;
    L_0x0588:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom ACTIVITY_CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x04e0;
    L_0x05a6:
        r0 = r17;
        r4 = r0.mHost;
        r0 = r18;
        r4 = r4.onShouldSaveFragmentState(r0);
        if (r4 == 0) goto L_0x04e6;
    L_0x05b2:
        r0 = r18;
        r4 = r0.mSavedViewState;
        if (r4 != 0) goto L_0x04e6;
    L_0x05b8:
        r17.saveFragmentViewState(r18);
        goto L_0x04e6;
    L_0x05bd:
        r0 = r18;
        r4 = r0.mContainer;
        if (r4 == 0) goto L_0x04f7;
    L_0x05c3:
        r10 = 0;
        r0 = r17;
        r4 = r0.mCurState;
        if (r4 > 0) goto L_0x05de;
    L_0x05ca:
        r4 = 0;
        r0 = r18;
        r0.mPostponedAlpha = r4;
        if (r10 != 0) goto L_0x0605;
    L_0x05d1:
        r0 = r18;
        r4 = r0.mContainer;
        r0 = r18;
        r5 = r0.mView;
        r4.removeView(r5);
        goto L_0x04f7;
    L_0x05de:
        r0 = r17;
        r4 = r0.mDestroyed;
        if (r4 != 0) goto L_0x05ca;
    L_0x05e4:
        r0 = r18;
        r4 = r0.mView;
        r4 = r4.getVisibility();
        if (r4 != 0) goto L_0x05ca;
    L_0x05ee:
        r0 = r18;
        r4 = r0.mPostponedAlpha;
        r5 = 0;
        r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1));
        if (r4 < 0) goto L_0x05ca;
    L_0x05f7:
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r2 = r20;
        r3 = r21;
        r10 = r0.loadAnimation(r1, r2, r4, r3);
        goto L_0x05ca;
    L_0x0605:
        r13 = r18;
        r0 = r18;
        r4 = r0.mView;
        r0 = r18;
        r0.setAnimatingAway(r4);
        r18.setStateAfterAnimating(r19);
        r0 = r18;
        r0 = r0.mView;
        r16 = r0;
        r4 = new android.support.v4.app.FragmentManagerImpl$2;
        r0 = r17;
        r1 = r16;
        r4.<init>(r1, r10, r13);
        r10.setAnimationListener(r4);
        r0 = r18;
        r4 = r0.mView;
        r4.startAnimation(r10);
        goto L_0x05d1;
    L_0x062d:
        r4 = r18.getAnimatingAway();
        if (r4 == 0) goto L_0x048f;
    L_0x0633:
        r15 = r18.getAnimatingAway();
        r4 = 0;
        r0 = r18;
        r0.setAnimatingAway(r4);
        r15.clearAnimation();
        goto L_0x048f;
    L_0x0642:
        r18.setStateAfterAnimating(r19);
        r19 = 1;
        goto L_0x0027;
    L_0x0649:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "movefrom CREATED: ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r5 = r5.toString();
        android.util.Log.v(r4, r5);
        goto L_0x0499;
    L_0x0667:
        r18.performDestroy();
        r4 = 0;
        r0 = r17;
        r1 = r18;
        r0.dispatchOnFragmentDestroyed(r1, r4);
        goto L_0x04a4;
    L_0x0674:
        r17.makeInactive(r18);
        goto L_0x0027;
    L_0x0679:
        r4 = "FragmentManager";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "moveToState: Fragment state for ";
        r5 = r5.append(r6);
        r0 = r18;
        r5 = r5.append(r0);
        r6 = " not updated inline; ";
        r5 = r5.append(r6);
        r6 = "expected state ";
        r5 = r5.append(r6);
        r0 = r19;
        r5 = r5.append(r0);
        r6 = " found ";
        r5 = r5.append(r6);
        r0 = r18;
        r6 = r0.mState;
        r5 = r5.append(r6);
        r5 = r5.toString();
        android.util.Log.w(r4, r5);
        r0 = r19;
        r1 = r18;
        r1.mState = r0;
        goto L_0x002f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.moveToState(android.support.v4.app.Fragment, int, int, int, boolean):void");
    }

    void moveToState(Fragment f) {
        moveToState(f, this.mCurState, 0, 0, false);
    }

    void completeShowHideFragment(Fragment fragment) {
        if (fragment.mView != null) {
            boolean z;
            int nextTransition = fragment.getNextTransition();
            if (fragment.mHidden) {
                z = false;
            } else {
                z = true;
            }
            Animation anim = loadAnimation(fragment, nextTransition, z, fragment.getNextTransitionStyle());
            if (anim != null) {
                setHWLayerAnimListenerIfAlpha(fragment.mView, anim);
                fragment.mView.startAnimation(anim);
                setHWLayerAnimListenerIfAlpha(fragment.mView, anim);
                anim.start();
            }
            int visibility = (fragment.mHidden && !fragment.isHideReplaced()) ? 8 : 0;
            fragment.mView.setVisibility(visibility);
            if (fragment.isHideReplaced()) {
                fragment.setHideReplaced(false);
            }
        }
        if (fragment.mAdded && fragment.mHasMenu && fragment.mMenuVisible) {
            this.mNeedMenuInvalidate = true;
        }
        fragment.mHiddenChanged = false;
        fragment.onHiddenChanged(fragment.mHidden);
    }

    void moveFragmentToExpectedState(Fragment f) {
        if (f != null) {
            int nextState = this.mCurState;
            if (f.mRemoving) {
                if (f.isInBackStack()) {
                    nextState = Math.min(nextState, 1);
                } else {
                    nextState = Math.min(nextState, 0);
                }
            }
            moveToState(f, nextState, f.getNextTransition(), f.getNextTransitionStyle(), false);
            if (f.mView != null) {
                Fragment underFragment = findFragmentUnder(f);
                if (underFragment != null) {
                    View underView = underFragment.mView;
                    ViewGroup container = f.mContainer;
                    int underIndex = container.indexOfChild(underView);
                    int viewIndex = container.indexOfChild(f.mView);
                    if (viewIndex < underIndex) {
                        container.removeViewAt(viewIndex);
                        container.addView(f.mView, underIndex);
                    }
                }
                if (f.mIsNewlyAdded && f.mContainer != null) {
                    if (VERSION.SDK_INT < 11) {
                        f.mView.setVisibility(0);
                    } else if (f.mPostponedAlpha > 0.0f) {
                        f.mView.setAlpha(f.mPostponedAlpha);
                    }
                    f.mPostponedAlpha = 0.0f;
                    f.mIsNewlyAdded = false;
                    Animation anim = loadAnimation(f, f.getNextTransition(), true, f.getNextTransitionStyle());
                    if (anim != null) {
                        setHWLayerAnimListenerIfAlpha(f.mView, anim);
                        f.mView.startAnimation(anim);
                    }
                }
            }
            if (f.mHiddenChanged) {
                completeShowHideFragment(f);
            }
        }
    }

    void moveToState(int newState, boolean always) {
        if (this.mHost == null && newState != 0) {
            throw new IllegalStateException("No activity");
        } else if (always || newState != this.mCurState) {
            this.mCurState = newState;
            if (this.mActive != null) {
                int i;
                Fragment f;
                int loadersRunning = 0;
                if (this.mAdded != null) {
                    int numAdded = this.mAdded.size();
                    for (i = 0; i < numAdded; i++) {
                        f = (Fragment) this.mAdded.get(i);
                        moveFragmentToExpectedState(f);
                        if (f.mLoaderManager != null) {
                            loadersRunning |= f.mLoaderManager.hasRunningLoaders();
                        }
                    }
                }
                int numActive = this.mActive.size();
                for (i = 0; i < numActive; i++) {
                    f = (Fragment) this.mActive.get(i);
                    if (f != null) {
                        if (f.mRemoving || f.mDetached) {
                            if (!f.mIsNewlyAdded) {
                                moveFragmentToExpectedState(f);
                                if (f.mLoaderManager != null) {
                                    loadersRunning |= f.mLoaderManager.hasRunningLoaders();
                                }
                            }
                        }
                    }
                }
                if (loadersRunning == 0) {
                    startPendingDeferredFragments();
                }
                if (this.mNeedMenuInvalidate && this.mHost != null && this.mCurState == 5) {
                    this.mHost.onSupportInvalidateOptionsMenu();
                    this.mNeedMenuInvalidate = false;
                }
            }
        }
    }

    void startPendingDeferredFragments() {
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = (Fragment) this.mActive.get(i);
                if (f != null) {
                    performPendingDeferredStart(f);
                }
            }
        }
    }

    void makeActive(Fragment f) {
        if (f.mIndex < 0) {
            if (this.mAvailIndices != null && this.mAvailIndices.size() > 0) {
                f.setIndex(((Integer) this.mAvailIndices.remove(this.mAvailIndices.size() - 1)).intValue(), this.mParent);
                this.mActive.set(f.mIndex, f);
            } else {
                if (this.mActive == null) {
                    this.mActive = new ArrayList();
                }
                f.setIndex(this.mActive.size(), this.mParent);
                this.mActive.add(f);
            }
            if (DEBUG) {
                Log.v("FragmentManager", "Allocated fragment index " + f);
            }
        }
    }

    void makeInactive(Fragment f) {
        if (f.mIndex >= 0) {
            if (DEBUG) {
                Log.v("FragmentManager", "Freeing fragment index " + f);
            }
            this.mActive.set(f.mIndex, null);
            if (this.mAvailIndices == null) {
                this.mAvailIndices = new ArrayList();
            }
            this.mAvailIndices.add(Integer.valueOf(f.mIndex));
            this.mHost.inactivateFragment(f.mWho);
            f.initState();
        }
    }

    public void addFragment(Fragment fragment, boolean moveToStateNow) {
        if (this.mAdded == null) {
            this.mAdded = new ArrayList();
        }
        if (DEBUG) {
            Log.v("FragmentManager", "add: " + fragment);
        }
        makeActive(fragment);
        if (!fragment.mDetached) {
            if (this.mAdded.contains(fragment)) {
                throw new IllegalStateException("Fragment already added: " + fragment);
            }
            this.mAdded.add(fragment);
            fragment.mAdded = true;
            fragment.mRemoving = false;
            if (fragment.mView == null) {
                fragment.mHiddenChanged = false;
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            if (moveToStateNow) {
                moveToState(fragment);
            }
        }
    }

    public void removeFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "remove: " + fragment + " nesting=" + fragment.mBackStackNesting);
        }
        boolean inactive;
        if (fragment.isInBackStack()) {
            inactive = false;
        } else {
            inactive = true;
        }
        if (!fragment.mDetached || inactive) {
            if (this.mAdded != null) {
                this.mAdded.remove(fragment);
            }
            if (fragment.mHasMenu && fragment.mMenuVisible) {
                this.mNeedMenuInvalidate = true;
            }
            fragment.mAdded = false;
            fragment.mRemoving = true;
        }
    }

    public void hideFragment(Fragment fragment) {
        boolean z = false;
        if (DEBUG) {
            Log.v("FragmentManager", "hide: " + fragment);
        }
        if (!fragment.mHidden) {
            fragment.mHidden = true;
            if (!fragment.mHiddenChanged) {
                z = true;
            }
            fragment.mHiddenChanged = z;
        }
    }

    public void showFragment(Fragment fragment) {
        boolean z = false;
        if (DEBUG) {
            Log.v("FragmentManager", "show: " + fragment);
        }
        if (fragment.mHidden) {
            fragment.mHidden = false;
            if (!fragment.mHiddenChanged) {
                z = true;
            }
            fragment.mHiddenChanged = z;
        }
    }

    public void detachFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "detach: " + fragment);
        }
        if (!fragment.mDetached) {
            fragment.mDetached = true;
            if (fragment.mAdded) {
                if (this.mAdded != null) {
                    if (DEBUG) {
                        Log.v("FragmentManager", "remove from detach: " + fragment);
                    }
                    this.mAdded.remove(fragment);
                }
                if (fragment.mHasMenu && fragment.mMenuVisible) {
                    this.mNeedMenuInvalidate = true;
                }
                fragment.mAdded = false;
            }
        }
    }

    public void attachFragment(Fragment fragment) {
        if (DEBUG) {
            Log.v("FragmentManager", "attach: " + fragment);
        }
        if (fragment.mDetached) {
            fragment.mDetached = false;
            if (!fragment.mAdded) {
                if (this.mAdded == null) {
                    this.mAdded = new ArrayList();
                }
                if (this.mAdded.contains(fragment)) {
                    throw new IllegalStateException("Fragment already added: " + fragment);
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "add from attach: " + fragment);
                }
                this.mAdded.add(fragment);
                fragment.mAdded = true;
                if (fragment.mHasMenu && fragment.mMenuVisible) {
                    this.mNeedMenuInvalidate = true;
                }
            }
        }
    }

    public Fragment findFragmentById(int id) {
        int i;
        Fragment f;
        if (this.mAdded != null) {
            for (i = this.mAdded.size() - 1; i >= 0; i--) {
                f = (Fragment) this.mAdded.get(i);
                if (f != null && f.mFragmentId == id) {
                    return f;
                }
            }
        }
        if (this.mActive != null) {
            for (i = this.mActive.size() - 1; i >= 0; i--) {
                f = (Fragment) this.mActive.get(i);
                if (f != null && f.mFragmentId == id) {
                    return f;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByTag(String tag) {
        int i;
        Fragment f;
        if (!(this.mAdded == null || tag == null)) {
            for (i = this.mAdded.size() - 1; i >= 0; i--) {
                f = (Fragment) this.mAdded.get(i);
                if (f != null && tag.equals(f.mTag)) {
                    return f;
                }
            }
        }
        if (!(this.mActive == null || tag == null)) {
            for (i = this.mActive.size() - 1; i >= 0; i--) {
                f = (Fragment) this.mActive.get(i);
                if (f != null && tag.equals(f.mTag)) {
                    return f;
                }
            }
        }
        return null;
    }

    public Fragment findFragmentByWho(String who) {
        if (!(this.mActive == null || who == null)) {
            for (int i = this.mActive.size() - 1; i >= 0; i--) {
                Fragment f = (Fragment) this.mActive.get(i);
                if (f != null) {
                    f = f.findFragmentByWho(who);
                    if (f != null) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    private void checkStateLoss() {
        if (this.mStateSaved) {
            throw new IllegalStateException("Can not perform this action after onSaveInstanceState");
        } else if (this.mNoTransactionsBecause != null) {
            throw new IllegalStateException("Can not perform this action inside of " + this.mNoTransactionsBecause);
        }
    }

    private void scheduleCommit() {
        synchronized (this) {
            boolean postponeReady;
            if (this.mPostponedTransactions == null || this.mPostponedTransactions.isEmpty()) {
                postponeReady = false;
            } else {
                postponeReady = true;
            }
            boolean pendingReady = this.mPendingActions != null && this.mPendingActions.size() == 1;
            if (postponeReady || pendingReady) {
                this.mHost.getHandler().removeCallbacks(this.mExecCommit);
                this.mHost.getHandler().post(this.mExecCommit);
            }
        }
    }

    public void setBackStackIndex(int index, BackStackRecord bse) {
        synchronized (this) {
            if (this.mBackStackIndices == null) {
                this.mBackStackIndices = new ArrayList();
            }
            int N = this.mBackStackIndices.size();
            if (index >= N) {
                while (N < index) {
                    this.mBackStackIndices.add(null);
                    if (this.mAvailBackStackIndices == null) {
                        this.mAvailBackStackIndices = new ArrayList();
                    }
                    if (DEBUG) {
                        Log.v("FragmentManager", "Adding available back stack index " + N);
                    }
                    this.mAvailBackStackIndices.add(Integer.valueOf(N));
                    N++;
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "Adding back stack index " + index + " with " + bse);
                }
                this.mBackStackIndices.add(bse);
            } else {
                if (DEBUG) {
                    Log.v("FragmentManager", "Setting back stack index " + index + " to " + bse);
                }
                this.mBackStackIndices.set(index, bse);
            }
        }
    }

    public void freeBackStackIndex(int index) {
        synchronized (this) {
            this.mBackStackIndices.set(index, null);
            if (this.mAvailBackStackIndices == null) {
                this.mAvailBackStackIndices = new ArrayList();
            }
            if (DEBUG) {
                Log.v("FragmentManager", "Freeing back stack index " + index);
            }
            this.mAvailBackStackIndices.add(Integer.valueOf(index));
        }
    }

    private void ensureExecReady(boolean allowStateLoss) {
        if (this.mExecutingActions) {
            throw new IllegalStateException("FragmentManager is already executing transactions");
        } else if (Looper.myLooper() == this.mHost.getHandler().getLooper()) {
            if (!allowStateLoss) {
                checkStateLoss();
            }
            if (this.mTmpRecords == null) {
                this.mTmpRecords = new ArrayList();
                this.mTmpIsPop = new ArrayList();
            }
            this.mExecutingActions = true;
            try {
                executePostponedTransaction(null, null);
            } finally {
                this.mExecutingActions = false;
            }
        } else {
            throw new IllegalStateException("Must be called from main thread of fragment host");
        }
    }

    public void execSingleAction(OpGenerator action, boolean allowStateLoss) {
        ensureExecReady(allowStateLoss);
        if (action.generateOps(this.mTmpRecords, this.mTmpIsPop)) {
            this.mExecutingActions = true;
            try {
                optimizeAndExecuteOps(this.mTmpRecords, this.mTmpIsPop);
            } finally {
                cleanupExec();
            }
        }
        doPendingDeferredStart();
    }

    private void cleanupExec() {
        this.mExecutingActions = false;
        this.mTmpIsPop.clear();
        this.mTmpRecords.clear();
    }

    public boolean execPendingActions() {
        ensureExecReady(true);
        boolean didSomething = false;
        while (generateOpsForPendingActions(this.mTmpRecords, this.mTmpIsPop)) {
            this.mExecutingActions = true;
            try {
                optimizeAndExecuteOps(this.mTmpRecords, this.mTmpIsPop);
                cleanupExec();
                didSomething = true;
            } catch (Throwable th) {
                cleanupExec();
                throw th;
            }
        }
        doPendingDeferredStart();
        return didSomething;
    }

    private void executePostponedTransaction(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        int numPostponed;
        if (this.mPostponedTransactions != null) {
            numPostponed = this.mPostponedTransactions.size();
        } else {
            numPostponed = 0;
        }
        int i = 0;
        while (i < numPostponed) {
            int index;
            StartEnterTransitionListener listener = (StartEnterTransitionListener) this.mPostponedTransactions.get(i);
            if (!(records == null || listener.mIsBack)) {
                index = records.indexOf(listener.mRecord);
                if (index != -1 && ((Boolean) isRecordPop.get(index)).booleanValue()) {
                    listener.cancelTransaction();
                    i++;
                }
            }
            if (!listener.isReady()) {
                if (records != null) {
                    if (!listener.mRecord.interactsWith(records, 0, records.size())) {
                    }
                }
                i++;
            }
            this.mPostponedTransactions.remove(i);
            i--;
            numPostponed--;
            if (!(records == null || listener.mIsBack)) {
                index = records.indexOf(listener.mRecord);
                if (index != -1 && ((Boolean) isRecordPop.get(index)).booleanValue()) {
                    listener.cancelTransaction();
                    i++;
                }
            }
            listener.completeTransaction();
            i++;
        }
    }

    private void optimizeAndExecuteOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop) {
        if (records != null && !records.isEmpty()) {
            if (isRecordPop != null && records.size() == isRecordPop.size()) {
                executePostponedTransaction(records, isRecordPop);
                int numRecords = records.size();
                int startIndex = 0;
                int recordNum = 0;
                while (recordNum < numRecords) {
                    if (!((BackStackRecord) records.get(recordNum)).mAllowOptimization) {
                        if (startIndex != recordNum) {
                            executeOpsTogether(records, isRecordPop, startIndex, recordNum);
                        }
                        int optimizeEnd = recordNum + 1;
                        if (((Boolean) isRecordPop.get(recordNum)).booleanValue()) {
                            while (optimizeEnd < numRecords && ((Boolean) isRecordPop.get(optimizeEnd)).booleanValue()) {
                                if (((BackStackRecord) records.get(optimizeEnd)).mAllowOptimization) {
                                    break;
                                }
                                optimizeEnd++;
                            }
                        }
                        executeOpsTogether(records, isRecordPop, recordNum, optimizeEnd);
                        startIndex = optimizeEnd;
                        recordNum = optimizeEnd - 1;
                    }
                    recordNum++;
                }
                if (startIndex != numRecords) {
                    executeOpsTogether(records, isRecordPop, startIndex, numRecords);
                }
                return;
            }
            throw new IllegalStateException("Internal error with the back stack records");
        }
    }

    private void executeOpsTogether(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        int recordNum;
        boolean allowOptimization = ((BackStackRecord) records.get(startIndex)).mAllowOptimization;
        boolean addToBackStack = false;
        if (this.mTmpAddedFragments != null) {
            this.mTmpAddedFragments.clear();
        } else {
            this.mTmpAddedFragments = new ArrayList();
        }
        if (this.mAdded != null) {
            this.mTmpAddedFragments.addAll(this.mAdded);
        }
        for (recordNum = startIndex; recordNum < endIndex; recordNum++) {
            BackStackRecord record = (BackStackRecord) records.get(recordNum);
            if (((Boolean) isRecordPop.get(recordNum)).booleanValue()) {
                record.trackAddedFragmentsInPop(this.mTmpAddedFragments);
            } else {
                record.expandReplaceOps(this.mTmpAddedFragments);
            }
            if (addToBackStack || record.mAddToBackStack) {
                addToBackStack = true;
            } else {
                addToBackStack = false;
            }
        }
        this.mTmpAddedFragments.clear();
        if (!allowOptimization) {
            FragmentTransition.startTransitions(this, records, isRecordPop, startIndex, endIndex, false);
        }
        executeOps(records, isRecordPop, startIndex, endIndex);
        int postponeIndex = endIndex;
        if (allowOptimization) {
            ArraySet<Fragment> addedFragments = new ArraySet();
            addAddedFragments(addedFragments);
            postponeIndex = postponePostponableTransactions(records, isRecordPop, startIndex, endIndex, addedFragments);
            makeRemovedFragmentsInvisible(addedFragments);
        }
        if (postponeIndex != startIndex && allowOptimization) {
            FragmentTransition.startTransitions(this, records, isRecordPop, startIndex, postponeIndex, true);
            moveToState(this.mCurState, true);
        }
        for (recordNum = startIndex; recordNum < endIndex; recordNum++) {
            record = (BackStackRecord) records.get(recordNum);
            if (((Boolean) isRecordPop.get(recordNum)).booleanValue() && record.mIndex >= 0) {
                freeBackStackIndex(record.mIndex);
                record.mIndex = -1;
            }
        }
        if (addToBackStack) {
            reportBackStackChanged();
        }
    }

    private void makeRemovedFragmentsInvisible(ArraySet<Fragment> fragments) {
        int numAdded = fragments.size();
        for (int i = 0; i < numAdded; i++) {
            Fragment fragment = (Fragment) fragments.valueAt(i);
            if (!fragment.mAdded) {
                View view = fragment.getView();
                if (VERSION.SDK_INT >= 11) {
                    fragment.mPostponedAlpha = view.getAlpha();
                    view.setAlpha(0.0f);
                } else {
                    fragment.getView().setVisibility(4);
                }
            }
        }
    }

    private int postponePostponableTransactions(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex, ArraySet<Fragment> added) {
        int postponeIndex = endIndex;
        int i = endIndex - 1;
        while (i >= startIndex) {
            boolean isPostponed;
            BackStackRecord record = (BackStackRecord) records.get(i);
            boolean isPop = ((Boolean) isRecordPop.get(i)).booleanValue();
            if (record.isPostponed() && !record.interactsWith(records, i + 1, endIndex)) {
                isPostponed = true;
            } else {
                isPostponed = false;
            }
            if (isPostponed) {
                if (this.mPostponedTransactions == null) {
                    this.mPostponedTransactions = new ArrayList();
                }
                StartEnterTransitionListener listener = new StartEnterTransitionListener(record, isPop);
                this.mPostponedTransactions.add(listener);
                record.setOnStartPostponedListener(listener);
                if (isPop) {
                    record.executeOps();
                } else {
                    record.executePopOps(false);
                }
                postponeIndex--;
                if (i != postponeIndex) {
                    records.remove(i);
                    records.add(postponeIndex, record);
                }
                addAddedFragments(added);
            }
            i--;
        }
        return postponeIndex;
    }

    private void completeExecute(BackStackRecord record, boolean isPop, boolean runTransitions, boolean moveToState) {
        ArrayList<BackStackRecord> records = new ArrayList(1);
        ArrayList<Boolean> isRecordPop = new ArrayList(1);
        records.add(record);
        isRecordPop.add(Boolean.valueOf(isPop));
        executeOps(records, isRecordPop, 0, 1);
        if (runTransitions) {
            FragmentTransition.startTransitions(this, records, isRecordPop, 0, 1, true);
        }
        if (moveToState) {
            moveToState(this.mCurState, true);
        }
        if (this.mActive != null) {
            int numActive = this.mActive.size();
            for (int i = 0; i < numActive; i++) {
                Fragment fragment = (Fragment) this.mActive.get(i);
                if (fragment != null && fragment.mView != null && fragment.mIsNewlyAdded && record.interactsWith(fragment.mContainerId)) {
                    if (VERSION.SDK_INT >= 11 && fragment.mPostponedAlpha > 0.0f) {
                        fragment.mView.setAlpha(fragment.mPostponedAlpha);
                    }
                    if (moveToState) {
                        fragment.mPostponedAlpha = 0.0f;
                    } else {
                        fragment.mPostponedAlpha = -1.0f;
                        fragment.mIsNewlyAdded = false;
                    }
                }
            }
        }
    }

    private Fragment findFragmentUnder(Fragment f) {
        ViewGroup container = f.mContainer;
        View view = f.mView;
        if (container == null || view == null) {
            return null;
        }
        for (int i = this.mAdded.indexOf(f) - 1; i >= 0; i--) {
            Fragment underFragment = (Fragment) this.mAdded.get(i);
            if (underFragment.mContainer == container && underFragment.mView != null) {
                return underFragment;
            }
        }
        return null;
    }

    private static void executeOps(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            BackStackRecord record = (BackStackRecord) records.get(i);
            if (((Boolean) isRecordPop.get(i)).booleanValue()) {
                boolean moveToState;
                record.bumpBackStackNesting(-1);
                if (i != endIndex - 1) {
                    moveToState = false;
                } else {
                    moveToState = true;
                }
                record.executePopOps(moveToState);
            } else {
                record.bumpBackStackNesting(1);
                record.executeOps();
            }
        }
    }

    private void addAddedFragments(ArraySet<Fragment> added) {
        if (this.mCurState >= 1) {
            int numAdded;
            int state = Math.min(this.mCurState, 4);
            if (this.mAdded != null) {
                numAdded = this.mAdded.size();
            } else {
                numAdded = 0;
            }
            for (int i = 0; i < numAdded; i++) {
                Fragment fragment = (Fragment) this.mAdded.get(i);
                if (fragment.mState < state) {
                    moveToState(fragment, state, fragment.getNextAnim(), fragment.getNextTransition(), false);
                    if (!(fragment.mView == null || fragment.mHidden || !fragment.mIsNewlyAdded)) {
                        added.add(fragment);
                    }
                }
            }
        }
    }

    private void forcePostponedTransactions() {
        if (this.mPostponedTransactions != null) {
            while (!this.mPostponedTransactions.isEmpty()) {
                ((StartEnterTransitionListener) this.mPostponedTransactions.remove(0)).completeTransaction();
            }
        }
    }

    private void endAnimatingAwayFragments() {
        int numFragments;
        if (this.mActive != null) {
            numFragments = this.mActive.size();
        } else {
            numFragments = 0;
        }
        for (int i = 0; i < numFragments; i++) {
            Fragment fragment = (Fragment) this.mActive.get(i);
            if (!(fragment == null || fragment.getAnimatingAway() == null)) {
                int stateAfterAnimating = fragment.getStateAfterAnimating();
                View animatingAway = fragment.getAnimatingAway();
                fragment.setAnimatingAway(null);
                Animation animation = animatingAway.getAnimation();
                if (animation != null) {
                    animation.cancel();
                }
                moveToState(fragment, stateAfterAnimating, 0, 0, false);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean generateOpsForPendingActions(java.util.ArrayList<android.support.v4.app.BackStackRecord> r6, java.util.ArrayList<java.lang.Boolean> r7) {
        /*
        r5 = this;
        r3 = 0;
        monitor-enter(r5);
        r2 = r5.mPendingActions;	 Catch:{ all -> 0x003c }
        if (r2 != 0) goto L_0x0008;
    L_0x0006:
        monitor-exit(r5);	 Catch:{ all -> 0x003c }
        return r3;
    L_0x0008:
        r2 = r5.mPendingActions;	 Catch:{ all -> 0x003c }
        r2 = r2.size();	 Catch:{ all -> 0x003c }
        if (r2 == 0) goto L_0x0006;
    L_0x0010:
        r2 = r5.mPendingActions;	 Catch:{ all -> 0x003c }
        r1 = r2.size();	 Catch:{ all -> 0x003c }
        r0 = 0;
    L_0x0017:
        if (r0 < r1) goto L_0x002e;
    L_0x0019:
        r2 = r5.mPendingActions;	 Catch:{ all -> 0x003c }
        r2.clear();	 Catch:{ all -> 0x003c }
        r2 = r5.mHost;	 Catch:{ all -> 0x003c }
        r2 = r2.getHandler();	 Catch:{ all -> 0x003c }
        r4 = r5.mExecCommit;	 Catch:{ all -> 0x003c }
        r2.removeCallbacks(r4);	 Catch:{ all -> 0x003c }
        monitor-exit(r5);	 Catch:{ all -> 0x003c }
        if (r1 > 0) goto L_0x003f;
    L_0x002c:
        r2 = r3;
    L_0x002d:
        return r2;
    L_0x002e:
        r2 = r5.mPendingActions;	 Catch:{ all -> 0x003c }
        r2 = r2.get(r0);	 Catch:{ all -> 0x003c }
        r2 = (android.support.v4.app.FragmentManagerImpl.OpGenerator) r2;	 Catch:{ all -> 0x003c }
        r2.generateOps(r6, r7);	 Catch:{ all -> 0x003c }
        r0 = r0 + 1;
        goto L_0x0017;
    L_0x003c:
        r2 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x003c }
        throw r2;
    L_0x003f:
        r2 = 1;
        goto L_0x002d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.app.FragmentManagerImpl.generateOpsForPendingActions(java.util.ArrayList, java.util.ArrayList):boolean");
    }

    void doPendingDeferredStart() {
        if (this.mHavePendingDeferredStart) {
            boolean loadersRunning = false;
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = (Fragment) this.mActive.get(i);
                if (!(f == null || f.mLoaderManager == null)) {
                    loadersRunning |= f.mLoaderManager.hasRunningLoaders();
                }
            }
            if (!loadersRunning) {
                this.mHavePendingDeferredStart = false;
                startPendingDeferredFragments();
            }
        }
    }

    void reportBackStackChanged() {
        if (this.mBackStackChangeListeners != null) {
            for (int i = 0; i < this.mBackStackChangeListeners.size(); i++) {
                ((OnBackStackChangedListener) this.mBackStackChangeListeners.get(i)).onBackStackChanged();
            }
        }
    }

    void addBackStackState(BackStackRecord state) {
        if (this.mBackStack == null) {
            this.mBackStack = new ArrayList();
        }
        this.mBackStack.add(state);
        reportBackStackChanged();
    }

    boolean popBackStackState(ArrayList<BackStackRecord> records, ArrayList<Boolean> isRecordPop, String name, int id, int flags) {
        if (this.mBackStack == null) {
            return false;
        }
        if (name == null && id < 0 && (flags & 1) == 0) {
            int last = this.mBackStack.size() - 1;
            if (last < 0) {
                return false;
            }
            records.add(this.mBackStack.remove(last));
            isRecordPop.add(Boolean.valueOf(true));
        } else {
            int index = -1;
            if (name != null || id >= 0) {
                BackStackRecord bss;
                index = this.mBackStack.size() - 1;
                while (index >= 0) {
                    bss = (BackStackRecord) this.mBackStack.get(index);
                    if (name != null) {
                        if (name.equals(bss.getName())) {
                            break;
                        }
                    }
                    if (id >= 0) {
                        if (id == bss.mIndex) {
                            break;
                        }
                    }
                    index--;
                }
                if (index < 0) {
                    return false;
                }
                if ((flags & 1) != 0) {
                    index--;
                    while (index >= 0) {
                        bss = (BackStackRecord) this.mBackStack.get(index);
                        if (name == null || !name.equals(bss.getName())) {
                            if (id < 0 || id != bss.mIndex) {
                                break;
                            }
                        }
                        index--;
                    }
                }
            }
            if (index == this.mBackStack.size() - 1) {
                return false;
            }
            for (int i = this.mBackStack.size() - 1; i > index; i--) {
                records.add(this.mBackStack.remove(i));
                isRecordPop.add(Boolean.valueOf(true));
            }
        }
        return true;
    }

    FragmentManagerNonConfig retainNonConfig() {
        List list = null;
        ArrayList<FragmentManagerNonConfig> childFragments = null;
        if (this.mActive != null) {
            for (int i = 0; i < this.mActive.size(); i++) {
                Fragment f = (Fragment) this.mActive.get(i);
                if (f != null) {
                    if (f.mRetainInstance) {
                        if (list == null) {
                            list = new ArrayList();
                        }
                        list.add(f);
                        f.mRetaining = true;
                        f.mTargetIndex = f.mTarget == null ? -1 : f.mTarget.mIndex;
                        if (DEBUG) {
                            Log.v("FragmentManager", "retainNonConfig: keeping retained " + f);
                        }
                    }
                    boolean addedChild = false;
                    if (f.mChildFragmentManager != null) {
                        FragmentManagerNonConfig child = f.mChildFragmentManager.retainNonConfig();
                        if (child != null) {
                            if (childFragments == null) {
                                childFragments = new ArrayList();
                                for (int j = 0; j < i; j++) {
                                    childFragments.add(null);
                                }
                            }
                            childFragments.add(child);
                            addedChild = true;
                        }
                    }
                    if (!(childFragments == null || addedChild)) {
                        childFragments.add(null);
                    }
                }
            }
        }
        if (list == null && childFragments == null) {
            return null;
        }
        return new FragmentManagerNonConfig(list, childFragments);
    }

    void saveFragmentViewState(Fragment f) {
        if (f.mInnerView != null) {
            if (this.mStateArray != null) {
                this.mStateArray.clear();
            } else {
                this.mStateArray = new SparseArray();
            }
            f.mInnerView.saveHierarchyState(this.mStateArray);
            if (this.mStateArray.size() > 0) {
                f.mSavedViewState = this.mStateArray;
                this.mStateArray = null;
            }
        }
    }

    Bundle saveFragmentBasicState(Fragment f) {
        Bundle result = null;
        if (this.mStateBundle == null) {
            this.mStateBundle = new Bundle();
        }
        f.performSaveInstanceState(this.mStateBundle);
        dispatchOnFragmentSaveInstanceState(f, this.mStateBundle, false);
        if (!this.mStateBundle.isEmpty()) {
            result = this.mStateBundle;
            this.mStateBundle = null;
        }
        if (f.mView != null) {
            saveFragmentViewState(f);
        }
        if (f.mSavedViewState != null) {
            if (result == null) {
                result = new Bundle();
            }
            result.putSparseParcelableArray("android:view_state", f.mSavedViewState);
        }
        if (!f.mUserVisibleHint) {
            if (result == null) {
                result = new Bundle();
            }
            result.putBoolean("android:user_visible_hint", f.mUserVisibleHint);
        }
        return result;
    }

    Parcelable saveAllState() {
        forcePostponedTransactions();
        endAnimatingAwayFragments();
        execPendingActions();
        if (HONEYCOMB) {
            this.mStateSaved = true;
        }
        if (this.mActive == null || this.mActive.size() <= 0) {
            return null;
        }
        int i;
        int N = this.mActive.size();
        FragmentState[] active = new FragmentState[N];
        boolean haveFragments = false;
        for (i = 0; i < N; i++) {
            Fragment f = (Fragment) this.mActive.get(i);
            if (f != null) {
                if (f.mIndex < 0) {
                    throwException(new IllegalStateException("Failure saving state: active " + f + " has cleared index: " + f.mIndex));
                }
                haveFragments = true;
                FragmentState fs = new FragmentState(f);
                active[i] = fs;
                if (f.mState > 0 && fs.mSavedFragmentState == null) {
                    fs.mSavedFragmentState = saveFragmentBasicState(f);
                    if (f.mTarget != null) {
                        if (f.mTarget.mIndex < 0) {
                            throwException(new IllegalStateException("Failure saving state: " + f + " has target not in fragment manager: " + f.mTarget));
                        }
                        if (fs.mSavedFragmentState == null) {
                            fs.mSavedFragmentState = new Bundle();
                        }
                        putFragment(fs.mSavedFragmentState, "android:target_state", f.mTarget);
                        if (f.mTargetRequestCode != 0) {
                            fs.mSavedFragmentState.putInt("android:target_req_state", f.mTargetRequestCode);
                        }
                    }
                } else {
                    fs.mSavedFragmentState = f.mSavedFragmentState;
                }
                if (DEBUG) {
                    Log.v("FragmentManager", "Saved state of " + f + ": " + fs.mSavedFragmentState);
                }
            }
        }
        if (haveFragments) {
            int[] added = null;
            BackStackState[] backStack = null;
            if (this.mAdded != null) {
                N = this.mAdded.size();
                if (N > 0) {
                    added = new int[N];
                    for (i = 0; i < N; i++) {
                        added[i] = ((Fragment) this.mAdded.get(i)).mIndex;
                        if (added[i] < 0) {
                            throwException(new IllegalStateException("Failure saving state: active " + this.mAdded.get(i) + " has cleared index: " + added[i]));
                        }
                        if (DEBUG) {
                            Log.v("FragmentManager", "saveAllState: adding fragment #" + i + ": " + this.mAdded.get(i));
                        }
                    }
                }
            }
            if (this.mBackStack != null) {
                N = this.mBackStack.size();
                if (N > 0) {
                    backStack = new BackStackState[N];
                    for (i = 0; i < N; i++) {
                        backStack[i] = new BackStackState((BackStackRecord) this.mBackStack.get(i));
                        if (DEBUG) {
                            Log.v("FragmentManager", "saveAllState: adding back stack #" + i + ": " + this.mBackStack.get(i));
                        }
                    }
                }
            }
            FragmentManagerState fms = new FragmentManagerState();
            fms.mActive = active;
            fms.mAdded = added;
            fms.mBackStack = backStack;
            return fms;
        }
        if (DEBUG) {
            Log.v("FragmentManager", "saveAllState: no fragments!");
        }
        return null;
    }

    void restoreAllState(Parcelable state, FragmentManagerNonConfig nonConfig) {
        if (state != null) {
            FragmentManagerState fms = (FragmentManagerState) state;
            if (fms.mActive != null) {
                List<Fragment> nonConfigFragments;
                int count;
                int i;
                Fragment f;
                FragmentState fs;
                List childNonConfigs = null;
                if (nonConfig != null) {
                    nonConfigFragments = nonConfig.getFragments();
                    childNonConfigs = nonConfig.getChildNonConfigs();
                    count = nonConfigFragments == null ? 0 : nonConfigFragments.size();
                    for (i = 0; i < count; i++) {
                        f = (Fragment) nonConfigFragments.get(i);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: re-attaching retained " + f);
                        }
                        fs = fms.mActive[f.mIndex];
                        fs.mInstance = f;
                        f.mSavedViewState = null;
                        f.mBackStackNesting = 0;
                        f.mInLayout = false;
                        f.mAdded = false;
                        f.mTarget = null;
                        if (fs.mSavedFragmentState != null) {
                            fs.mSavedFragmentState.setClassLoader(this.mHost.getContext().getClassLoader());
                            f.mSavedViewState = fs.mSavedFragmentState.getSparseParcelableArray("android:view_state");
                            f.mSavedFragmentState = fs.mSavedFragmentState;
                        }
                    }
                }
                this.mActive = new ArrayList(fms.mActive.length);
                if (this.mAvailIndices != null) {
                    this.mAvailIndices.clear();
                }
                i = 0;
                while (i < fms.mActive.length) {
                    fs = fms.mActive[i];
                    if (fs == null) {
                        this.mActive.add(null);
                        if (this.mAvailIndices == null) {
                            this.mAvailIndices = new ArrayList();
                        }
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: avail #" + i);
                        }
                        this.mAvailIndices.add(Integer.valueOf(i));
                    } else {
                        FragmentManagerNonConfig childNonConfig = null;
                        if (childNonConfigs != null && i < childNonConfigs.size()) {
                            childNonConfig = (FragmentManagerNonConfig) childNonConfigs.get(i);
                        }
                        f = fs.instantiate(this.mHost, this.mParent, childNonConfig);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: active #" + i + ": " + f);
                        }
                        this.mActive.add(f);
                        fs.mInstance = null;
                    }
                    i++;
                }
                if (nonConfig != null) {
                    nonConfigFragments = nonConfig.getFragments();
                    count = nonConfigFragments == null ? 0 : nonConfigFragments.size();
                    for (i = 0; i < count; i++) {
                        f = (Fragment) nonConfigFragments.get(i);
                        if (f.mTargetIndex >= 0) {
                            if (f.mTargetIndex >= this.mActive.size()) {
                                Log.w("FragmentManager", "Re-attaching retained fragment " + f + " target no longer exists: " + f.mTargetIndex);
                                f.mTarget = null;
                            } else {
                                f.mTarget = (Fragment) this.mActive.get(f.mTargetIndex);
                            }
                        }
                    }
                }
                if (fms.mAdded == null) {
                    this.mAdded = null;
                } else {
                    this.mAdded = new ArrayList(fms.mAdded.length);
                    for (i = 0; i < fms.mAdded.length; i++) {
                        f = (Fragment) this.mActive.get(fms.mAdded[i]);
                        if (f == null) {
                            throwException(new IllegalStateException("No instantiated fragment for index #" + fms.mAdded[i]));
                        }
                        f.mAdded = true;
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: added #" + i + ": " + f);
                        }
                        if (this.mAdded.contains(f)) {
                            throw new IllegalStateException("Already added!");
                        }
                        this.mAdded.add(f);
                    }
                }
                if (fms.mBackStack == null) {
                    this.mBackStack = null;
                } else {
                    this.mBackStack = new ArrayList(fms.mBackStack.length);
                    for (i = 0; i < fms.mBackStack.length; i++) {
                        BackStackRecord bse = fms.mBackStack[i].instantiate(this);
                        if (DEBUG) {
                            Log.v("FragmentManager", "restoreAllState: back stack #" + i + " (index " + bse.mIndex + "): " + bse);
                            PrintWriter pw = new PrintWriter(new LogWriter("FragmentManager"));
                            bse.dump("  ", pw, false);
                            pw.close();
                        }
                        this.mBackStack.add(bse);
                        if (bse.mIndex >= 0) {
                            setBackStackIndex(bse.mIndex, bse);
                        }
                    }
                }
            }
        }
    }

    public void attachController(FragmentHostCallback host, FragmentContainer container, Fragment parent) {
        if (this.mHost == null) {
            this.mHost = host;
            this.mContainer = container;
            this.mParent = parent;
            return;
        }
        throw new IllegalStateException("Already attached");
    }

    public void noteStateNotSaved() {
        this.mStateSaved = false;
    }

    public void dispatchCreate() {
        this.mStateSaved = false;
        this.mExecutingActions = true;
        moveToState(1, false);
        this.mExecutingActions = false;
    }

    public void dispatchActivityCreated() {
        this.mStateSaved = false;
        this.mExecutingActions = true;
        moveToState(2, false);
        this.mExecutingActions = false;
    }

    public void dispatchStart() {
        this.mStateSaved = false;
        this.mExecutingActions = true;
        moveToState(4, false);
        this.mExecutingActions = false;
    }

    public void dispatchResume() {
        this.mStateSaved = false;
        this.mExecutingActions = true;
        moveToState(5, false);
        this.mExecutingActions = false;
    }

    public void dispatchPause() {
        this.mExecutingActions = true;
        moveToState(4, false);
        this.mExecutingActions = false;
    }

    public void dispatchStop() {
        this.mStateSaved = true;
        this.mExecutingActions = true;
        moveToState(3, false);
        this.mExecutingActions = false;
    }

    public void dispatchReallyStop() {
        this.mExecutingActions = true;
        moveToState(2, false);
        this.mExecutingActions = false;
    }

    public void dispatchDestroyView() {
        this.mExecutingActions = true;
        moveToState(1, false);
        this.mExecutingActions = false;
    }

    public void dispatchDestroy() {
        this.mDestroyed = true;
        execPendingActions();
        this.mExecutingActions = true;
        moveToState(0, false);
        this.mExecutingActions = false;
        this.mHost = null;
        this.mContainer = null;
        this.mParent = null;
    }

    public void dispatchMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if (this.mAdded != null) {
            for (int i = this.mAdded.size() - 1; i >= 0; i--) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null) {
                    f.performMultiWindowModeChanged(isInMultiWindowMode);
                }
            }
        }
    }

    public void dispatchPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (this.mAdded != null) {
            for (int i = this.mAdded.size() - 1; i >= 0; i--) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null) {
                    f.performPictureInPictureModeChanged(isInPictureInPictureMode);
                }
            }
        }
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null) {
                    f.performConfigurationChanged(newConfig);
                }
            }
        }
    }

    public void dispatchLowMemory() {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null) {
                    f.performLowMemory();
                }
            }
        }
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int i;
        Fragment f;
        boolean show = false;
        ArrayList arrayList = null;
        if (this.mAdded != null) {
            for (i = 0; i < this.mAdded.size(); i++) {
                f = (Fragment) this.mAdded.get(i);
                if (f != null && f.performCreateOptionsMenu(menu, inflater)) {
                    show = true;
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(f);
                }
            }
        }
        if (this.mCreatedMenus != null) {
            for (i = 0; i < this.mCreatedMenus.size(); i++) {
                f = (Fragment) this.mCreatedMenus.get(i);
                if (arrayList == null || !arrayList.contains(f)) {
                    f.onDestroyOptionsMenu();
                }
            }
        }
        this.mCreatedMenus = arrayList;
        return show;
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        boolean show = false;
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null && f.performPrepareOptionsMenu(menu)) {
                    show = true;
                }
            }
        }
        return show;
    }

    public boolean dispatchOptionsItemSelected(MenuItem item) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null && f.performOptionsItemSelected(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean dispatchContextItemSelected(MenuItem item) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null && f.performContextItemSelected(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        if (this.mAdded != null) {
            for (int i = 0; i < this.mAdded.size(); i++) {
                Fragment f = (Fragment) this.mAdded.get(i);
                if (f != null) {
                    f.performOptionsMenuClosed(menu);
                }
            }
        }
    }

    void dispatchOnFragmentPreAttached(Fragment f, Context context, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentPreAttached(f, context, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentPreAttached(this, f, context);
                }
            }
        }
    }

    void dispatchOnFragmentAttached(Fragment f, Context context, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentAttached(f, context, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentAttached(this, f, context);
                }
            }
        }
    }

    void dispatchOnFragmentCreated(Fragment f, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentCreated(f, savedInstanceState, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentCreated(this, f, savedInstanceState);
                }
            }
        }
    }

    void dispatchOnFragmentActivityCreated(Fragment f, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentActivityCreated(f, savedInstanceState, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentActivityCreated(this, f, savedInstanceState);
                }
            }
        }
    }

    void dispatchOnFragmentViewCreated(Fragment f, View v, Bundle savedInstanceState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentViewCreated(f, v, savedInstanceState, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentViewCreated(this, f, v, savedInstanceState);
                }
            }
        }
    }

    void dispatchOnFragmentStarted(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentStarted(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentStarted(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentResumed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentResumed(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentResumed(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentPaused(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentPaused(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentPaused(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentStopped(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentStopped(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentStopped(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentSaveInstanceState(Fragment f, Bundle outState, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentSaveInstanceState(f, outState, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentSaveInstanceState(this, f, outState);
                }
            }
        }
    }

    void dispatchOnFragmentViewDestroyed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentViewDestroyed(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentViewDestroyed(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentDestroyed(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentDestroyed(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentDestroyed(this, f);
                }
            }
        }
    }

    void dispatchOnFragmentDetached(Fragment f, boolean onlyRecursive) {
        if (this.mParent != null) {
            FragmentManager parentManager = this.mParent.getFragmentManager();
            if (parentManager instanceof FragmentManagerImpl) {
                ((FragmentManagerImpl) parentManager).dispatchOnFragmentDetached(f, true);
            }
        }
        if (this.mLifecycleCallbacks != null) {
            Iterator it = this.mLifecycleCallbacks.iterator();
            while (it.hasNext()) {
                Pair<FragmentLifecycleCallbacks, Boolean> p = (Pair) it.next();
                if (!onlyRecursive || ((Boolean) p.second).booleanValue()) {
                    ((FragmentLifecycleCallbacks) p.first).onFragmentDetached(this, f);
                }
            }
        }
    }

    public static int reverseTransit(int transit) {
        switch (transit) {
            case 4097:
                return 8194;
            case 4099:
                return 4099;
            case 8194:
                return 4097;
            default:
                return 0;
        }
    }

    public static int transitToStyleIndex(int transit, boolean enter) {
        int animAttr = -1;
        switch (transit) {
            case 4097:
                animAttr = !enter ? 2 : 1;
                break;
            case 4099:
                animAttr = !enter ? 6 : 5;
                break;
            case 8194:
                animAttr = !enter ? 4 : 3;
                break;
        }
        return animAttr;
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Fragment fragment = null;
        if (!"fragment".equals(name)) {
            return null;
        }
        String fname = attrs.getAttributeValue(null, "class");
        TypedArray a = context.obtainStyledAttributes(attrs, FragmentTag.Fragment);
        if (fname == null) {
            fname = a.getString(0);
        }
        int id = a.getResourceId(1, -1);
        String tag = a.getString(2);
        a.recycle();
        if (!Fragment.isSupportFragmentClass(this.mHost.getContext(), fname)) {
            return null;
        }
        int containerId = parent == null ? 0 : parent.getId();
        if (containerId == -1 && id == -1 && tag == null) {
            throw new IllegalArgumentException(attrs.getPositionDescription() + ": Must specify unique android:id, android:tag, or have a parent with an id for " + fname);
        }
        if (id != -1) {
            fragment = findFragmentById(id);
        }
        if (fragment == null && tag != null) {
            fragment = findFragmentByTag(tag);
        }
        if (fragment == null && containerId != -1) {
            fragment = findFragmentById(containerId);
        }
        if (DEBUG) {
            Log.v("FragmentManager", "onCreateView: id=0x" + Integer.toHexString(id) + " fname=" + fname + " existing=" + fragment);
        }
        if (fragment == null) {
            fragment = Fragment.instantiate(context, fname);
            fragment.mFromLayout = true;
            fragment.mFragmentId = id == 0 ? containerId : id;
            fragment.mContainerId = containerId;
            fragment.mTag = tag;
            fragment.mInLayout = true;
            fragment.mFragmentManager = this;
            fragment.mHost = this.mHost;
            fragment.onInflate(this.mHost.getContext(), attrs, fragment.mSavedFragmentState);
            addFragment(fragment, true);
        } else if (fragment.mInLayout) {
            throw new IllegalArgumentException(attrs.getPositionDescription() + ": Duplicate id 0x" + Integer.toHexString(id) + ", tag " + tag + ", or parent id 0x" + Integer.toHexString(containerId) + " with another fragment for " + fname);
        } else {
            fragment.mInLayout = true;
            fragment.mHost = this.mHost;
            if (!fragment.mRetaining) {
                fragment.onInflate(this.mHost.getContext(), attrs, fragment.mSavedFragmentState);
            }
        }
        if (this.mCurState < 1 && fragment.mFromLayout) {
            moveToState(fragment, 1, 0, 0, false);
        } else {
            moveToState(fragment);
        }
        if (fragment.mView != null) {
            if (id != 0) {
                fragment.mView.setId(id);
            }
            if (fragment.mView.getTag() == null) {
                fragment.mView.setTag(tag);
            }
            return fragment.mView;
        }
        throw new IllegalStateException("Fragment " + fname + " did not create a view.");
    }

    LayoutInflaterFactory getLayoutInflaterFactory() {
        return this;
    }
}
