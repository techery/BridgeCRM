package com.bridgecrm.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthResult implements Parcelable {

    public final Account account;

    public AuthResult(Account account) {this.account = account;}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {dest.writeParcelable(this.account, 0);}

    private AuthResult(Parcel in) {this.account = in.readParcelable(Account.class.getClassLoader());}

    public static final Parcelable.Creator<AuthResult> CREATOR = new Parcelable.Creator<AuthResult>() {
        public AuthResult createFromParcel(Parcel source) {return new AuthResult(source);}

        public AuthResult[] newArray(int size) {return new AuthResult[size];}
    };
}
