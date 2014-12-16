package com.bridgecrm.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bridgecrm.R;
import com.bridgecrm.api.model.AuthResult;
import com.bridgecrm.api.model.LoginData;

import butterknife.OnClick;
import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;
import me.tatarka.rxloader.RxLoader1;

public class SignInFragment extends AuthFragment {

    private RxLoader1<LoginData, AuthResult> loginJob;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginJob = buildLoginJob();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @OnClick(R.id.auth_forgot_password)
    void onForgotPassword() {
        // TODO implement forgot password with side sliding animation
    }

    @Override
    void onAuthAttempt() {
        boolean isValid = FormValidator.validate(this, new SimpleErrorPopupCallback(getActivity(), true));
        if (isValid) {
            loginJob.restart(new LoginData(emailView.getText().toString(), passwordView.getText().toString()));
        }
    }

    private RxLoader1<LoginData, AuthResult> buildLoginJob() {
        return rxLoaderManager
            .create((LoginData data) -> sessionManager.tryLogin(data), new AuthCallback())
            .save();
    }

}
