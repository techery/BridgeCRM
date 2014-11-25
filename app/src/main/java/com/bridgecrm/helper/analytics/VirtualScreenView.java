package com.bridgecrm.helper.analytics;

public enum VirtualScreenView {
    INSTALL("Install"),
    REGISTRATION_SUCCESS("RegistrationSuccess"),
    REGISTRATION_FAIL("RegistrationFail"),
    LOGIN_SUCCESS("LoginSuccess"),
    LOGIN_FAIL("LoginFail");

    private String key;

    VirtualScreenView(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
