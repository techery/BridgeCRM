package com.bridgecrm.manager;

import android.content.Context;

import com.bridgecrm.api.AccountApi;
import com.bridgecrm.api.model.Account;
import com.bridgecrm.api.model.AuthResult;
import com.bridgecrm.api.model.LoginData;
import com.bridgecrm.api.model.RegistrationData;
import com.parse.ParseUser;

import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class SessionManager {

    private final Context context;
    private final PreferenceWrapper preferences;
    private final TrackingWrapper trackingWrapper;
    private final AccountApi accountApi;

    public SessionManager(Context context, PreferenceWrapper preferences, TrackingWrapper trackingWrapper, AccountApi accountApi) {
        this.context = context;
        this.preferences = preferences;
        this.trackingWrapper = trackingWrapper;
        this.accountApi = accountApi;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Current session
    ///////////////////////////////////////////////////////////////////////////

    public boolean isSessionExist() {
        return ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated();
    }

    public Account getAccount() {
        return preferences.getObjectFromGson(Account.class, false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Authentication
    ///////////////////////////////////////////////////////////////////////////

    private PublishSubject<AuthResult> authPublisher = PublishSubject.create();

    public Observable<AuthResult> getAuthPipe() {
        return authPublisher.asObservable();
    }

    public Observable<AuthResult> tryLogin(LoginData loginData) {
        return authenticate(accountApi.login(loginData.getUser(), loginData.getPassword()));
    }

    public Observable<AuthResult> tryRegister(RegistrationData regData) {
        return authenticate(accountApi.register(regData.getUser(), regData.getPassword(), regData.getName()));
    }

    private Observable<AuthResult> authenticate(Observable<Account> authJob) {
        Observable<AuthResult> job =
            authJob
                .doOnNext(account -> {
                        Timber.d("Auth success");
                    }
                )
                .doOnError(e -> {
                        Timber.w(e, "Auth failed");
                    }
                )
                .map(account -> new AuthResult(account))
                .cache();
        // pass result further, but no error - it will terminate publisher
        job.subscribe(result -> authPublisher.onNext(result), e -> {});
        return job;
    }

    public void logout() {
        ParseUser.logOut();
    }
}
