package com.bridgecrm.api;

import com.bridgecrm.api.model.Account;
import com.parse.ParseException;
import com.parse.ParseUser;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class AccountApi {

    public Observable<Account> login(String email, String password) {
        return Observable
            .create((Subscriber<? super ParseUser> subscriber) -> {
                    try {
                        subscriber.onNext(ParseUser.logIn(email, password));
                        subscriber.onCompleted();
                    } catch (ParseException e) {
                        subscriber.onError(e);
                    }
                }
            )
            .map(user -> new Account.AccountBuilder(user).create())
            .subscribeOn(Schedulers.io());
    }

    public Observable<Account> register(String email, String password, String name) {
        return Observable
            .create((Subscriber<? super ParseUser> subscriber) -> {
                    try {
                        ParseUser user = new ParseUser();
                        user.setUsername(email);
                        user.setEmail(email);
                        user.setPassword(password);
                        user.put("name", name);
                        user.signUp();
                        subscriber.onNext(user);
                        subscriber.onCompleted();
                    } catch (ParseException e) {
                        subscriber.onError(e);
                    }
                }
            )
            .map(user -> new Account.AccountBuilder(user).create())
            .subscribeOn(Schedulers.io());
    }

}
