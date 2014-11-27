package com.bridgecrm.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseUser;

public class Account implements Parcelable{

    private String email;

    public Account(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (email != null ? !email.equals(account.email) : account.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Account{" +
            "email='" + email + '\'' +
            '}';
    }

    public static class AccountBuilder {
        private String email;

        public AccountBuilder(ParseUser user) {
            email = user.getEmail();
        }

        public Account create() {
            return new Account(email);
        }
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {dest.writeString(this.email);}

    private Account(Parcel in) {this.email = in.readString();}

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        public Account createFromParcel(Parcel source) {return new Account(source);}

        public Account[] newArray(int size) {return new Account[size];}
    };
}
