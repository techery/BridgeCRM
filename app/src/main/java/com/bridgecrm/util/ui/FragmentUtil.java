package com.bridgecrm.util.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.bridgecrm.util.base.ListUtils;

import java.util.List;

public class FragmentUtil {

    /** Checks if fragment's child manager has other fragments (not null values) */
    public static boolean hasChildren(Fragment fragment) {
        boolean hasChildren = false;
        List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
        if (!ListUtils.isEmpty(fragments)) {
            for (Fragment f : fragments) {
                if (f != null) {
                    hasChildren = true;
                    break;
                }
            }
        }
        return hasChildren;
    }

    /** Checks if fragment with tag is inside provided one, and not removing */
    public static boolean containsFragment(Fragment f, String tag) {
        Fragment byTag = f.getChildFragmentManager().findFragmentByTag(tag);
        return byTag != null && !byTag.isRemoving();
    }

    public static boolean hasFragments(FragmentActivity activity) {
        return !ListUtils.isEmpty(activity.getSupportFragmentManager().getFragments());
    }
}
