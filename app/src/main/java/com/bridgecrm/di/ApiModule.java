package com.bridgecrm.di;

import com.bridgecrm.api.AccountApi;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiModule {

    @Provides
    AccountApi provideAccountApi() {
        return new AccountApi();
    }
}
