package com.bridgecrm.util.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public abstract class BaseActivityMediator {

    protected Context context;
    protected WeakReference<Activity> activityRef;

    /**
     * Initiate mediator with context to start activities. If context is not
     * instance of Activity (e.g. service, receiver, etc), activities will be
     * started with {@link Intent#FLAG_ACTIVITY_NEW_TASK}
     *
     * @param context
     */
    public BaseActivityMediator(Context context) {
        this.context = context.getApplicationContext();
        if (context instanceof Activity)
            activityRef = new WeakReference<>((Activity) context);
    }

    public void swapActivity(Activity activity) {
        activityRef = new WeakReference<>(activity);
    }

    private boolean isActivityContextReady() {return activityRef.get() != null && !activityRef.get().isFinishing();}

    protected void startActivity(Class<?> cls) {
        startActivity(cls, null, null);
    }

    protected void startActivity(Class<?> cls, Bundle extras) {
        startActivity(cls, extras, null);
    }

    protected void startActivity(Class<?> cls, Bundle extras, int... flags) {
        Intent intent = new Intent(context, cls);
        // check if new task flag is needed 'cause activityRef is being started
        // from outside of activityRef context
        if (!isActivityContextReady()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        // set bundle with extras
        intent.replaceExtras(extras);
        // set intent flags
        if (flags != null)
            for (int flag : flags) {
                intent.addFlags(flag);
            }

        Context startingContext = isActivityContextReady() ? activityRef.get() : context;
        startingContext.startActivity(intent);
    }

    protected void startActivityForResult(Class<?> cls, int requestCode) {
        startActivityForResult(cls, requestCode, null);
    }

    protected void startActivityForResult(Class<?> cls, int requestCode,
                                          Bundle extras) {
        // check if new task flag is needed 'cause activityRef is being started
        // from outside of activityRef context
        if (!isActivityContextReady()) {
            throw new IllegalStateException();
        } else {
            // set bundle with extras
            Intent intent = new Intent(context, cls);
            intent.replaceExtras(extras);
            activityRef.get().startActivityForResult(intent, requestCode);
        }

    }

}
