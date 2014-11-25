package com.bridgecrm.helper.media;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.bridgecrm.helper.media.event.PhotoSelectedErrorEvent;
import com.bridgecrm.helper.media.event.PhotoSelectedEvent;
import com.bridgecrm.util.app.StorageHelper;
import com.bridgecrm.util.base.IOUtils;
import com.halfbit.tinybus.TinyBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import timber.log.Timber;

public class MediaHelper {

    public static final String TAG = MediaHelper.class.getSimpleName();

    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_GALLERY = 200;
    public static final String TEMP_PHOTO_JPG = "tempPhoto.jpg";

    public static void askCapturePhoto(FragmentActivity activity) {
        Intent intent = buildCapturePhotoIntent(activity);
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }

    public static void askCapturePhoto(Fragment fragment) {
        Intent intent = buildCapturePhotoIntent(fragment.getActivity());
        fragment.startActivityForResult(intent, REQUEST_CAMERA);
    }

    public static void askPickPhoto(FragmentActivity activity, String title) {
        Intent chooserIntent = buildPickPhotoIntent(title);
        activity.startActivityForResult(chooserIntent, REQUEST_GALLERY);
    }

    public static void askPickPhoto(Fragment fragment, String title) {
        Intent chooserIntent = buildPickPhotoIntent(title);
        fragment.startActivityForResult(chooserIntent, REQUEST_GALLERY);
    }

    private static Intent buildPickPhotoIntent(String title) {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent = Intent.createChooser(intent, title);
        return intent;
    }

    /**
     * Handle {@link Activity#onActivityResult(int, int, Intent)} for media-related picking/creating operations
     *
     * @return true if result proces
     */
    public static boolean handleOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                case REQUEST_GALLERY:
                    TinyBus.from(activity).post(new PhotoSelectedErrorEvent());
                    cleanUpPhotoCaptureData(); // just in case
                    break;
            }
            return false;
        }
        // seems to be ok, proceed
        try {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    Uri uri = parsePhotoCaptureResult(activity, data);
                    if (uri != null) {
                        String capturedPath = getAbsolutePathFromLocalUri(activity, uri);
                        TinyBus.from(activity).post(new PhotoSelectedEvent(capturedPath));
                    } else {
                        TinyBus.from(activity).post(new PhotoSelectedErrorEvent());
                    }
                    cleanUpPhotoCaptureData();
                    return true;
                case REQUEST_GALLERY:
                    Uri selectedImageUri = data.getData();
                    String pickedPath;
                    pickedPath = getAbsolutePathFromUri(activity, selectedImageUri);
                    // fallback to try get local path, useful for Samsung devices
                    if (TextUtils.isEmpty(pickedPath)) {
                        pickedPath = getAbsolutePathFromLocalUri(activity, selectedImageUri);
                    }
                    TinyBus.from(activity).post(new PhotoSelectedEvent(pickedPath));
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            Timber.e(e, "Photo upload failed");
            TinyBus.from(activity).post(new PhotoSelectedErrorEvent());
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Deal with photo capture
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Starts the camera intent depending on the device configuration.
     * <p/>
     * <b>for Samsung and Sony devices:</b>
     * We call the camera activity with the method call to startActivityForResult. We only set the constant CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE. We do NOT set any other intent extras.
     * <p/>
     * <b>for all other devices:</b>
     * We call the camera activity with the method call to startActivityForResult as previously. This time, however, we additionally set the intent extra MediaStore.EXTRA_OUTPUT and provide an URI, where we want the image to be stored.
     * <p/>
     * In both cases we remember the time the camera activity was started.
     */
    private static Intent buildCapturePhotoIntent(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Timber.w("Photo capture failed: no SD-card");
            return null;
        }
        cleanUpPhotoCaptureData();

        boolean isPreDefinedCameraUri = usePredefinedUri(context);
        dateCameraIntentStarted = new Date();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (isPreDefinedCameraUri) {
            String filename = System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, filename);
            preDefinedCameraUri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, preDefinedCameraUri);
        }
        return intent;
    }

    private static boolean usePredefinedUri(Context context) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_USE_PREDEFINED_URI, true);
    }

    private static void invertPredefinedUriState(Context context) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        boolean use = preferences.getBoolean(KEY_USE_PREDEFINED_URI, true);
        preferences.edit().putBoolean(KEY_USE_PREDEFINED_URI, !use).commit();
    }

    private static String KEY_USE_PREDEFINED_URI = "KEY_USE_PREDEFINED_URI";
    /**
     * Date and time the camera intent was started.
     */
    private static Date dateCameraIntentStarted = null;
    /**
     * Default location where we want the photo to be ideally stored.
     */
    private static Uri preDefinedCameraUri = null;
    /**
     * Potential 3rd location of photo data.
     */
    private static Uri photoUriIn3rdLocation = null;
    /**
     * Orientation of the retrieved photo.
     */
    private static int rotateXDegrees = 0;

    /**
     * On camera activity result, we try to locate the photo.
     * <p/>
     * <b>Mediastore:</b>
     * First, we try to read the photo being captured from the MediaStore. Using a ContentResolver on the MediaStore content, we retrieve the latest image being taken, as well as its orientation property and its timestamp. If we find an image and it was not taken before the camera intent was called, it is the image we were looking for. Otherwise, we dismiss the result and try one of the following approaches.
     * <b>Intent extra:</b>
     * Second, we try to get an image Uri from intent.getData() of the returning intent. If this is not successful either, we continue with step 3.
     * <b>Default photo Uri:</b>
     * If all of the above mentioned steps did not work, we use the image Uri we passed to the camera activity.
     *
     * @param intent
     */
    protected static Uri parsePhotoCaptureResult(Context context, Intent intent) {
        Uri photoUri = null;
        Cursor myCursor = null;
        Date dateOfPicture = null;
        try {
            // Create a Cursor to obtain the file Path for the large image
            String[] largeFileProjection = {MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStore.Images.ImageColumns.DATE_TAKEN};
            String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
            myCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                largeFileProjection,
                null, null,
                largeFileSort
            );
            myCursor.moveToFirst();
            if (!myCursor.isAfterLast()) {
                // This will actually give you the file path location of the image.
                String largeImagePath = myCursor.getString(
                    myCursor
                        .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                );
                photoUri = Uri.fromFile(new File(largeImagePath));
                if (photoUri != null) {
                    dateOfPicture = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                    if (dateOfPicture != null && dateOfPicture.after(dateCameraIntentStarted)) {
                        rotateXDegrees = myCursor.getInt(
                            myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION)
                        );
                    } else {
                        photoUri = null;
                    }
                }
                if (!myCursor.isAfterLast()) {
                    myCursor.moveToNext();
                    String largeImagePath3rdLocation = myCursor.getString(
                        myCursor
                            .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                    );
                    Date dateOfPicture3rdLocation = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                    if (dateOfPicture3rdLocation != null && dateOfPicture3rdLocation.after(dateCameraIntentStarted)) {
                        photoUriIn3rdLocation = Uri.fromFile(new File(largeImagePath3rdLocation));
                    }
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Photo capture parsing failed");
        } finally {
            if (myCursor != null && !myCursor.isClosed()) {
                myCursor.close();
            }
        }

        if (photoUri == null) {
            try {
                photoUri = intent.getData();
            } catch (Exception e) {
                Timber.w(e, "Photo capture parsing failed");
            }
        }

        if (photoUri == null) {
            photoUri = preDefinedCameraUri;
        }

        try {
            if (photoUri != null && preDefinedCameraUri != null && new File(photoUri.getPath()).length() <= 0) {
                Uri tempUri = photoUri;
                photoUri = preDefinedCameraUri;
                preDefinedCameraUri = tempUri;
            } else {
                invertPredefinedUriState(context);
            }
        } catch (Exception e) {
            Timber.w(e, "Photo capture parsing failed");
        }

        photoUri = getFileUriFromContentUri(context, photoUri);
        preDefinedCameraUri = getFileUriFromContentUri(context, preDefinedCameraUri);
        try {
            if (photoUriIn3rdLocation != null) {
                if (photoUriIn3rdLocation.equals(photoUri) || photoUriIn3rdLocation.equals(preDefinedCameraUri)) {
                    photoUriIn3rdLocation = null;
                } else {
                    photoUriIn3rdLocation = getFileUriFromContentUri(context, photoUriIn3rdLocation);
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Photo capture parsing failed");
        }
        return photoUri;
    }

    private static void cleanUpPhotoCaptureData() {
        dateCameraIntentStarted = null;
        preDefinedCameraUri = null;
        photoUriIn3rdLocation = null;
        rotateXDegrees = 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Additional
    ///////////////////////////////////////////////////////////////////////////

    private static String getAbsolutePathFromUri(Activity activity, Uri selectedImageUri) {
        String filePath = null;
        InputStream photoStream = null;
        OutputStream fileStream = null;
        try {
            photoStream = activity.getContentResolver().openInputStream(selectedImageUri);
            File photoFile = new File(StorageHelper.getAvailableCacheDir(activity), String.valueOf(System.currentTimeMillis()) + "_" + TEMP_PHOTO_JPG);
            fileStream = new BufferedOutputStream(new FileOutputStream(photoFile));
            IOUtils.copyStream(photoStream, fileStream);
            filePath = photoFile.getAbsolutePath();
        } catch (IOException | NullPointerException e) {
            Timber.e(e, "getAbsolutePathFromUri failed");
        } finally {
            if (photoStream != null) {
                try {
                    photoStream.close();
                } catch (IOException e) {
                    Timber.e(TAG, e.toString());
                }
            }
            if (fileStream != null) {
                try {
                    fileStream.flush();
                    fileStream.close();
                } catch (IOException e) {
                    Timber.e(TAG, e.toString());
                }
            }
        }
        return filePath;
    }

    public static String getAbsolutePathFromLocalUri(Context context, Uri contentUri) {
        String path = null;
        String scheme = contentUri.getScheme();
        if (scheme.equals("file")) {
            path = contentUri.getPath();
        } else {
            Cursor cursor = null;
            try {
                String data = MediaStore.MediaColumns.DATA;
                String[] proj = {data};
                cursor = new CursorLoader(
                    context, contentUri, proj, null, null, null
                ).loadInBackground();
                int columnIndex = cursor.getColumnIndexOrThrow(data);
                cursor.moveToFirst();
                path = cursor.getString(columnIndex);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return path;
    }

    /**
     * Given an Uri that is a content Uri (e.g.
     * content://media/external/images/media/1884) this function returns the
     * respective file Uri, that is e.g. file://media/external/DCIM/abc.jpg
     *
     * @param cameraPicUri
     * @return Uri
     */
    private static Uri getFileUriFromContentUri(Context context, Uri cameraPicUri) {
        try {
            if (cameraPicUri != null
                && cameraPicUri.toString().startsWith("content")) {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(cameraPicUri, proj, null, null, null);
                cursor.moveToFirst();
                // This will actually give you the file path location of the image.
                String largeImagePath = cursor.getString(
                    cursor
                        .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                );
                return Uri.fromFile(new File(largeImagePath));
            }
            return cameraPicUri;
        } catch (Exception e) {
            return cameraPicUri;
        }
    }

    public static String getMimeTypeFromUri(Context context, Uri uri) {
        String path = getAbsolutePathFromLocalUri(context, uri);
        return getMimeTypeFromPath(path);
    }

    public static String getMimeTypeFromPath(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}