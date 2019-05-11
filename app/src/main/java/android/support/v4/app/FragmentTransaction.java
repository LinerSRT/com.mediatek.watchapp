package android.support.v4.app;

import android.support.annotation.IdRes;

public abstract class FragmentTransaction {
    public abstract FragmentTransaction add(@IdRes int i, Fragment fragment);

    public abstract void commitNowAllowingStateLoss();

    public abstract FragmentTransaction remove(Fragment fragment);
}
