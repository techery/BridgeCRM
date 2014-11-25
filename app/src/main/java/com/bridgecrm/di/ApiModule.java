package com.bridgecrm.di;

import android.content.Context;

import com.bridgecrm.api.AccountApi;
import com.bridgecrm.manager.ParseManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApiModule.ParseModule.class)
public class ApiModule {

    @Provides
    AccountApi provideAccountApi() {
        return new AccountApi();
    }

    @Module
    public static class ParseModule {

        @Provides
        ParseManager provideParseManager(Context context, @Named("parse_appId") String appId, @Named("parse_clientId") String clientId) {
            return new ParseManager(context, appId, clientId);
        }

        @Provides
        @Named("parse_appId")
        String provideAppId() {
            return "XN6e67P7UtT4KpGBG7ldlkF2zQqKicIRbVSApKU5";
        }

        @Provides
        @Named("parse_clientId")
        String provideClientId() {
            return "8RRfTPSyzLBz1PwE0ElEcDn2O8UUrmpdc2FiYn5h";
        }

    }
}
