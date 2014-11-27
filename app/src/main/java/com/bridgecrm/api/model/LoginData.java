package com.bridgecrm.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginData implements Parcelable {
    private final String user;
    private final String password;

    public LoginData(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user);
        dest.writeString(this.password);
    }

    private LoginData(Parcel in) {
        this.user = in.readString();
        this.password = in.readString();
    }

    public static final Parcelable.Creator<LoginData> CREATOR = new Parcelable.Creator<LoginData>() {
        public LoginData createFromParcel(Parcel source) {return new LoginData(source);}

        public LoginData[] newArray(int size) {return new LoginData[size];}
    };
}
