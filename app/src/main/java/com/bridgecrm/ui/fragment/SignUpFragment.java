package com.bridgecrm.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bridgecrm.R;
import com.bridgecrm.api.model.AuthResult;
import com.bridgecrm.api.model.RegistrationData;

import butterknife.InjectView;
import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;
import me.tatarka.rxloader.RxLoader1;

public class SignUpFragment extends AuthFragment {

    // UI references.
    @NotEmpty(messageId = R.string.error_field_required, order = 1)
    @InjectView(R.id.auth_name)
    EditText nameView;

    private RxLoader1<RegistrationData, AuthResult> regJob;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        regJob = buildRegJob();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    void onAuthAttempt() {
        super.onAuthAttempt();
        boolean isValid = FormValidator.validate(this, new SimpleErrorPopupCallback(getActivity(), true));
        if (isValid) {
            regJob.restart(
                new RegistrationData(
                    emailView.getText().toString(),
                    passwordView.getText().toString(),
                    nameView.getText().toString())
            );
        }
    }

    private RxLoader1<RegistrationData, AuthResult> buildRegJob() {
        return rxLoaderManager
            .create((RegistrationData data) -> sessionManager.tryRegister(data), new AuthCallback())
            .save();
    }

}
