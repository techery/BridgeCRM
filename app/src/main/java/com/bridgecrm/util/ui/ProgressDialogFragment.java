package com.bridgecrm.util.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public final class ProgressDialogFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = ProgressDialogFragment.class.getName();
    private static final String BUNDLE_MESSAGE = "message";
    private static final String BUNDLE_IS_CANCELABLE = "isCancelable";
    private DialogInterface.OnCancelListener mOnCancelListener;

    private static ProgressDialogFragment newInstance(
            String message,
            DialogInterface.OnCancelListener onCancelListener, boolean isCancelable) {
        ProgressDialogFragment dialogFragment = new ProgressDialogFragment();

        dialogFragment.mOnCancelListener = onCancelListener;

        Bundle args = new Bundle();
        args.putString(BUNDLE_MESSAGE, message);
        args.putBoolean(BUNDLE_IS_CANCELABLE, isCancelable);
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(args.getString(BUNDLE_MESSAGE));
        dialog.setIndeterminate(true);
        setCancelable(args.getBoolean(BUNDLE_IS_CANCELABLE));
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(dialog);
        }
    }

    public static class ProgressDialogFragmentBuilder {

        private FragmentActivity mActivity;
        private String mMessage = null;
        private DialogInterface.OnCancelListener mOnCancelListener = null;
        private boolean mCancelable = true;

        public ProgressDialogFragmentBuilder(FragmentActivity activity) {
            mActivity = activity;
        }

        public ProgressDialogFragmentBuilder setMessage(int resId) {
            mMessage = mActivity.getString(resId);
            return this;
        }

        public ProgressDialogFragmentBuilder setMessage(String text) {
            mMessage = text;
            return this;
        }

        public ProgressDialogFragmentBuilder setOnCancelListener(
                DialogInterface.OnCancelListener onCancelListener) {
            mOnCancelListener = onCancelListener;
            return this;
        }

        public ProgressDialogFragmentBuilder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public void show() {
            FragmentManager fragmentManager = mActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            ProgressDialogFragment.newInstance(mMessage, mOnCancelListener, mCancelable)
                    .show(fragmentManager, FRAGMENT_TAG);
        }
    }

    public static void dismiss(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.commit();
    }
}
