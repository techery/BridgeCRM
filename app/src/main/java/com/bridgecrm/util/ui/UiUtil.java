package com.bridgecrm.util.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Author: Aleksey Malevaniy, a.malevaniy@gmail.com
 */
public class UiUtil {

    ///////////////////////////////////////////////////////////////////////////
    // Bitmaps and drawables
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets the background for a view while preserving its current padding. If the background drawable
     * has its own padding, that padding will be added to the current padding.
     *
     * @param view          View to receive the new background.
     * @param backgroundRes Drawable res to set as new background.
     */
    public static void setBackgroundAndKeepPadding(View view, int backgroundRes) {
        Drawable backgroundDrawable = view.getResources().getDrawable(backgroundRes);
        setBackgroundAndKeepPadding(view, backgroundDrawable);
    }

    /**
     * Sets the background for a view while preserving its current padding. If the background drawable
     * has its own padding, that padding will be added to the current padding.
     *
     * @param view               View to receive the new background.
     * @param backgroundDrawable Drawable to set as new background.
     */
    public static void setBackgroundAndKeepPadding(View view, Drawable backgroundDrawable) {
        int top = view.getPaddingTop();
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();

        setBackgroundCompat(view, backgroundDrawable);
        view.setPadding(left, top, right, bottom);
    }

    public static void setBackgroundCompat(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void setBackgroundCompat(View view, Bitmap bitmap) {
        setBackgroundCompat(view, new BitmapDrawable(bitmap));
    }

    public static Bitmap getDrawingCache(View view) {
        Bitmap drawingCache = null;
        try {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap b = view.getDrawingCache();
            if (b != null) {
                drawingCache = b.copy(Bitmap.Config.ARGB_8888, false);
                view.destroyDrawingCache();
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return drawingCache;
    }

    public static InputStream convertToInputStream(Drawable drawable, Bitmap.CompressFormat format, int quality) {
        BitmapDrawable bitDw = ((BitmapDrawable) drawable);
        Bitmap bitmap = bitDw.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, stream);
        byte[] imageInByte = stream.toByteArray();
        Timber.d("........length......" + imageInByte.length);
        return new ByteArrayInputStream(imageInByte);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private static WeakReference<RenderScript> rsRef = new WeakReference<>(null);
    private static WeakReference<ScriptIntrinsicBlur> blurScriptRef = new WeakReference<>(null);

    /**
     * Use {@link RenderScript} to blur bitmap.
     *
     * @param bitmap to be blurred
     * @param radius of blur
     * @throws RSRuntimeException when some .so libs are not available (e.g. on Genymotion emulator)
     */
    public static synchronized void applyBlur(Context context, Bitmap bitmap, Float radius) throws RSRuntimeException {
        RenderScript rs = rsRef.get();
        if (rs == null) {
            rs = RenderScript.create(context.getApplicationContext());
            rsRef = new WeakReference<>(rs);
        }
        //this will blur the bitmap with a provided radius and save it in bitmap
        final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        final Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptIntrinsicBlur blurScript = blurScriptRef.get();
        if (blurScript == null) {
            blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blurScriptRef = new WeakReference<>(blurScript);
        }

        blurScript.setRadius(radius);
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(bitmap);
    }

    /**
     * Take srcView's bg, apply blur in background and set as destView's bg.
     * Dim color is applied as fast as possible.
     */
    public static void applyBlurredBackground(final Context context, View srcView, final View destView, final float radius, final int dimColor) {
        destView.setBackgroundColor(context.getResources().getColor(dimColor));
        //
        final Bitmap bitmap = UiUtil.getDrawingCache(srcView);
        new AsyncTaskLoader<Drawable>(context) {

            @Override
            public Drawable loadInBackground() {
                BitmapDrawable drawable = null;
                if (bitmap != null) {
                    try {
                        UiUtil.applyBlur(getContext(), bitmap, radius);
                        drawable = new BitmapDrawable(context.getResources(), bitmap);
                        drawable.setColorFilter(context.getResources().getColor(dimColor), PorterDuff.Mode.DARKEN);
                    } catch (RSRuntimeException | OutOfMemoryError e) {
                        Timber.w(e, "Blurred background will be replaced to dimmed due to error");
                        bitmap.recycle();
                    }
                }
                return drawable;
            }

            @Override
            public void deliverResult(Drawable data) {
                try {
                    if (data != null) {
                        UiUtil.setBackgroundCompat(destView, data);
                    }
                } catch (OutOfMemoryError e) {
                    Timber.w(e, "Blurred background will be replaced to dimmed due to error");
                }
            }
        }.forceLoad();

    }

    ///////////////////////////////////////////////////////////////////////////
    // Widgets
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Center dialog content inside available space
     *
     * @param dialog
     */
    public static void centerDialogContent(Dialog dialog) {
        ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
        View content = decorView.getChildAt(0);
        FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) content.getLayoutParams();
        contentParams.gravity = Gravity.CENTER;
        content.setLayoutParams(contentParams);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Keyboard
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Disable soft keyboard from appearing, use in conjunction with android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"
     *
     * @param editText
     */
    public static void disableSoftInputFromAppearing(EditText editText) {
        if (Build.VERSION.SDK_INT >= 11) {
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
        } else {
            editText.setRawInputType(InputType.TYPE_NULL);
            editText.setFocusable(true);
        }
    }

    /**
     * Show soft keyboard explicitly
     *
     * @param activity
     */
    public static void showSoftInputMethod(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Hide soft keyboard if visible
     *
     * @param activity
     */
    public static void hideSoftInputMethod(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
            activity.getWindow().getDecorView().getWindowToken(),
            0
        );
        try {
            View currentFocus = activity.getWindow().getCurrentFocus();
            if (currentFocus != null)
                currentFocus.clearFocus();
        } catch (Exception e) {
            // current focus could be out of visibility
        }
    }

    public static void hideSoftInputMethod(View view) {
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Screen & Metrics
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Get screen's available height without statusbar and actionbar
     *
     * @param context
     * @return available height in pixels
     */
    public static int getAvailableDisplayHeight(Context context, boolean withActionBar) {
        int height = 0;
        // Calculate height for photo section
        // 1. Calculate action bar height
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
            new int[]{android.R.attr.actionBarSize}
        );
        int actionBarHeight = 0;
        if (withActionBar) {
            actionBarHeight = styledAttributes.getDimensionPixelSize(0, 0);
        }
        styledAttributes.recycle();
        // 2. Calculate status bar height (On MDPI devices, the status bar is 25px)
        int statusBarHeight = (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        // 3. Calculate window height
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        // 4. And now we can calculate available section height
        height = display.getHeight() - actionBarHeight - statusBarHeight;
        return height;
    }

    public static int getAvailableDisplayHeight(Context context) {
        return getAvailableDisplayHeight(context, true);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    public static int convertPixsToDips(Context context, int pixels) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    public static float convertDipsToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    private static final int INCHES_IN_FEET = 12;
    private static final float INCHS_FACTOR = 2.54f;

    public static Pair<Integer, Integer> getDistanceFromCentimeters(int centimeters) {
        int inches = Math.round(centimeters / INCHS_FACTOR);
        int feet = inches / INCHES_IN_FEET;
        inches = inches % INCHES_IN_FEET;
        return new Pair<>(feet, inches);
    }

    public static int getDistanceFromFeet(int feet, int inches) {
        int inchesTotal = inches + feet * INCHES_IN_FEET;
        return Math.round(inchesTotal * INCHS_FACTOR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Spans
    ///////////////////////////////////////////////////////////////////////////

    public static SpannableString buildStringWithColoredWords(String templateString, String coloredString, int color) {
        String resultString = String.format(templateString, coloredString);
        SpannableString spannableString = new SpannableString(resultString);
        int startIndex = resultString.indexOf(coloredString);
        spannableString.setSpan(new ForegroundColorSpan(color), startIndex, startIndex + coloredString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
