package com.bridgecrm.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RegistrationData implements Parcelable {
    private final String user;
    private final String password;
    private final String name;

    public RegistrationData(String user, String password, String name) {
        this.user = user;
        this.password = password;
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user);
        dest.writeString(this.password);
        dest.writeString(this.name);
    }

    private RegistrationData(Parcel in) {
        this.user = in.readString();
        this.password = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<RegistrationData> CREATOR = new Parcelable.Creator<RegistrationData>() {
        public RegistrationData createFromParcel(Parcel source) {return new RegistrationData(source);}

        public RegistrationData[] newArray(int size) {return new RegistrationData[size];}
    };
}
