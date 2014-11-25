package com.bridgecrm.helper.analytics;

import android.content.Context;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.util.Collection;

import timber.log.Timber;


public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Collection<Tracker> trackers;
    private final Thread.UncaughtExceptionHandler originalHandler;
    private ExceptionParser exceptionParser;

    public CustomUncaughtExceptionHandler(Context context, Collection<Tracker> trackers, Thread.UncaughtExceptionHandler originalHandler) {
        if (trackers == null) {
            throw new NullPointerException("tracker cannot be null");
        }
        this.originalHandler = originalHandler;
        this.trackers = trackers;
        this.exceptionParser = new StandardExceptionParser(context, null);
        this.trackers = trackers;
        Timber.d("ExceptionReporter created, original handler is %s", originalHandler == null ? "null" : originalHandler.getClass().getName());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable){
        String description = "UncaughtException";
        if (this.exceptionParser != null) {
            String threadName = thread != null ? thread.getName() : null;
            description = this.exceptionParser.getDescription(threadName, throwable);
        }
        Timber.d(new StringBuilder().append("Tracking Exception: ").append(description).toString());
        for (Tracker tracker : trackers) {
            tracker.send(
                new HitBuilders.ExceptionBuilder()
                    .setDescription(description)
                    .setFatal(true)
                    .build()
            );
        }

        if (this.originalHandler != null) {
            Timber.d("Passing exception to original handler.");
            this.originalHandler.uncaughtException(thread, throwable);
        }
    }

    public ExceptionParser getExceptionParser() {
        return this.exceptionParser;
    }

    public void setExceptionParser(ExceptionParser exceptionParser) {
        this.exceptionParser = exceptionParser;
    }
}
