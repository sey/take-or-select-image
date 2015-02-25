package com.seventythree_apps.takeorselectimage.library;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to take or select an image using intents.
 */
public class TakeOrSelectImage {

    private static final String OUTPUT_FILE_URI = "TOSI_OUTPUT_FILE_URI";
    private static final String CHOOSER_TITLE = "TOSI_CHOOSER_TITLE";
    private static final String REQUEST_CODE = "TOSI_REQUEST_CODE";

    /**
     * The output file URI used to store the image if taken from the camera.
     */
    private Uri outputFileUri;

    /**
     * The title used when presenting the available apps to take or select a picture.
     */
    private CharSequence chooserTitle;

    /**
     * The request code used when starting the activity.
     */
    private int requestCode;

    public TakeOrSelectImage(File outputFile, CharSequence chooserTitle, int requestCode) {
        this.outputFileUri = Uri.fromFile(outputFile);
        this.chooserTitle = chooserTitle;
        this.requestCode = requestCode;
    }

    /**
     * Starts the activity that displays the available apps.
     * @param activity
     *      the activity from which
     *      the {@link android.app.Activity#startActivityForResult(android.content.Intent, int) startActivityForResult}
     *      should be called.
     */
    public void startFrom(Activity activity) {
        final Intent chooserIntent = buildChooserIntent(activity);
        activity.startActivityForResult(chooserIntent, this.requestCode);
    }

    /**
     * Starts the activity that displays the available apps.
     * @param fragment
     *      the fragment from which
     *      the {@link android.support.v4.app.Fragment#startActivityForResult(android.content.Intent, int) startActivityForResult}
     *      should be called.
     */
    public void startFrom(Fragment fragment) {
        final Intent chooserIntent = buildChooserIntent(fragment.getActivity());
        fragment.startActivityForResult(chooserIntent, this.requestCode);
    }

    /**
     * To be called from an Activity or Fragment's onCreate method.
     *
     * @param savedInstanceState the previously saved state
     */
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            outputFileUri = savedInstanceState.getParcelable(OUTPUT_FILE_URI);
            chooserTitle = savedInstanceState.getString(CHOOSER_TITLE);
            requestCode = savedInstanceState.getInt(REQUEST_CODE);
        }
    }

    /**
     * To be called from an Activity or Fragment's onSaveInstanceState method.
     *
     * @param outState the bundle to save state in
     */
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putParcelable(OUTPUT_FILE_URI, outputFileUri);
            outState.putString(CHOOSER_TITLE, chooserTitle.toString());
            outState.putInt(REQUEST_CODE, requestCode);
        }
    }

    /**
     * To be called from an Activity or Fragment's onActivityResult method.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     * @param callback the callback interface to receive the image URI.
     * @return whether or not the request code is handled.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, Callback callback) {
        if (requestCode != this.requestCode) {
            return false;
        }

        if (resultCode == Activity.RESULT_OK) {
            boolean fromCamera = checkIsFromCamera(data);
            Uri imageUri = (fromCamera) ? outputFileUri : data.getData();
            callback.onComplete(fromCamera, imageUri);
        } else {
            callback.onError(true);
        }

        return true;
    }

    private boolean checkIsFromCamera(Intent data) {
        if (data == null) {
            return true;
        }
        final String action = data.getAction();
        return action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    protected Uri getOutputFileUri() {
        return outputFileUri;
    }

    /**
     * Returns a list of Intent of apps which can take pictures.
     * @param context the context to get the package manager.
     * @return a list of Intent of apps which can take pictures.
     */
    protected List<Intent> buildCameraIntents(Context context) {
        final List<Intent> cameraIntents = new ArrayList<>();

        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputFileUri());
            cameraIntents.add(intent);
        }

        return cameraIntents;
    }

    /**
     * Returns an intent responsible for getting an image from the device.
     * @return an intent responsible for getting an image from the device.
     */
    protected Intent buildGalleryIntent() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        return galleryIntent;
    }

    protected Intent buildChooserIntent(Context context) {
        // Take a picture
        final List<Intent> cameraIntents = buildCameraIntents(context);

        // Select a picture
        final Intent galleryIntent = buildGalleryIntent();

        // Chooser
        return buildChooserIntent(chooserTitle, galleryIntent, cameraIntents);
    }

    protected Intent buildChooserIntent(CharSequence title, Intent galleryIntent, List<Intent> cameraIntents) {
        final Intent chooserIntent = Intent.createChooser(galleryIntent, title);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        return chooserIntent;
    }

    public static class Builder {

        private File outputFile;
        private CharSequence chooserTitle;
        private int requestCode;

        public Builder() {
        }

        public Builder outputFile(File outputFile) {
            if (outputFile == null) {
                throw new IllegalArgumentException("Output file must not be null.");
            }
            this.outputFile = outputFile;
            return this;
        }

        public Builder chooserTitle(CharSequence chooserTitle) {
            if (chooserTitle == null) {
                throw new IllegalArgumentException("Chooser title must not be null.");
            }
            this.chooserTitle = chooserTitle;
            return this;
        }

        public Builder requestCode(int requestCode) {
            if (requestCode < 0) {
                throw new IllegalArgumentException("Request code must be >= 0 as reply is requested.");
            }
            this.requestCode = requestCode;
            return this;
        }

        public TakeOrSelectImage build() {
            if (outputFile == null) {
                throw new IllegalStateException("Output file not set.");
            }

            if (chooserTitle == null) {
                throw new IllegalStateException("Chooser title not set.");
            }
            return new TakeOrSelectImage(outputFile, chooserTitle, requestCode);
        }
    }

    /**
     * Callback interface used with {@link #onActivityResult(int, int, android.content.Intent, com.seventythree_apps.takeorselectimage.library.TakeOrSelectImage.Callback) onActivityForResult}.
     */
    public static interface Callback {
        /**
         * Called when the user completes taking or selecting an image.
         * @param fromCamera whether or not the image comes from the camera (The user took a picture).
         * @param imageUri the URI where the image is saved.
         *                 It can be the original output file URI if the user took a picture.
         */
        void onComplete(boolean fromCamera, Uri imageUri);

        /**
         * Called when the user cancels taking or selecting an image or an error occurred.
         * @param cancelled
         */
        void onError(boolean cancelled);
    }

}
