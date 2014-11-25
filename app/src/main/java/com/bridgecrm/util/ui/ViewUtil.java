package com.bridgecrm.util.ui;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.MeasureSpec;

public class ViewUtil {

    ///////////////////////////////////////////////////////////////////////////
    // Attrs helper
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Parse custom view attr values (flags).<br>
     * E.g. you want your custom view with attr to work like that: <br>
     * <code>
     * <com.dating.sdk.ui.widget.UserActionSection
     * android:layout_width="fill_parent"
     * android:layout_height="wrap_content"
     * <b>app:action="chat|mail|wink|icebreaker" /></b></code> <br>
     * E.g. You've got custom stylable with attr which works like
     * list of available flags. <br>
     * <code><declare-styleable name="UserActions">
     * <attr name="action">
     * <flag name="chat" value="1" />
     * <flag name="mail" value="2" />
     * <flag name="wink" value="4" />
     * <flag name="icebreaker" value="8" />
     * </attr>
     * </declare-styleable></code> <br>
     * <b>Caveat: flag values should be power of 2 (2,4,8,16 etc)</b>
     *
     * @param a        attrs of view to search for attrName
     * @param attrName custom attr with flag values
     * @param values   enum's values. enum should implement {@code AttrValue}
     * @return list of available (declared in xml) view's values
     */
    public static <U extends AttrValue> List<U> readAttrValues(TypedArray a, int attrName, U[] values) {
        List<U> availableValues = null;
        int valuesSumIndex = a.getInteger(attrName, 0);
        if (valuesSumIndex != 0) {
            availableValues = new ArrayList<U>();
            for (U action : values) {
                int actionIndex = action.getAttrIndex();
                if ((valuesSumIndex & actionIndex) == actionIndex)
                    availableValues.add(action);
            }
        }
        return availableValues;
    }

    /**
     * Use it for enum which defines custom view attr flags.
     */
    public interface AttrValue {

        public int getAttrIndex();
    }

    ///////////////////////////////////////////////////////////////////////////
    // ListView tricks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Method for Setting the Height of the ListView dynamically.
     * Hack to fix the issue of not showing all the items of the ListView
     * when placed inside a ScrollView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null) return;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        int desiredItemWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        int desiredItemHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredItemWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            //
            view.measure(desiredItemWidth, desiredItemHeight);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragments tricks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Get fragment from ViewPager (with FragmentPagerAdapter) by position
     */
    public static Fragment findFragmentByPosition(FragmentManager fm, View viewPager, FragmentPagerAdapter adapter, int position) {
        return fm.findFragmentByTag(
            "android:switcher:" + viewPager.getId() + ":"
                + adapter.getItemId(position)
        );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Visibility tricks
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isVisibleOnScreen(Fragment fragment) {
        return isVisibleOnScreen(fragment.getActivity(), fragment.getView());
    }

    public static boolean isVisibleOnScreen(Activity activity, View view) {
        if (activity == null || view == null) {
            return false; // ASK better throw exception?
        }
        Rect screenRect = new Rect();
        activity.getWindow().getDecorView().getGlobalVisibleRect(screenRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        boolean inHorizontalBounds = screenRect.left <= location[0] && screenRect.right >= location[0] + view.getWidth();
        boolean inVerticalBounds = screenRect.top <= location[1] && screenRect.bottom >= location[1] + view.getHeight();
        return view.isShown() && inHorizontalBounds && inVerticalBounds;
    }

    public static void setViewGroupEnabled(ViewGroup group, boolean isEnabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            child.setEnabled(isEnabled);
            if (child instanceof ViewGroup)
                setViewGroupEnabled((ViewGroup) child, isEnabled);
        }
    }

    public static void setViewGroupVisibile(ViewGroup group, boolean isVisible) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (isVisible)
                child.setVisibility(View.VISIBLE);
            else
                child.setVisibility(View.GONE);
            if (child instanceof ViewGroup)
                setViewGroupEnabled((ViewGroup) child, isVisible);
        }
    }
}
