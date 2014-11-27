package com.bridgecrm.di;

import android.content.Context;

import com.bridgecrm.ui.ActivityMediator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UiModule {

    @Provides
    @Singleton
    ActivityMediator provideActivityMediator(Context context) {
        return new ActivityMediator(context);
    }
}
